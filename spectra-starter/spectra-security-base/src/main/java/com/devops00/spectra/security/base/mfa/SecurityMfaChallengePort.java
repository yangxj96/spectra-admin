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

import com.devops00.spectra.security.base.constant.ClientType;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

/** MFA 预认证挑战存储窄端口。 */
@NullMarked
public interface SecurityMfaChallengePort {

    /** 创建短期挑战；挑战值本身是一次性 Bearer 凭据。 */
    MfaLoginChallenge create(UUID userId, String username, ClientType clientType, boolean enrollmentRequired);

    /** 查询仍然有效的挑战。 */
    @Nullable
    MfaLoginChallenge find(String challengeId);

    /** 记录一次 MFA 失败尝试，达到上限后使挑战失效。 */
    boolean recordFailure(String challengeId);

    /** 首次 TOTP 登记成功后，将挑战转为已完成 MFA 的待签发状态。 */
    boolean markEnrollmentCompleted(String challengeId);

    /** 原子消费挑战，防止并发重复签发 Token。 */
    boolean consume(String challengeId);

    /** MFA 预认证挑战。 */
    record MfaLoginChallenge(String id, UUID userId, String username, ClientType clientType,
                             boolean enrollmentRequired, boolean enrollmentCompleted, long expiresAt) {
    }
}
