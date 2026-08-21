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

package com.devops00.spectra.core.security.authentication.util;

import com.devops00.spectra.security.base.constant.ClientType;
import com.devops00.spectra.security.base.mfa.SecurityMfaChallengePort;
import com.devops00.spectra.security.base.mfa.SecurityMfaChallengePort.MfaLoginChallenge;
import jakarta.servlet.http.HttpServletRequest;

/** MFA 登录挑战校验工具。 */
public final class MfaChallengeUtils {

    private MfaChallengeUtils() {
    }

    public static SecurityMfaChallengePort requireChallengePort(SecurityMfaChallengePort challengePort) {
        if (challengePort == null) {
            throw new IllegalStateException("MFA 挑战存储未配置");
        }
        return challengePort;
    }

    public static MfaLoginChallenge requireSetupChallenge(SecurityMfaChallengePort challengePort,
                                                          String challengeId, HttpServletRequest request) {
        MfaLoginChallenge challenge = requireChallengePort(challengePort).find(challengeId);
        if (challenge == null || !challenge.enrollmentRequired() || challenge.enrollmentCompleted()) {
            throw new IllegalArgumentException("MFA 登记挑战不存在或已失效");
        }
        validateClient(challenge, request);
        return challenge;
    }

    public static void validateClient(MfaLoginChallenge challenge, HttpServletRequest request) {
        ClientType actual = ClientType.fromName(request.getHeader("X-Client-Type"));
        if (challenge.clientType() != actual) {
            throw new IllegalArgumentException("MFA 挑战客户端不匹配");
        }
    }
}
