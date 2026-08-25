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

package com.devops00.spectra.security.base.mfa;

import org.jspecify.annotations.NullMarked;

import java.util.UUID;

/** MFA 校验窄端口；安全适配层不依赖具体 MFA 数据库实现。 */
@NullMarked
public interface SecurityMfaVerifier {

    /** 判断用户是否已经激活 TOTP 因子。 */
    boolean hasActiveTotp(UUID userId);

    /** 判断用户是否存在任意 TOTP 登记，包括已撤销的历史登记。 */
    boolean hasAnyTotpEnrollment(UUID userId);

    /** 判断用户是否存在未撤销的 TOTP 登记，包括 PENDING 和 ACTIVE 状态。 */
    boolean hasNonRevokedTotpEnrollment(UUID userId);

    /** 校验 TOTP 验证码。 */
    boolean verifyTotp(UUID userId, String code);

    /** 原子消费 Recovery Code。 */
    boolean consumeRecoveryCode(UUID userId, String code);
}
