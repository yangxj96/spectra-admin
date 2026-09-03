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

package com.devops00.spectra.core.notification.javabean.from;

import jakarta.validation.constraints.NotNull;
import com.devops00.spectra.common.notification.NotificationChannel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

/**
 * 通知模板预览入参。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/23
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTemplatePreviewFrom {

    /**
     * 已保存模板版本 ID；为空时预览请求中的草稿内容。
     */
    private UUID templateId;

    /**
     * 未保存草稿的通知渠道。
     */
    private NotificationChannel channel;

    /**
     * 未保存草稿的通知用途。
     */
    private String purpose;

    /**
     * 标题模板。
     */
    private String titleTemplate;

    /**
     * 纯文本正文模板。
     */
    private String contentTemplate;

    /**
     * HTML 正文模板。
     */
    private String htmlTemplate;

    /**
     * 模板参数 JSON Schema。
     */
    private Map<String, Object> parameterSchema;

    /**
     * 仅用于预览的示例参数。
     */
    @NotNull(message = "预览参数不能为空")
    private Map<String, Object> parameters;

    /**
     * 仅用于预览的敏感示例参数，不会写入数据库。
     */
    private Map<String, Object> sensitiveParameters;
}
