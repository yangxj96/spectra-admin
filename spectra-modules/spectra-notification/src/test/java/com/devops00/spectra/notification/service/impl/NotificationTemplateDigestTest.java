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

import com.devops00.spectra.notification.javabean.entity.NotificationTemplateEntity;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * 通知模板版本摘要稳定性测试。
 */
class NotificationTemplateDigestTest {

    @Test
    void shouldIgnoreJsonObjectKeyOrderAndChangeWhenContentChanges() {
        var firstSchema = new LinkedHashMap<String, Object>();
        firstSchema.put("properties", Map.of("name", Map.of("type", "string"), "code", Map.of("type", "string")));
        var secondSchema = new LinkedHashMap<String, Object>();
        secondSchema.put("properties", Map.of("code", Map.of("type", "string"), "name", Map.of("type", "string")));

        var first = template(firstSchema, "正文");
        var sameContent = template(secondSchema, "正文");
        var changed = template(secondSchema, "正文-已修改");

        assertEquals(NotificationTemplateDigest.calculate(first), NotificationTemplateDigest.calculate(sameContent));
        assertNotEquals(NotificationTemplateDigest.calculate(first), NotificationTemplateDigest.calculate(changed));
    }

    private NotificationTemplateEntity template(Map<String, Object> schema, String content) {
        var template = new NotificationTemplateEntity();
        template.setTemplateGroupCode("SYSTEM_NOTICE");
        template.setChannel("IN_APP");
        template.setPurpose("SYSTEM_NOTICE");
        template.setVersionNo(1);
        template.setTitleTemplate("标题");
        template.setContentTemplate(content);
        template.setParameterSchema(schema);
        return template;
    }
}
