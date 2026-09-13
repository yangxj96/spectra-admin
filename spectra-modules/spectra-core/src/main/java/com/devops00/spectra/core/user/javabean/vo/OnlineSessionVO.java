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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 在线用户页面展示的一条会话摘要。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OnlineSessionVO {

    /** 管理端用于精确撤销的随机会话句柄，不是认证凭据。 */
    private String sessionId;

    /** 登录客户端类型。 */
    private String clientType;

    /** 登录来源 IP。 */
    private String ip;

    /** 会话登录时间。 */
    private LocalDateTime loginTime;
}
