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

package com.devops00.spectra.framework.security.session;

import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 安全 Session 用例拆分契约测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
class SecuritySessionServiceSplitTest {

    private static final String SERVICE_PACKAGE = "com.devops00.spectra.framework.security.session.";

    @Test
    void shouldRegisterOneIndependentComponentForEachSecuritySessionUseCase() throws Exception {
        for (String serviceName : new String[]{
                "SecuritySessionIssueService",
                "SecuritySessionRefreshService",
                "SecuritySessionRevocationService",
                "SecurityOnlineUserQueryService",
                "SecurityLoginFailureStore",
                "SecuritySessionReaderService"}) {
            Class<?> serviceType = Class.forName(SERVICE_PACKAGE + serviceName);
            assertThat(serviceType.getAnnotation(Component.class))
                    .as("必须将 %s 注册为独立 Spring Bean", serviceName)
                    .isNotNull();
        }
    }

    @Test
    void shouldRemoveTheMonolithicSessionRepository() {
        assertThatThrownBy(() -> Class.forName(
                "com.devops00.spectra.framework.security.session.repository.RedisSecuritySessionRepository"))
                .isInstanceOf(ClassNotFoundException.class);
    }
}
