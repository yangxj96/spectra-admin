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
import java.util.Locale;
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
     * 清除合法占位符后，用于识别残留的非法双大括号。
     */
    private static final Pattern INVALID_VARIABLE = Pattern.compile("\\{\\{|}}", Pattern.DOTALL);

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
                validatePlaceholders(template);
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
     * 校验模板声明的敏感参数是否按敏感参数通道传入。
     *
     * @param parameterSchema     模板参数 JSON Schema
     * @param parameters          普通参数
     * @param sensitiveParameters 敏感参数
     */
    public void validateParameterSecurity(Map<String, Object> parameterSchema,
                                          Map<String, ?> parameters,
                                          Map<String, ?> sensitiveParameters) {
        var sensitiveVariables = sensitiveVariables(parameterSchema);
        var ordinary = parameters == null ? Map.<String, Object>of() : parameters;
        var sensitive = sensitiveParameters == null ? Map.<String, Object>of() : sensitiveParameters;
        for (var name : ordinary.keySet()) {
            if (sensitiveVariables.contains(name)) {
                throw new DataSaveException("模板敏感参数不能作为普通参数传入: " + name);
            }
        }
        for (var name : sensitive.keySet()) {
            if (!sensitiveVariables.contains(name)) {
                throw new DataSaveException("请求敏感参数未声明为模板敏感参数: " + name);
            }
        }
        for (var name : sensitiveVariables) {
            if (!sensitive.containsKey(name) || sensitive.get(name) == null) {
                throw new DataSaveException("模板缺少敏感参数: " + name);
            }
        }
    }

    /**
     * 校验模板定义与 JSON Schema 声明的变量完全一致。
     *
     * @param parameterSchema 模板参数 JSON Schema
     * @param templates       标题、正文等模板
     */
    public void validateDefinition(Map<String, Object> parameterSchema, String... templates) {
        var referenced = new LinkedHashSet<String>();
        if (templates != null) {
            for (var template : templates) {
                validatePlaceholders(template);
                collectVariables(template, referenced);
            }
        }
        var declared = declaredVariables(parameterSchema);
        if (!declared.equals(referenced)) {
            var missing = new LinkedHashSet<>(referenced);
            missing.removeAll(declared);
            var unused = new LinkedHashSet<>(declared);
            unused.removeAll(referenced);
            throw new DataSaveException("模板变量声明与正文不一致: 缺少=" + missing + ", 多余=" + unused);
        }
        if (templates != null && templates.length > 1 && isOnlyVariable(templates[1])) {
            throw new DataSaveException("模板正文不能只有一个占位符，请补充通知语义");
        }
        for (var template : templates == null ? new String[0] : templates) {
            validateHtml(template);
        }
    }

    /**
     * 判断条件是否满足（{@code isOnlyVariable}）。
     */
    private boolean isOnlyVariable(String template) {
        return template != null && template.trim().matches("\\{\\{\\s*[A-Za-z0-9_.-]+\\s*}}");
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

    /**
     * 处理内部业务逻辑（{@code collectVariables}）。
     */
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
     * 从标准 JSON Schema 的 properties 节点提取变量名称。
     */
    private Set<String> declaredVariables(Map<String, Object> parameterSchema) {
        var propertyMap = propertyDefinitions(parameterSchema);
        var declared = new LinkedHashSet<String>();
        for (var entry : propertyMap.entrySet()) {
            var key = entry.getKey();
            if (!(key instanceof String name) || !name.matches("[A-Za-z0-9_.-]+")) {
                throw new DataSaveException("模板参数名称不合法");
            }
            validateParameterDefinition(name, entry.getValue());
            declared.add(name);
        }
        return declared;
    }

    /**
     * 提取模板参数定义中的敏感字段。
     */
    private Set<String> sensitiveVariables(Map<String, Object> parameterSchema) {
        declaredVariables(parameterSchema);
        var sensitive = new LinkedHashSet<String>();
        for (var entry : propertyDefinitions(parameterSchema).entrySet()) {
            var name = String.valueOf(entry.getKey());
            validateParameterDefinition(name, entry.getValue());
            if (Boolean.TRUE.equals(((Map<?, ?>) entry.getValue()).get("sensitive"))) {
                sensitive.add(name);
            }
        }
        return sensitive;
    }

    /**
     * 处理内部业务逻辑（{@code propertyDefinitions}）。
     */
    private Map<?, ?> propertyDefinitions(Map<String, Object> parameterSchema) {
        if (parameterSchema == null || parameterSchema.isEmpty()) {
            return Map.of();
        }
        var properties = parameterSchema.get("properties");
        if (!(properties instanceof Map<?, ?> propertyMap)) {
            throw new DataSaveException("模板参数 schema 必须包含 properties 对象");
        }
        return propertyMap;
    }

    /**
     * 校验并确保数据满足当前约束（{@code validateParameterDefinition}）。
     */
    private void validateParameterDefinition(String name, Object definition) {
        if (!(definition instanceof Map<?, ?> property)) {
            throw new DataSaveException("模板参数定义不合法: " + name);
        }
        var sensitive = property.get("sensitive");
        if (sensitive != null && !(sensitive instanceof Boolean)) {
            throw new DataSaveException("模板参数敏感标识不合法: " + name);
        }
    }

    /**
     * 邮件 HTML 仅允许普通标记，首版拒绝脚本、事件属性和外部协议。
     */
    public void validateHtml(String html) {
        if (!StringUtils.hasText(html)) {
            return;
        }
        var unsafe = html.toLowerCase(Locale.ROOT);
        if (unsafe.contains("<script")
                || unsafe.matches("(?s).*\\bon[a-z]+\\s*=.*")
                || unsafe.matches("(?s).*\\b(?:javascript|vbscript|data|file):.*")) {
            throw new DataSaveException("邮件模板包含不安全 HTML");
        }
    }

    /**
     * 拒绝看起来像占位符但不符合安全变量语法的双大括号。
     */
    private void validatePlaceholders(String template) {
        if (!StringUtils.hasText(template)) {
            return;
        }
        var remainder = VARIABLE.matcher(template).replaceAll("");
        if (INVALID_VARIABLE.matcher(remainder).find()) {
            throw new DataSaveException("通知模板包含非法占位符");
        }
    }
}
