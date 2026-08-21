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

package com.devops00.spectra.security.base.javabean.vo;

import com.devops00.spectra.security.base.constant.LoginType;
import lombok.*;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * 登录认证token响应
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/6/14 00:00
 */
@Data
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TokenVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 登录类型
     */
    private LoginType loginType;

    /**
     * 用户ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private UUID id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 权限TOKEN
     */
    private String accessToken;

    /**
     * 刷新TOKEN
     */
    private String refreshToken;

    /**
     * 当前会话可使用的稳定 Permission code 列表。
     * <p>
     * RoleAssignment 和角色名称不是客户端授权事实，不随 Token 暴露。
     */
    private List<String> permissions;

    /** 是否需要在创建正式会话前完成 MFA。 */
    private boolean mfaRequired;

    /** 是否需要先完成首次 TOTP 登记。 */
    private boolean mfaEnrollmentRequired;

    /** 是否需要先修改临时密码。 */
    private boolean passwordChangeRequired;

    /** MFA 预认证挑战 ID；仅短期有效，不是 Access Token。 */
    private String mfaChallengeId;

    /** MFA 预认证挑战过期时间（毫秒时间戳）。 */
    private Long mfaChallengeExpiresAt;
}
