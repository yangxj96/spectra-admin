/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.core.security.authentication;

import java.util.List;
import java.util.UUID;

/** MFA 登记、挑战和 Recovery Code 应用服务。 */
public interface MfaService {

    MfaEnrollmentResult beginTotpEnrollment(UUID userId);

    List<String> confirmTotpEnrollment(UUID userId, UUID enrollmentId, String code);

    boolean verifyTotp(UUID userId, String code);

    boolean consumeRecoveryCode(UUID userId, String code);
}
