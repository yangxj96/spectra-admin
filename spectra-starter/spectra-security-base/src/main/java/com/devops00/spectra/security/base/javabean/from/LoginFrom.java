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

package com.devops00.spectra.security.base.javabean.from;

import com.devops00.spectra.security.base.constant.LoginType;

/// 登录入参
///
/// @author yangxj96
/// @version 1.0
/// @since 2025/12/2 23:15
public record LoginFrom(
        // 登录方式
        LoginType type,
        // 通用账号,可能是用户名/邮箱/手机号
        String username,
        // 账号密码
        String password,
        String captcha,
        // 手机验证码
        String smsCode,
        // 邮箱验证码
        String emailCode,
        // OTP
        String principal,
        String otp
) {
}