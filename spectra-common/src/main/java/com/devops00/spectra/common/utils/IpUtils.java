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

package com.devops00.spectra.common.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/// ip工具类
///
/// @author yangxj96
/// @version 1.0
/// @since 2025/7/23 00:00
public final class IpUtils {

    private static final String UNKNOWN = "unknown";
    private static final String LOCALHOST_IPV6 = "0:0:0:0:0:0:0:1";
    private static final String LOCALHOST_IPV4 = "127.0.0.1";

    private IpUtils() {
        // 工具类禁止实例化
    }

    /// 获取客户端真实 IP 地址（支持多级代理）
    ///
    /// @param request
    ///            HTTP 请求（允许 null）
    /// @return 非 null 的 IP 字符串，若无法获取则返回 "unknown"
    public static String getClientIP(@Nullable HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }

        // 按优先级尝试获取 IP（标准 header 名称）
        String ip = extractIpFromHeader(request, "X-Forwarded-For");
        if (isInvalidIp(ip)) {
            ip = extractIpFromHeader(request, "Proxy-Client-IP");
        }
        if (isInvalidIp(ip)) {
            ip = extractIpFromHeader(request, "WL-Proxy-Client-IP");
        }
        if (isInvalidIp(ip)) {
            ip = extractIpFromHeader(request, "X-Real-IP");
        }
        if (isInvalidIp(ip)) {
            ip = request.getRemoteAddr();
        }

        // 处理本地回环地址
        if (LOCALHOST_IPV6.equals(ip)) {
            ip = LOCALHOST_IPV4;
        }

        // 处理多级代理（如 "1.2.3.4, 5.6.7.8, unknown"）
        ip = getFirstValidIpFromList(ip);

        // 安全截断（防止超长 IP 攻击）
        return StrUtils.isNotBlank(ip) ? Objects.requireNonNullElse(StrUtils.substring(ip, 0, 255), UNKNOWN) : UNKNOWN;
    }

    /// 从指定 header 中提取 IP，自动 trim 并转为小写比较
    ///
    /// @param request
    ///            请求体
    /// @param headerName
    ///            请求头名称
    /// @return 提取出来的IP
    private static String extractIpFromHeader(HttpServletRequest request, String headerName) {
        String value = request.getHeader(headerName);
        return (value == null || value.isEmpty()) ? UNKNOWN : value.trim();
    }

    /// 判断 IP 是否无效（null、empty、blank 或 "unknown"）
    ///
    /// @param ip
    ///            需要判断的IP
    /// @return IP是否有效
    private static boolean isInvalidIp(@Nullable String ip) {
        return StrUtils.isBlank(ip) || UNKNOWN.equalsIgnoreCase(ip);
    }

    /// 从逗号分隔的 IP 列表中提取第一个有效 IP
    ///
    /// @param ipList
    ///            可能为 null 或单个 IP 或 "ip1, ip2, ..."
    /// @return 第一个有效 IP，若无则返回原值（可能为 invalid）
    private static String getFirstValidIpFromList(@Nullable String ipList) {
        if (ipList == null || StrUtils.isEmpty(ipList) || !ipList.contains(",")) {
            return ipList == null ? "" : ipList;
        }

        String[] ips = ipList.split(",");
        for (String ip : ips) {
            ip = ip.trim();
            if (!isInvalidIp(ip)) {
                return ip;
            }
        }
        // 全部无效，返回第一个（或原值）
        return ips.length > 0 ? ips[0].trim() : "";
    }
}
