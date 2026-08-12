/*
 *  Copyright 2018-2026 yangxj96
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

package com.devops00.spectra.core.common.service;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * IP转位置的服务
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/12/19 15:23
 */
public interface IpLocationService {

    /**
     * 查询 IP 所在地理位置，支持精度控制
     *
     * @param ip    客户端 IP 地址（IPv4），如 "8.8.8.8"
     * @param level 精度级别:
     *
     *              <pre>
     *                                                                                                                                   0 国家（如：中国）
     *                                                                                                                                   1 省份（如：中国 广东省）
     *                                                                                                                                   2 城市（如：中国 广东省 深圳市）
     *                                                                                                                                   3 运营商（如：中国 广东省 深圳市 电信）
     *              </pre>
     *
     * @return 格式化后的位置字符串，内网 IP 返回 "内网"，查询失败返回 "未知"
     */
    String getCityEn(String ip, int level);

    /**
     * 查询 IP 所在地理位置（默认精度：城市级别）
     * <p>
     * 相当于调用 {@code #getCityEn(String, int)} 并设置 level = 2。
     * </p>
     *
     * @param ip 客户端 IP 地址
     * @return 位置信息，格式：国家 省份 城市
     */
    String getCityEn(String ip);

    /**
     * 判断是否为私有（内网）IP 地址（IPv4）
     *
     * @param ip ip
     * @return 是否
     */
    boolean isPrivateIp(String ip);

    /**
     * 将 classpath 下的资源复制到临时文件，并返回 RandomAccessFile
     */
    RandomAccessFile createRandomAccessFileForResource() throws IOException;
}
