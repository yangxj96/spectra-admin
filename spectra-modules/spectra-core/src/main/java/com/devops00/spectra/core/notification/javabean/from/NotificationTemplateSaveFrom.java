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

import com.devops00.spectra.common.notification.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 通知模板保存参数。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/30
 */
@Data
public class NotificationTemplateSaveFrom {

    /**
     * 模板编码。
     */
    @NotBlank(message = "模板编码不能为空")
    @Size(max = 100, message = "模板编码长度不能超过100")
    private String code;
    /**
     * 模板名称。
     */
    @NotBlank(message = "模板名称不能为空")
    @Size(max = 200, message = "模板名称长度不能超过200")
    private String name;
    /**
     * 适用渠道。
     */
    @NotNull(message = "模板渠道不能为空")
    private NotificationChannel channel;
    /**
     * 标题模板。
     */
    private String titleTemplate;
    /**
     * 纯文本正文模板。
     */
    @NotBlank(message = "模板正文不能为空")
    private String contentTemplate;
    /**
     * HTML 正文模板。
     */
    private String htmlTemplate;
    /**
     * 供应商模板编码。
     */
    private String providerTemplateCode;
    /**
     * 是否启用。
     */
    private Boolean enabled;
}
