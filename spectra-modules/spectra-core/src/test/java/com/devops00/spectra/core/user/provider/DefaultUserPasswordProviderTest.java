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

package com.devops00.spectra.core.user.provider;

import com.devops00.spectra.common.exception.DataException;
import com.devops00.spectra.core.system.constant.SystemConfigKeys;
import com.devops00.spectra.core.system.service.ConfiguredService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 默认用户密码配置读取和失败关闭测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
class DefaultUserPasswordProviderTest {

    private static final String PROVIDER_CLASS = "com.devops00.spectra.core.user.provider.DefaultUserPasswordProvider";

    @Test
    void shouldReturnOnlyTheConfiguredPasswordHash() throws Exception {
        var configuredService = mock(ConfiguredService.class);
        when(configuredService.findValue(SystemConfigKeys.USER_DEFAULT_PASSWORD))
                .thenReturn(Optional.of("$2a$12$encoded-default-password-hash"));
        var provider = createProvider(configuredService);

        var result = provider.getClass().getMethod("requireEncodedPassword").invoke(provider);

        assertEquals("$2a$12$encoded-default-password-hash", result);
    }

    @Test
    void shouldFailClosedWhenTheDefaultPasswordIsNotConfigured() throws Exception {
        var configuredService = mock(ConfiguredService.class);
        when(configuredService.findValue(SystemConfigKeys.USER_DEFAULT_PASSWORD)).thenReturn(Optional.empty());
        var provider = createProvider(configuredService);

        var exception = assertThrows(InvocationTargetException.class,
                () -> provider.getClass().getMethod("requireEncodedPassword").invoke(provider));

        assertInstanceOf(DataException.class, exception.getCause());
        assertEquals("系统默认密码未配置，请先在系统设置中配置", exception.getCause().getMessage());
    }

    private static Object createProvider(ConfiguredService configuredService) throws Exception {
        var providerType = Class.forName(PROVIDER_CLASS);
        return providerType.getConstructor(ConfiguredService.class).newInstance(configuredService);
    }
}
