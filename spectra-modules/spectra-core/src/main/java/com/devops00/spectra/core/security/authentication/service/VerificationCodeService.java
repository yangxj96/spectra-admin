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

package com.devops00.spectra.core.security.authentication.service;

/**
 * 认证验证码用例。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/21
 */
public interface VerificationCodeService {

    /** 发送短信登录验证码。 */
    void sendSmsCode(String phone);

    /** 发送邮箱登录验证码。 */
    void sendEmailCode(String email);

    /** 发送绑定手机号验证码。 */
    void sendBindingSmsCode(String phone);

    /** 发送绑定邮箱验证码。 */
    void sendBindingEmailCode(String email);
}
