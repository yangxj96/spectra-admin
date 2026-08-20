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

package com.devops00.spectra.security.base.policy;

/**
 * 系统密码策略快照。
 *
 * @param minLength        最小长度
 * @param requireUppercase 是否要求大写字母
 * @param requireLowercase 是否要求小写字母
 * @param requireDigit     是否要求数字
 * @param requireSpecial   是否要求特殊字符
 * @param maxAgeDays       密码最大有效天数，可为空
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/15
 */
public record PasswordPolicy(int minLength,
                             boolean requireUppercase,
                             boolean requireLowercase,
                             boolean requireDigit,
                             boolean requireSpecial,
                             Integer maxAgeDays) {

    public PasswordPolicy {
        if (minLength < 8 || maxAgeDays != null && maxAgeDays < 1) {
            throw new IllegalArgumentException("密码策略参数无效");
        }
    }

    /**
     * 校验用户主动设置的新密码，不记录或返回密码原文。
     */
    public void assertAccepts(String password) {
        if (password == null
                || password.length() < minLength
                || requireUppercase && password.chars().noneMatch(Character::isUpperCase)
                || requireLowercase && password.chars().noneMatch(Character::isLowerCase)
                || requireDigit && password.chars().noneMatch(Character::isDigit)
                || requireSpecial && password.chars().allMatch(Character::isLetterOrDigit)) {
            throw new IllegalArgumentException("新密码不符合当前安全策略");
        }
    }
}
