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

import com.devops00.spectra.core.notification.javabean.enums.EmailProviderType;
import com.devops00.spectra.core.notification.javabean.enums.EmailSslMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Map;

/**
 * 邮件配置保存参数。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/30
 */
@Data
public class NotificationEmailConfigSaveFrom {

    /**
     * 配置名称。
     */
    @NotBlank(message = "配置名称不能为空")
    @Size(max = 200, message = "配置名称长度不能超过200")
    private String name;
    /**
     * 供应商类型。
     */
    @NotNull(message = "邮件供应商不能为空")
    private EmailProviderType provider;
    /**
     * SMTP 主机。
     */
    private String host;
    /**
     * SMTP 端口。
     */
    private Integer port;
    /**
     * SMTP 用户名。
     */
    private String username;
    /**
     * SMTP 密码。
     */
    private String password;
    /**
     * 发件地址。
     */
    private String fromAddress;
    /**
     * 发件人名称。
     */
    private String fromName;
    /**
     * 连接加密方式。
     */
    private EmailSslMode sslMode;
    /**
     * 供应商扩展字段。
     */
    private Map<String, Object> extra;
}
