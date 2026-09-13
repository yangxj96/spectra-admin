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

package com.devops00.spectra.core.system.service.impl;

import com.devops00.spectra.common.exception.DataException;
import com.devops00.spectra.common.security.policy.PasswordPolicy;
import com.devops00.spectra.common.security.policy.SecurityPasswordPolicyProvider;
import com.devops00.spectra.core.system.javabean.enums.ConfiguredValueType;
import com.devops00.spectra.core.system.javabean.from.ConfiguredFrom;
import com.devops00.spectra.core.system.javabean.entity.Configured;
import com.devops00.spectra.core.system.mapper.ConfiguredMapper;
import com.devops00.spectra.core.system.javabean.converter.ConfiguredConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 系统秘密配置的写入安全回归测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@ExtendWith(MockitoExtension.class)
class ConfiguredServiceImplTest {

    private static final UUID CONFIG_ID = UUID.fromString("018f3f2a-7c44-7d31-8c21-9a48de15f120");

    @Mock
    private ConfiguredMapper configuredMapper;

    @Mock
    private ConfiguredConverter configuredConverter;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SecurityPasswordPolicyProvider securityPasswordPolicyProvider;

    @InjectMocks
    private ConfiguredServiceImpl configuredService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(configuredService, "baseMapper", configuredMapper);
    }

    @Test
    void shouldHashNewSecretBeforePersistingIt() {
        var configured = secretConfiguration("old-hash");
        var params = secretUpdate("ValidPassword1!");
        when(configuredMapper.selectById(CONFIG_ID)).thenReturn(configured);
        when(securityPasswordPolicyProvider.current()).thenReturn(new PasswordPolicy(12, true, true, true, true, null));
        when(passwordEncoder.encode("ValidPassword1!")).thenReturn("encoded-default-password");
        when(configuredMapper.updateById(configured)).thenReturn(1);

        configuredService.modify(params);

        assertEquals("encoded-default-password", configured.getValue());
        verify(configuredMapper).updateById(configured);
    }

    @Test
    void shouldPreserveConfiguredSecretWhenTheEditInputIsBlank() {
        var configured = secretConfiguration("existing-encoded-password");
        var params = secretUpdate("");
        when(configuredMapper.selectById(CONFIG_ID)).thenReturn(configured);
        when(configuredMapper.updateById(configured)).thenReturn(1);

        configuredService.modify(params);

        assertEquals("existing-encoded-password", configured.getValue());
        verify(passwordEncoder, never()).encode("");
    }

    @Test
    void shouldRejectSecretOutsideTheCurrentPasswordPolicy() {
        var configured = secretConfiguration("existing-encoded-password");
        var params = secretUpdate("weak");
        when(configuredMapper.selectById(CONFIG_ID)).thenReturn(configured);
        when(securityPasswordPolicyProvider.current()).thenReturn(new PasswordPolicy(12, true, true, true, true, null));

        assertThrows(DataException.class, () -> configuredService.modify(params));

        assertEquals("existing-encoded-password", configured.getValue());
        verify(configuredMapper, never()).updateById(configured);
    }

    @Test
    void shouldInsertEmptySecretSettingWhenItDoesNotExist() {
        when(configuredMapper.selectOne(any(), anyBoolean())).thenReturn(null);
        when(configuredMapper.insert(any(Configured.class))).thenReturn(1);

        configuredService.ensureExists("user.default-password", "", ConfiguredValueType.SECRET, "默认密码");

        var saved = ArgumentCaptor.forClass(Configured.class);
        verify(configuredMapper).insert(saved.capture());
        assertEquals("user.default-password", saved.getValue().getKey());
        assertEquals("", saved.getValue().getValue());
        assertEquals(ConfiguredValueType.SECRET, saved.getValue().getType());
    }

    @Test
    void shouldNotOverwriteAnExistingSecretSettingOnInitialization() {
        var configured = secretConfiguration("existing-encoded-password");
        when(configuredMapper.selectOne(any(), anyBoolean())).thenReturn(configured);

        configuredService.ensureExists("user.default-password", "", ConfiguredValueType.SECRET, "默认密码");

        assertEquals("existing-encoded-password", configured.getValue());
        verify(configuredMapper, never()).insert(any(Configured.class));
        verifyNoInteractions(passwordEncoder);
    }

    private static Configured secretConfiguration(String value) {
        var configured = new Configured();
        configured.setId(CONFIG_ID);
        configured.setType(ConfiguredValueType.valueOf("SECRET"));
        configured.setValue(value);
        return configured;
    }

    private static ConfiguredFrom secretUpdate(String value) {
        var params = new ConfiguredFrom();
        params.setId(CONFIG_ID);
        params.setValue(value);
        return params;
    }
}
