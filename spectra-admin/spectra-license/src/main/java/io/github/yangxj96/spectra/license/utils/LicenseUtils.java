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

package io.github.yangxj96.spectra.license.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.yangxj96.spectra.license.javabean.bean.License;

import java.io.IOException;

/**
 * 许可证工具类
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-11-11
 */
public class LicenseUtils {

    private LicenseUtils() {
    }

    public static String toJsonWithoutSignature(License license, ObjectMapper om) throws IOException {
        var temp = License.builder()
                .id(license.getId())
                .productName(license.getProductName())
                .issuedAt(license.getIssuedAt())
                .expiresAt(license.getExpiresAt())
                .hwid(license.getHwid())
                .build();
        return om
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(temp);
    }

    public static String toJson(License license, ObjectMapper om) throws IOException {
        return om
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(license);
    }

    public static String formatDuration(java.time.Duration duration) {
        if (duration.isNegative()) {
            return "已过期";
        }

        var seconds = duration.getSeconds();
        var days = seconds / (24 * 3600);
        var hours = (seconds % (24 * 3600)) / 3600;
        var minutes = (seconds % 3600) / 60;

        var sb = new StringBuilder();
        if (days > 0) sb.append(days).append("天 ");
        if (hours > 0) sb.append(hours).append("小时 ");
        if (minutes > 0) sb.append(minutes).append("分钟");

        var result = sb.toString().trim();
        return result.isEmpty() ? "少于1分钟" : result;
    }


}
