package com.devops00.spectra.notification.template;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/** 独立通知模块模板渲染器，仅支持安全变量替换。 */
@Component
public class NotificationTemplateRenderer {

    private static final Pattern VARIABLE = Pattern.compile("\\{\\{\\s*([A-Za-z0-9_.-]+)\\s*}}" );

    /** 渲染模板，不执行表达式。 */
    public String render(String template, Map<String, ?> variables) {
        if (!StringUtils.hasText(template) || variables == null || variables.isEmpty()) {
            return template;
        }
        var matcher = VARIABLE.matcher(template);
        var result = new StringBuffer();
        while (matcher.find()) {
            var value = variables.get(matcher.group(1));
            matcher.appendReplacement(result, Matcher.quoteReplacement(value == null ? matcher.group(0) : String.valueOf(value)));
        }
        matcher.appendTail(result);
        return result.toString();
    }
}
