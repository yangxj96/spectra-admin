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

package com.devops00.spectra.security.starter.holder;

import com.devops00.spectra.common.config.SystemConfigValueProvider;
import com.devops00.spectra.security.base.holder.SecuritySessionReader;
import com.devops00.spectra.security.base.holder.SecurityTokenAccessor;
import com.devops00.spectra.security.base.javabean.entity.SecurityUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecuritySessionContextAccessorTest {

    @Mock
    private SecuritySessionReader sessionReader;

    @Mock
    private SecurityTokenAccessor tokenAccessor;

    @Mock
    private ObjectProvider<SystemConfigValueProvider> systemConfigProvider;

    @Mock
    private SystemConfigValueProvider systemConfigValueProvider;

    private SecuritySessionContextAccessor accessor;

    @BeforeEach
    void setUp() {
        accessor = new SecuritySessionContextAccessor(sessionReader, tokenAccessor, systemConfigProvider);
    }

    @Test
    void shouldPreferUserTimezone() {
        var user = new SecurityUser();
        user.setTimezone("Asia/Tokyo");
        when(sessionReader.getCurrentUser()).thenReturn(user);

        assertThat(accessor.currentUserZoneId()).isEqualTo("Asia/Tokyo");
        verifyNoInteractions(systemConfigProvider, systemConfigValueProvider);
    }

    @Test
    void shouldUseSystemTimezoneWhenUserTimezoneIsMissing() {
        var user = new SecurityUser();
        user.setTimezone(" ");
        when(sessionReader.getCurrentUser()).thenReturn(user);
        when(systemConfigProvider.getIfAvailable()).thenReturn(systemConfigValueProvider);
        when(systemConfigValueProvider.find("system.default-timezone"))
                .thenReturn(Optional.of("Asia/Shanghai"));

        assertThat(accessor.currentUserZoneId()).isEqualTo("Asia/Shanghai");
    }

    @Test
    void shouldUseUtcWhenSystemTimezoneIsUnavailableOrInvalid() {
        when(sessionReader.getCurrentUser()).thenReturn(null);
        when(systemConfigProvider.getIfAvailable()).thenReturn(systemConfigValueProvider);
        when(systemConfigValueProvider.find("system.default-timezone"))
                .thenReturn(Optional.of("not-a-timezone"));

        assertThat(accessor.currentUserZoneId()).isEqualTo("UTC");
    }
}
