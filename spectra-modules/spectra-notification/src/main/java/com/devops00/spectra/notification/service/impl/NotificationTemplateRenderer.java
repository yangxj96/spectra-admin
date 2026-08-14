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

package com.devops00.spectra.notification.service.impl;

import com.devops00.spectra.common.exception.DataSaveException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 独立通知模块模板渲染器，仅支持安全变量替换。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/11
 */
@Component
public class NotificationTemplateRenderer {

    /**
     * 安全变量占位符格式。
     */
    private static final Pattern VARIABLE = Pattern.compile("\\{\\{\\s*([A-Za-z0-9_.-]+)\\s*}}");

    /**
     * 渲染模板，不执行表达式。
     */
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

    /**
     * 校验模板变量是否完整，并拒绝把模板当作表达式执行。
     */
    public void validate(String template, Map<String, ?> variables) {
        validateAll(variables, template);
    }

    /**
     * 校验标题和正文共同使用的模板参数，并拒绝未被模板消费的参数。
     *
     * @param variables 模板参数
     * @param templates 标题、正文等模板
     */
    public void validateAll(Map<String, ?> variables, String... templates) {
        Set<String> referenced = new LinkedHashSet<>();
        if (templates != null) {
            for (var template : templates) {
                collectVariables(template, referenced);
            }
        }
        Map<String, ?> values = variables == null ? Map.of() : variables;
        for (var variable : referenced) {
            if (!values.containsKey(variable) || values.get(variable) == null) {
                throw new DataSaveException("通知模板缺少参数: " + variable);
            }
        }
        for (var key : values.keySet()) {
            if (!referenced.contains(key)) {
                throw new DataSaveException("通知模板包含未使用参数: " + key);
            }
        }
    }

    /**
     * 校验没有持久化模板时的标题和正文回退值；回退值允许业务参数直接作为正文快照。
     */
    public void validateFallback(String template, Map<String, ?> variables) {
        if (!StringUtils.hasText(template)) {
            throw new DataSaveException("通知模板不能为空");
        }
        var matcher = VARIABLE.matcher(template);
        while (matcher.find()) {
            if (variables == null || !variables.containsKey(matcher.group(1)) || variables.get(matcher.group(1)) == null) {
                throw new DataSaveException("通知模板缺少参数: " + matcher.group(1));
            }
        }
    }

    private void collectVariables(String template, Set<String> referenced) {
        if (!StringUtils.hasText(template)) {
            return;
        }
        var matcher = VARIABLE.matcher(template);
        while (matcher.find()) {
            referenced.add(matcher.group(1));
        }
    }

    /**
     * 邮件 HTML 仅允许普通标记，首版拒绝脚本、事件属性和外部协议。
     */
    public void validateHtml(String html) {
        if (!StringUtils.hasText(html)) {
            return;
        }
        var unsafe = html.toLowerCase(java.util.Locale.ROOT);
        if (unsafe.contains("<script") || unsafe.contains("javascript:") || unsafe.matches("(?s).*\\bon[a-z]+\\s*=.*")) {
            throw new DataSaveException("邮件模板包含不安全 HTML");
        }
    }
}
