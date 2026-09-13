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

package com.devops00.spectra.common.security.policy;

/**
 * 封装密码相关的业务规则和判定策略。
 *
 * @param minLength        密码允许的最小字符数
 * @param requireUppercase 密码是否必须包含大写字母
 * @param requireLowercase 密码是否必须包含小写字母
 * @param requireDigit     密码是否必须包含数字
 * @param requireSpecial   密码是否必须包含特殊字符
 * @param maxAgeDays       密码的最长有效天数
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/03
 */
public record PasswordPolicy(int minLength,
                             boolean requireUppercase,
                             boolean requireLowercase,
                             boolean requireDigit,
                             boolean requireSpecial,
                             Integer maxAgeDays) {

    /** 用户主动修改密码时允许的最大长度。 */
    public static final int MAX_LENGTH = 20;

    public PasswordPolicy {
        if (minLength < 8 || minLength > MAX_LENGTH || maxAgeDays != null && maxAgeDays < 1) {
            throw new IllegalArgumentException("密码策略参数无效");
        }
    }

    /** 校验用户主动设置的新密码。 */
    public void assertAccepts(String password) {
        if (password == null
                || password.length() < minLength
                || password.length() > MAX_LENGTH
                || requireUppercase && password.codePoints().noneMatch(Character::isUpperCase)
                || requireLowercase && password.codePoints().noneMatch(Character::isLowerCase)
                || requireDigit && password.codePoints().noneMatch(Character::isDigit)
                || requireSpecial && password.codePoints().allMatch(Character::isLetterOrDigit)) {
            throw new IllegalArgumentException("新密码不符合当前安全策略");
        }
    }
}
