package io.github.yangxj96.spectra.core.service.common.impl;


import io.github.yangxj96.spectra.common.utils.StrUtils;
import io.github.yangxj96.spectra.core.service.common.IpLocationService;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lionsoul.ip2region.xdb.LongByteArray;
import org.lionsoul.ip2region.xdb.Searcher;
import org.lionsoul.ip2region.xdb.Version;
import org.lionsoul.ip2region.xdb.XdbException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/// IP转位置的实现
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/12/19 15:23
@Slf4j
@Service
public class IpLocationServiceImpl implements IpLocationService {

    private static final String PREFIX = "[IP2REGION]";

    @Nullable
    private Searcher searcher;

    public IpLocationServiceImpl() {
        try (var raf = createRandomAccessFileForResource()) {
            // 校验数据库完整性
            Searcher.verify(raf);
            // 加载内容到 LongByteArray
            LongByteArray buffer = Searcher.loadContent(raf);
            // 创建内存搜索器
            this.searcher = Searcher.newWithBuffer(Version.IPv4, buffer);
            // 输出成功日志
            log.info("{}IP 地理位置数据库加载成功", PREFIX);
            log.info("{}IP版本: {}", PREFIX, this.searcher.getIPVersion());
            log.info("{}总记录数: {}", PREFIX, this.searcher.getIOCount());
        } catch (FileNotFoundException e) {
            log.error("{}未找到 IP 数据库文件，请检查 resources/ip2region/ 目录下是否存在 ip2region_v4.xdb", PREFIX, e);
        } catch (XdbException e) {
            log.error("{}IP 数据库文件校验失败，请检查文件完整性: ip2region/ip2region_v4.xdb", PREFIX, e);
        } catch (IOException e) {
            log.error("{}读取 IP 数据库文件时发生 I/O 错误", PREFIX, e);
        } catch (Exception e) {
            log.error("{}初始化 IP 定位服务失败", PREFIX, e);
        }
    }

    /**
     * 格式化 IP 查询结果，按精度截断
     *
     * @param fields IP 数据字段数组，顺序：国家|省份|城市|运营商|...
     * @param level  精度级别
     * @return 格式化后的位置字符串
     */
    @NullMarked
    private static String formatRegion(String[] fields, int level) {
        var sb = new StringBuilder();

        // 国家
        if (fields.length > 0 && isValidField(fields[0])) {
            sb.append(fields[0]);
        }

        // 省份
        if (level >= 1 && fields.length > 1 && isValidField(fields[1])) {
            append(sb, fields[1]);
        }

        // 城市
        if (level >= 2 && fields.length > 2 && isValidField(fields[2])) {
            append(sb, fields[2]);
        }

        // 运营商
        if (level >= 3 && fields.length > 3 && isValidField(fields[3])) {
            append(sb, fields[3]);
        }

        return sb.toString();
    }

    /**
     * 判断字段是否有效（非空、非"0"、非"内网IP"）
     */
    private static boolean isValidField(String field) {
        return StrUtils.isNotBlank(field) && !"0".equals(field) && !"内网IP".equals(field);
    }

    /**
     * 向 StringBuilder 添加内容，自动处理空格分隔
     */
    @NullMarked
    private static void append(StringBuilder sb, String part) {
        if (!sb.isEmpty()) {
            sb.append(' ');
        }
        sb.append(part);
    }

    @Override
    public String getCityEn(String ip, int level) {
        if (isPrivateIp(ip)) {
            return "内网";
        }

        if (searcher == null) {
            log.warn("{}IP 数据库未加载，无法查询 IP: {}", PREFIX, ip);
            return "未知";
        }

        try {
            String region = searcher.search(ip);
            if (region != null && !region.isEmpty() && !"0|0|0|0|0|0|0".equals(region)) {
                String[] fields = region.split("\\|");
                return formatRegion(fields, level);
            }
        } catch (Exception e) {
            log.warn("{}查询 IP 位置失败，IP: {}, 错误: {}", PREFIX, ip, e.getMessage());
        }

        return "未知";
    }

    @Override
    public String getCityEn(String ip) {
        return getCityEn(ip, 2);
    }

    @Override
    public boolean isPrivateIp(String ip) {
        if (StrUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            return true;
        }

        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            return false;
        }

        try {
            int a = Integer.parseInt(parts[0]);
            int b = Integer.parseInt(parts[1]);

            return (a == 10) ||                       // 10.x.x.x
                    (a == 127) ||                     // 127.x.x.x
                    (a == 192 && b == 168) ||         // 192.168.x.x
                    (a == 172 && b >= 16 && b <= 31); // 172.16.0.0 ~ 172.31.255.255
        } catch (NumberFormatException _) {
            return false;
        }
    }

    @Override
    public RandomAccessFile createRandomAccessFileForResource() throws IOException {
        var resource = new ClassPathResource("ip2region/ip2region_v4.xdb");
        if (!resource.exists()) {
            throw new FileNotFoundException(PREFIX + "IP数据库文件不存在: ip2region/ip2region_v4.xdb");
        }

        // 创建临时文件
        Path tempFile = Files.createTempFile("ip2region_", ".xdb");
        tempFile.toFile().deleteOnExit(); // JVM 退出时自动删除

        // 复制资源内容
        try (var is = resource.getInputStream()) {
            Files.copy(is, tempFile, StandardCopyOption.REPLACE_EXISTING);
        }

        // 返回只读 RandomAccessFile
        return new RandomAccessFile(tempFile.toFile(), "r");
    }

    /**
     * 释放资源 关闭 Searcher 实例，释放内存映射资源。
     */
    @PreDestroy
    public void destroy() {
        if (searcher != null) {
            try {
                searcher.close();
                log.info("{}IP 地理位置数据库已关闭", PREFIX);
            } catch (IOException e) {
                log.error("{}关闭 IP 数据库失败", PREFIX, e);
            }
        }
    }

}
