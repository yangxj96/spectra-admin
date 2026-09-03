/*
 * Copyright 2018-2026 yangxj96
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.devops00.spectra.common.port.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * 登录认证 Token 响应契约。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/03
 */
@Data
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SecurityToken implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 登录类型名称。 */
    private String loginType;
    /** 用户 ID。 */
    private UUID id;
    /** 用户名。 */
    private String username;
    /** Access Token。 */
    private String accessToken;
    /** Refresh Token。 */
    private String refreshToken;
    /** 当前会话可使用的稳定权限编码列表。 */
    private List<String> permissions;
    /** 是否需要先修改临时密码。 */
    private boolean passwordChangeRequired;
}
