package io.github.yangxj96.spectra.core.service.common;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * IP转位置的服务
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/12/19 15:23
 */
public interface IpLocationService {

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
    String getCityEn(String ip, int level);

    /**
     * 查询 IP 所在地理位置（默认精度：城市级别）
     * <p>
     * 相当于调用 {@link #getCityEn(String, int)} 并设置 level = 2。
     * </p>
     *
     * @param ip 客户端 IP 地址
     * @return 位置信息，格式：国家 省份 城市
     */
    String getCityEn(String ip);

    /**
     * 判断是否为私有（内网）IP 地址（IPv4）
     * @param ip ip
     * @return 是否
     */
    boolean isPrivateIp(String ip);

    /**
     * 将 classpath 下的资源复制到临时文件，并返回 RandomAccessFile
     */
    RandomAccessFile createRandomAccessFileForResource() throws IOException;
}
