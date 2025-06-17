/*
 *  Copyright 2025 yangxj96.com
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
 *
 */

package com.yangxj96.spectra.common.enums;


import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>
 * 账号类型
 * </p>
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/6/15
 */
@Getter
@AllArgsConstructor
public enum AccountType implements IEnum<Short> {
    /**
     * 默认登录,也就是平台账号登录,默认注册的平台登录用邮箱作为登录账号
     */
    DEFAULT((short) 0, "邮箱登录"),
    /**
     * 手机号码登录,预留
     */
    PHONE((short) 1, "手机号码"),
    /**
     * 微信登录,预留
     */
    WECHAT((short) 2, "微信登录");

    private final short value;

    @JsonValue
    private final String desc;

    @Override
    public Short getValue() {
        return this.value;
    }
}
