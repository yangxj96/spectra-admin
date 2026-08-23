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

package com.devops00.spectra.notification.javabean.from;

import com.devops00.spectra.common.base.Verify;
import com.devops00.spectra.common.notification.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

/**
 * 通知模板草稿保存入参。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/23
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTemplateSaveFrom {

    /**
     * 模板版本 ID。
     */
    @Null(message = "新增模板时不能有ID", groups = Verify.Insert.class)
    @NotNull(message = "修改模板时必须提供ID", groups = Verify.Update.class)
    private UUID id;

    /**
     * 逻辑模板组编码。
     */
    @NotBlank(message = "模板组编码不能为空", groups = {Verify.Insert.class, Verify.Update.class})
    private String templateGroupCode;

    /**
     * 投递渠道。
     */
    @NotNull(message = "通知渠道不能为空", groups = {Verify.Insert.class, Verify.Update.class})
    private NotificationChannel channel;

    /**
     * 通知用途。
     */
    @NotBlank(message = "通知用途不能为空", groups = {Verify.Insert.class, Verify.Update.class})
    private String purpose;

    /**
     * 标题模板。
     */
    private String titleTemplate;

    /**
     * 纯文本正文模板。
     */
    @NotBlank(message = "正文模板不能为空", groups = {Verify.Insert.class, Verify.Update.class})
    private String contentTemplate;

    /**
     * HTML 正文模板。
     */
    private String htmlTemplate;

    /**
     * 模板参数 JSON Schema。
     */
    @NotNull(message = "模板参数 schema 不能为空", groups = {Verify.Insert.class, Verify.Update.class})
    private Map<String, Object> parameterSchema;

    /**
     * 外部供应商模板编码。
     */
    private String providerTemplateCode;

    /**
     * 乐观锁版本号。
     */
    @NotNull(message = "修改模板时必须提供版本号", groups = Verify.Update.class)
    private Long version;
}
