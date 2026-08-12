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

package com.devops00.spectra.security.base.constant;

import com.baomidou.mybatisplus.annotation.IEnum;
import lombok.Getter;

/**
 *
 * 登录方式支持
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/12/2 23:14
 *
 */
@Getter
public enum LoginType implements IEnum<Integer> {
    /**
     * 账号密码
     */
    PASSWORD(1, "password"),
    /**
     * 手机验证码
     */
    SMS(2, "sms"),
    /**
     * 扫码
     */
    OTP(3, "OTP"),
    /**
     * 邮件验证码登录
     */
    EMAIL(4, "email"),
    ;

    private final Integer value;

    private final String name;

    LoginType(Integer value, String name) {
        this.value = value;
        this.name = name;
    }

    @Override
    public Integer getValue() {
        return this.value;
    }
}
