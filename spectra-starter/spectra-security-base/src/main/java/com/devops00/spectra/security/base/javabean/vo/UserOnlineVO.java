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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/// 在线用户VO
///
/// @author yangxj96
/// @version 1.0
/// @since 2025/10/15 10:58
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserOnlineVO {

    /// 用户ID
    private String userId;

    /// 用户名
    private String username;

    /// 登陆类型
    private String loginType;

    /// 客户端类型
    private String clientType;

    /// IP
    private String ip;

    /// IP所在地
    private String address;

    /// 登陆时间
    private LocalDateTime loginTime;

    /// 关联的token
    private String token;
}
