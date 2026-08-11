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

package com.devops00.spectra.core.auth.javabean.constant;

import lombok.Getter;

/**
 * 登录账号状态。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/11
 */
@Getter
public enum AccountStatus {

    /** 可登录。 */
    ACTIVE((short) 1),

    /** 已禁用。 */
    DISABLED((short) 2),

    /** 尚未完成手机号或邮箱验证。 */
    UNVERIFIED((short) 3),

    /** 已生成凭证但必须先完成密码设置。 */
    PASSWORD_RESET_REQUIRED((short) 4);

    private final Short code;

    AccountStatus(Short code) {
        this.code = code;
    }
}
