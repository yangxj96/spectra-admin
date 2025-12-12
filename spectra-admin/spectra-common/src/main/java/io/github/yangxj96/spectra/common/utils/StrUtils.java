package io.github.yangxj96.spectra.common.utils;


import com.google.common.base.Strings;
import org.jspecify.annotations.Nullable;

/**
 * 字符串工具类，替代 Apache Commons Lang3 的 StringUtils。
 * <ui>
 * <li>- 兼容 JDK 8+（包括 JDK 25）</li>
 * <li>- 利用 Guava 处理 null/empty</li>
 * <li>- 行为与 org.apache.commons.lang3.StringUtils 一致</li>
 * </ui>
 */
public final class StrUtils {

    private StrUtils() {
        // 工具类禁止实例化
    }

    /**
     * 判断字符串是否为 null 或 ""
     */
    public static boolean isEmpty(@Nullable String str) {
        return Strings.isNullOrEmpty(str);
    }

    /**
     * 判断字符串是否非 null 且非 ""
     */
    public static boolean isNotEmpty(@Nullable String str) {
        return !isEmpty(str);
    }

    /**
     * 判断字符串是否为 null、"" 或仅由空白字符组成（如空格、制表符、换行符等）
     */
    public static boolean isBlank(@Nullable String str) {
        return str == null || str.isBlank();
    }

    /**
     * 判断字符串是否非 null、非 "" 且包含非空白字符
     */
    public static boolean isNotBlank(@Nullable String str) {
        return !isBlank(str);
    }

    /**
     * 安全截取子串 [start, end)
     * - 若 str 为 null，返回 null
     * - 自动处理越界（不会抛 IndexOutOfBoundsException）
     * - 行为等同于 org.apache.commons.lang3.StringUtils.substring(str, start, end)
     */
    public static @Nullable String substring(@Nullable String str, int start, int end) {
        if (str == null) {
            return null;
        }

        // 处理负数起始位置（Commons 行为：负数视为 0）
        if (start < 0) {
            start = 0;
        }
        if (end < 0) {
            end = 0;
        }
        if (start > end) {
            return "";
        }

        int len = str.length();
        if (start >= len) {
            return "";
        }
        if (end > len) {
            end = len;
        }

        return str.substring(start, end);
    }

    /**
     * 将字符串转换为 camelCase 或 PascalCase 格式
     * <p>
     * 前提：str 不得为 null（Servlet 参数名、配置 key 等场景天然满足）
     *
     * @param str                   非 null 输入字符串（如 "user_first_name"）
     * @param capitalizeFirstLetter true → PascalCase ("UserName")，false → camelCase ("userName")
     * @param delimiter             分隔符（如 '_'）
     * @return 转换后的非 null 字符串
     */
    public static String toCamelCase(String str, boolean capitalizeFirstLetter, char delimiter) {
        if (str.isEmpty()) {
            return str;
        }

        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = capitalizeFirstLetter;

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == delimiter) {
                capitalizeNext = true;
            } else {
                if (capitalizeNext) {
                    result.append(Character.toUpperCase(c));
                    capitalizeNext = false;
                } else {
                    result.append(Character.toLowerCase(c));
                }
            }
        }
        return result.toString();
    }
}