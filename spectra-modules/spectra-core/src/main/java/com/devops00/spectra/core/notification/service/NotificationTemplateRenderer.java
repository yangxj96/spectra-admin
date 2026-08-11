package com.devops00.spectra.core.notification.service;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 通知模板渲染器。
 * 模板变量格式为 {@code {{variable}}}，只替换调用方明确提供的变量，避免把模板内容当作表达式执行。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/11
 */
@Component
public class NotificationTemplateRenderer {

    private static final Pattern VARIABLE = Pattern.compile("\\{\\{\\s*([A-Za-z0-9_.-]+)\\s*}}") ;

    /**
     * 渲染模板。
     *
     * @param template 模板文本
     * @param variables 变量集合
     * @return 渲染后的文本；未提供的变量保持原样，便于审计和发现模板配置问题
     */
    public String render(String template, Map<String, ?> variables) {
        if (!StringUtils.hasText(template) || variables == null || variables.isEmpty()) {
            return template;
        }
        var matcher = VARIABLE.matcher(template);
        var result = new StringBuffer();
        while (matcher.find()) {
            var value = variables.get(matcher.group(1));
            if (value == null) {
                matcher.appendReplacement(result, Matcher.quoteReplacement(matcher.group(0)));
            } else {
                matcher.appendReplacement(result, Matcher.quoteReplacement(String.valueOf(value)));
            }
        }
        matcher.appendTail(result);
        return result.toString();
    }
}
