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

package com.devops00.spectra.core.common.constant;

/**
 * Core 使用的 Redis 缓存 key。
 *
 * <p>安全 Session、Token、防重放和登录锁定 key 由 security-base 的 {@code SecurityRedisKey}
 * 统一维护；本类只保留 Core 的验证码和认证身份绑定缓存 key。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/31
 */
public final class RedisCacheKey {

    /** Core 图形验证码。 */
    public static final String KAPTCHA = "core:common:kaptcha:up:";

    /** 登录用途的短信验证码。 */
    public static final String LOGIN_SMS_CODE = "security:verification:login:sms:";

    /** 登录用途的邮箱验证码。 */
    public static final String LOGIN_EMAIL_CODE = "security:verification:login:email:";

    /** 绑定手机号用途的短信验证码。 */
    public static final String BIND_PHONE_CODE = "security:verification:bind:phone:";

    /** 绑定邮箱用途的邮箱验证码。 */
    public static final String BIND_EMAIL_CODE = "security:verification:bind:email:";

    /** 登录用途的短信验证码失败尝试次数。 */
    public static final String LOGIN_SMS_CODE_ATTEMPTS = "security:verification:login:sms:attempts:";

    /** 登录用途的邮箱验证码失败尝试次数。 */
    public static final String LOGIN_EMAIL_CODE_ATTEMPTS = "security:verification:login:email:attempts:";

    private RedisCacheKey() {
    }
}
