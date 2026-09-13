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

package com.devops00.spectra.core.security.policy.javabean.vo;

/**
 * 封装安全密码策略相关的响应数据。
 *
 * @param policyKey        密码策略的配置键
 * @param minLength        密码允许的最小字符数
 * @param maxLength        用户主动修改密码时允许的最大字符数
 * @param requireUppercase 密码是否必须包含大写字母
 * @param requireLowercase 密码是否必须包含小写字母
 * @param requireDigit     密码是否必须包含数字
 * @param requireSpecial   密码是否必须包含特殊字符
 * @param maxAgeDays       密码的最长有效天数
 * @param version          当前对象或配置的版本号
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
public record SecurityPasswordPolicyVO(String policyKey,
                                       Integer minLength,
                                       Integer maxLength,
                                       Boolean requireUppercase,
                                       Boolean requireLowercase,
                                       Boolean requireDigit,
                                       Boolean requireSpecial,
                                       Integer maxAgeDays,
                                       Long version) {
}
