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

package com.devops00.spectra.core.notification.sender;

import java.util.Map;

/**
 * 已完成占位符渲染的通知内容。
 *
 * @param templateCode         模板编码
 * @param title                渲染标题
 * @param content              渲染纯文本正文
 * @param htmlContent          渲染 HTML 正文
 * @param providerTemplateCode 供应商模板编码
 * @param parameters           合并后的模板参数
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/30
 */
public record RenderedMessage(String templateCode, String title, String content, String htmlContent,
                              String providerTemplateCode, Map<String, Object> parameters) {

    /**
     * 归一化模板参数。
     */
    public RenderedMessage {
        parameters = parameters == null ? Map.of() : Map.copyOf(parameters);
    }
}
