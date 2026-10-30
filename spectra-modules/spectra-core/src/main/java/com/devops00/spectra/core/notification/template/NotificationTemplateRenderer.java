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

package com.devops00.spectra.core.notification.template;

import com.devops00.spectra.common.exception.BusinessRuleViolationException;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 通知模板占位符渲染器，只执行 {@code {{name}}} 形式的直接变量替换。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/30
 */
public final class NotificationTemplateRenderer {

    /**
     * 占位符格式。
     */
    private static final Pattern VARIABLE = Pattern.compile("\\{\\{\\s*([A-Za-z0-9_.-]+)\\s*}}");

    private NotificationTemplateRenderer() {
    }

    /**
     * 渲染模板。
     *
     * @param template  模板内容
     * @param variables 模板参数
     * @return 渲染结果；模板为空时返回空字符串
     * @throws BusinessRuleViolationException 模板引用了缺失参数时抛出
     */
    public static String render(String template, Map<String, ?> variables) {
        if (!StringUtils.hasText(template)) {
            return "";
        }
        Map<String, ?> values = variables == null ? Map.of() : variables;
        var matcher = VARIABLE.matcher(template);
        var result = new StringBuffer();
        while (matcher.find()) {
            var name = matcher.group(1);
            if (!values.containsKey(name) || values.get(name) == null) {
                throw new BusinessRuleViolationException("通知模板缺少参数: " + name);
            }
            matcher.appendReplacement(result, Matcher.quoteReplacement(String.valueOf(values.get(name))));
        }
        matcher.appendTail(result);
        return result.toString();
    }
}
