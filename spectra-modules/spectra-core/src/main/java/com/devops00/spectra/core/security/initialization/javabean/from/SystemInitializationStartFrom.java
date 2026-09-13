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

package com.devops00.spectra.core.security.initialization.javabean.from;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 首次初始化的 DEV_OPS 账号参数。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@Data
public class SystemInitializationStartFrom {

    @NotBlank
    @Size(max = 100)
    private String username;

    @NotBlank
    @Size(min = 12, max = 128)
    private String password;

    @Size(max = 50)
    private String realName;

    @NotBlank(message = "系统名称不能为空")
    @Size(max = 100, message = "系统名称长度不能超过 100 个字符")
    private String systemName;

    @Size(max = 50, message = "系统简称长度不能超过 50 个字符")
    private String systemShortName;

    @Size(max = 512, message = "系统 Logo 地址长度不能超过 512 个字符")
    private String systemLogo;

    @Pattern(regexp = "zh-CN|en-US", message = "默认语言只支持 zh-CN 或 en-US")
    private String defaultLocale = "zh-CN";

    @Size(max = 64, message = "默认时区长度不能超过 64 个字符")
    private String defaultTimezone = "Asia/Shanghai";

    @Pattern(regexp = "STANDARD|STRICT", message = "安全策略只支持 STANDARD 或 STRICT")
    private String securityProfile = "STANDARD";
}
