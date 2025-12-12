/*
 *  Copyright 2018-2025 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.github.yangxj96.spectra.core.template;

import io.github.yangxj96.spectra.common.utils.StrUtils;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.lionsoul.ip2region.xdb.LongByteArray;
import org.lionsoul.ip2region.xdb.Searcher;
import org.lionsoul.ip2region.xdb.Version;
import org.lionsoul.ip2region.xdb.XdbException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * IP位置相关操作
 */
@Slf4j
@Component
public class IpLocationTemplate {

    private static final String PREFIX = "[IP2REGION]";

    @Nullable
    private Searcher searcher;

    /**
     * 初始化 IP 地理位置查询器
     * <p>
     * 从 classpath 加载 ip2region_v4.xdb 文件，验证完整性，并预加载到内存以提升性能。
     * 使用内存模式（newWithBuffer）可避免 I/O 开销，适合高频查询场景。
     * </p>
     */
    public IpLocationTemplate() {
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
     * 查询 IP 所在地理位置，支持精度控制
     *
     * @param ip    客户端 IP 地址（IPv4），如 "8.8.8.8"
     * @param level 精度级别：
     *              <ul>
     *                <li>0: 国家（如：中国）</li>
     *                <li>1: 省份（如：中国 广东省）</li>
     *                <li>2: 城市（如：中国 广东省 深圳市）</li>
     *                <li>3: 运营商（如：中国 广东省 深圳市 电信）</li>
     *              </ul>
     * @return 格式化后的位置字符串，内网 IP 返回 "内网"，查询失败返回 "未知"
     */
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

    /**
     * 查询 IP 所在地理位置（默认精度：城市级别）
     * <p>
     * 相当于调用 {@link #getCityEn(String, int)} 并设置 level = 2。
     * </p>
     *
     * @param ip 客户端 IP 地址
     * @return 位置信息，格式：国家 省份 城市
     */
    public String getCityEn(String ip) {
        return getCityEn(ip, 2);
    }


    /**
     * 释放资源
     * <p>
     * 关闭 Searcher 实例，释放内存映射资源。
     * </p>
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

    /**
     * 格式化 IP 查询结果，按精度截断
     *
     * @param fields IP 数据字段数组，顺序：国家|省份|城市|运营商|...
     * @param level  精度级别
     * @return 格式化后的位置字符串
     */
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
    private static void append(StringBuilder sb, String part) {
        if (!sb.isEmpty()) {
            sb.append(' ');
        }
        sb.append(part);
    }

    /**
     * 判断是否为私有（内网）IP 地址（IPv4）
     */
    private boolean isPrivateIp(String ip) {
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


    /**
     * 将 classpath 下的资源复制到临时文件，并返回 RandomAccessFile
     */
    private RandomAccessFile createRandomAccessFileForResource() throws IOException {
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

}
