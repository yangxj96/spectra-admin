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

package com.devops00.spectra.common.constant;

/**
 * Redis缓存的key
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/12/4 09:53
 */
public final class RedisCacheKey {

    /**
     * 验证码
     */
    public static final String KAPTCHA = "core:common:kaptcha:up:";

    /**
     * 短信验证码
     */
    public static final String SMS_CODE = "core:common:kaptcha:sms:";

    /**
     * 邮箱验证码
     */
    public static final String EMAIL_CODE = "core:common:kaptcha:email:";

    private RedisCacheKey() {
    }
}
