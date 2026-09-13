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

package com.devops00.spectra.framework.security.configuration.session;

import com.devops00.spectra.common.constant.ClientType;
import com.devops00.spectra.common.port.security.SecuritySessionRevocationPort;
import com.devops00.spectra.framework.security.session.lifecycle.SecuritySessionRevoker;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Session 撤销端口适配契约测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
class SecuritySessionPortConfigurationTest {

    @Test
    void shouldDelegateUserClientRevocationToSecuritySessionUseCase() {
        var revoker = mock(SecuritySessionRevoker.class);
        SecuritySessionRevocationPort port = new SecuritySessionPortConfiguration()
                .securitySessionRevocationPort(revoker);
        UUID userId = UUID.randomUUID();

        port.revokeUserClientSessions(userId, ClientType.WEB);

        verify(revoker).deleteByUserIdAndClient(userId.toString(), ClientType.WEB);
    }
}
