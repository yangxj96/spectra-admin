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

package com.devops00.spectra.core.user.javabean.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 在线用户VO
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/6/14 00:00
 */
@Data
@Builder
public class UserOnlineVO {

    /**
     * 用户 ID。
     */
    private UUID userId;

    /**
     * 用户名。
     */
    private String username;

    /**
     * 登录类型字段。
     */
    private String loginType;

    /**
     * IP 地址。
     */
    private String ip;

    /**
     * 地址。
     */
    private String address;

    /**
     * 登录时间字段。
     */
    private LocalDateTime loginTime;

    /**
     * 访问令牌。
     */
    private String token;
}
