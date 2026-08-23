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
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 通知模板渲染规则测试。
 */
class NotificationTemplateRendererTest {

    private final NotificationTemplateRenderer renderer = new NotificationTemplateRenderer();

    @Test
    void shouldRenderKnownVariablesWithoutExecutingExpressions() {
        var result = renderer.render("审批人 {{ user.name }}，单号 {{id}}", Map.of("user.name", "Ada", "id", 7));

        assertEquals("审批人 Ada，单号 7", result);
    }

    @Test
    void shouldRejectMissingVariables() {
        assertThrows(DataSaveException.class,
                () -> renderer.validate("验证码 {{code}}", Map.of("title", "登录")));
    }

    @Test
    void shouldRejectUnusedVariables() {
        assertThrows(DataSaveException.class,
                () -> renderer.validate("正文 {{content}}", Map.of("content", "通知", "unused", "禁止")));
    }

    @Test
    void shouldRejectUnsafeHtml() {
        assertThrows(DataSaveException.class, () -> renderer.validateHtml("<img src=x onerror=alert(1) />"));
        assertThrows(DataSaveException.class, () -> renderer.validateHtml("<a href=\"javascript:alert(1)\">x</a>"));
        assertThrows(DataSaveException.class, () -> renderer.validateHtml("<img src=\"data:image/png;base64,abc\" />"));
        assertThrows(DataSaveException.class, () -> renderer.validateHtml("<a href=\"file:///tmp/a\">x</a>"));
    }

    @Test
    void shouldRequireSchemaPropertiesToMatchTemplateVariables() {
        var schema = Map.<String, Object>of("properties", Map.of("name", Map.of("type", "string")));

        renderer.validateDefinition(schema, "你好 {{name}}", "正文 {{name}}");
        assertThrows(DataSaveException.class, () -> renderer.validateDefinition(schema, "正文 {{missing}}"));
        assertThrows(DataSaveException.class, () -> renderer.validateDefinition(Map.of(), "正文 {{name}}"));
    }

    @Test
    void shouldRejectIllegalPlaceholders() {
        assertThrows(DataSaveException.class, () -> renderer.validateDefinition(Map.of(), "正文 {{bad name}}"));
        assertThrows(DataSaveException.class, () -> renderer.validateDefinition(Map.of(), "正文 {{name"));
    }
}
