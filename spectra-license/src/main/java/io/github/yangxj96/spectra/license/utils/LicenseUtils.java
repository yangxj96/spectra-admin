package io.github.yangxj96.spectra.license.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.yangxj96.spectra.license.javabean.bean.License;

import java.io.IOException;

/**
 * 许可证工具类
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

        long seconds = duration.getSeconds();
        long days = seconds / (24 * 3600);
        long hours = (seconds % (24 * 3600)) / 3600;
        long minutes = (seconds % 3600) / 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("天 ");
        if (hours > 0) sb.append(hours).append("小时 ");
        if (minutes > 0) sb.append(minutes).append("分钟");

        String result = sb.toString().trim();
        return result.isEmpty() ? "少于1分钟" : result;
    }


}
