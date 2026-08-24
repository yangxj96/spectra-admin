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
import com.devops00.spectra.common.utils.SHA256Utils;
import com.devops00.spectra.notification.javabean.entity.NotificationTemplateEntity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * 通知模板版本摘要生成器。
 *
 * <p>摘要输入包含模板版本号、正文定义和参数 Schema；对象键按字典序规范化，确保数据库 JSONB
 * 的键顺序变化不会产生不同摘要。摘要只用于版本追溯，不用于保存或回显敏感参数。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/23
 */
final class NotificationTemplateDigest {

    private NotificationTemplateDigest() {
    }

    /**
     * 计算模板版本的 SHA-256 十六进制摘要。
     *
     * @param template 模板实体
     * @return 64 位十六进制摘要
     */
    static String calculate(NotificationTemplateEntity template) {
        var payload = new LinkedHashMap<String, Object>();
        payload.put("template_group_code", template.getTemplateGroupCode());
        payload.put("channel", template.getChannel());
        payload.put("purpose", template.getPurpose());
        payload.put("version_no", template.getVersionNo());
        payload.put("title_template", template.getTitleTemplate());
        payload.put("content_template", template.getContentTemplate());
        payload.put("html_template", template.getHtmlTemplate());
        payload.put("parameter_schema", canonicalize(template.getParameterSchema()));
        payload.put("provider_template_code", template.getProviderTemplateCode());
        try {
            return SHA256Utils.hash(canonicalize(payload).toString());
        } catch (Exception exception) {
            throw new DataSaveException("生成通知模板版本摘要失败", exception);
        }
    }

    /**
     * 判断条件是否满足（{@code canonicalize}）。
     */
    private static Object canonicalize(Object value) {
        if (value instanceof Map<?, ?> map) {
            var sorted = new TreeMap<String, Object>();
            map.forEach((key, item) -> sorted.put(String.valueOf(key), canonicalize(item)));
            return sorted;
        }
        if (value instanceof Iterable<?> iterable) {
            var list = new ArrayList<Object>();
            iterable.forEach(item -> list.add(canonicalize(item)));
            return list;
        }
        return value;
    }
}
