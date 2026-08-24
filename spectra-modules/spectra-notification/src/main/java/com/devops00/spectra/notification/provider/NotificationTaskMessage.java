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

package com.devops00.spectra.notification.provider;

import com.devops00.spectra.notification.configuration.NotificationPayloadProtector;
import com.devops00.spectra.notification.javabean.entity.NotificationTaskEntity;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Provider 使用的已渲染消息视图；敏感参数只在调用 Provider 的短暂内存中解密。
 */
public final class NotificationTaskMessage {

    private NotificationTaskMessage() {
    }

    public static Message resolve(NotificationTaskEntity task, NotificationPayloadProtector protector) {
        var parameters = new LinkedHashMap<String, Object>();
        String providerTemplateCode = null;
        if (task.getExtra() != null) {
            parameters.putAll(task.getExtra());
            var configuredTemplateCode = parameters.remove("__provider_template_code");
            if (configuredTemplateCode != null && !String.valueOf(configuredTemplateCode).isBlank()) {
                providerTemplateCode = String.valueOf(configuredTemplateCode);
            }
        }
        var title = task.getTitle();
        var content = task.getContent();
        if (task.getSensitiveParametersCiphertext() != null
                && !task.getSensitiveParametersCiphertext().isBlank()) {
            var protectedPayload = protector.unprotectParameters(task.getSensitiveParametersCiphertext());
            var sensitiveParameters = protectedPayload.get("parameters");
            if (sensitiveParameters instanceof Map<?, ?> map) {
                map.forEach((key, value) -> {
                    if (key != null) {
                        parameters.put(String.valueOf(key), value);
                    }
                });
            }
            if (protectedPayload.get("title") != null) {
                title = String.valueOf(protectedPayload.get("title"));
            }
            if (protectedPayload.get("content") != null) {
                content = String.valueOf(protectedPayload.get("content"));
            }
        }
        return new Message(title, content, Map.copyOf(parameters), providerTemplateCode);
    }

    public record Message(String title, String content, Map<String, Object> parameters, String providerTemplateCode) {
    }
}
