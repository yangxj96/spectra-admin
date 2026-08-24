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

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 管理员重置用户密码后的临时凭证响应。
 *
 * <p>临时密码只在本次接口响应中返回，服务端只保存密码哈希，不能通过查询接口再次获取。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPasswordResetVO implements Serializable {

    /**
     * 临时密码，仅本次响应返回。
     */
    private String temporaryPassword;

    /**
     * 临时密码过期时间。
     */
    private LocalDateTime expiresAt;

    /**
     * 用户登录后是否必须修改密码。
     */
    private boolean mustChange;
}
