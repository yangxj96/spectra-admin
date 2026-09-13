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
import com.devops00.spectra.core.system.javabean.enums.ConfiguredCategory;
import com.devops00.spectra.core.system.javabean.from.ConfiguredBatchFrom;
import com.devops00.spectra.core.system.javabean.from.ConfiguredBatchItemFrom;
import com.devops00.spectra.core.system.javabean.entity.Configured;
import com.devops00.spectra.core.system.mapper.ConfiguredMapper;
import com.devops00.spectra.core.system.javabean.converter.ConfiguredConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    @Test
    void shouldPersistDictionaryCodeWhenInsertingConfiguration() {
        when(configuredMapper.selectOne(any(), anyBoolean())).thenReturn(null);
        when(configuredMapper.insert(any(Configured.class))).thenReturn(1);

        configuredService.upsertWithDictCode("security.profile", "STANDARD", ConfiguredValueType.SELECT,
                "sys_security_profile", "安全策略");

        var saved = ArgumentCaptor.forClass(Configured.class);
        verify(configuredMapper).insert(saved.capture());
        assertEquals("sys_security_profile", saved.getValue().getDictCode());
    }

    @Test
    void shouldUpdateDictionaryCodeWhenUpsertingExistingConfiguration() {
        var configured = setting(CONFIG_ID, "security.profile", ConfiguredValueType.SELECT, "STANDARD");
        when(configuredMapper.selectOne(any(), anyBoolean())).thenReturn(configured);
        when(configuredMapper.updateById(configured)).thenReturn(1);

        configuredService.upsertWithDictCode("security.profile", "STRICT", ConfiguredValueType.SELECT,
                "sys_security_profile", "安全策略");

        assertEquals("STRICT", configured.getValue());
        assertEquals("sys_security_profile", configured.getDictCode());
        verify(configuredMapper).updateById(configured);
    }

    @Test
    void shouldListSettingsInStableKeyOrderAndWithCategories() {
        var systemName = setting(CONFIG_ID, "system.name", ConfiguredValueType.TEXT, "Spectra");
        when(configuredMapper.selectList(any())).thenReturn(List.of(systemName));
        when(configuredConverter.toVOList(List.of(systemName))).thenReturn(List.of());

        configuredService.settings();

        verify(configuredMapper).selectList(any());
        verify(configuredConverter).toVOList(List.of(systemName));
    }

    @Test
    void shouldUpdateCurrentCategoryAsOneBatch() {
        var systemName = setting(CONFIG_ID, "system.name", ConfiguredValueType.TEXT, "Old name");
        var copyright = setting(UUID.fromString("018f3f2a-7c44-7d31-8c21-9a48de15f121"),
                "copyright.name", ConfiguredValueType.TEXT, "Old footer");
        when(configuredMapper.selectById(systemName.getId())).thenReturn(systemName);
        when(configuredMapper.selectById(copyright.getId())).thenReturn(copyright);
        when(configuredMapper.updateById(any(Configured.class))).thenReturn(1);

        configuredService.modifyBatch(new ConfiguredBatchFrom(ConfiguredCategory.SYSTEM, List.of(
                item(systemName.getId(), "Spectra Admin", "系统名称"),
                item(copyright.getId(), "Spectra", "版权名称"))));

        assertEquals("Spectra Admin", systemName.getValue());
        assertEquals("Spectra", copyright.getValue());
        verify(configuredMapper).updateById(systemName);
        verify(configuredMapper).updateById(copyright);
    }

    @Test
    void shouldRejectMixedCategoriesBeforeUpdatingAnySetting() {
        var systemName = setting(CONFIG_ID, "system.name", ConfiguredValueType.TEXT, "Old name");
        var notification = setting(UUID.fromString("018f3f2a-7c44-7d31-8c21-9a48de15f121"),
                "notification.enabled", ConfiguredValueType.BOOL, "false");
        when(configuredMapper.selectById(systemName.getId())).thenReturn(systemName);
        when(configuredMapper.selectById(notification.getId())).thenReturn(notification);

        assertThrows(DataException.class, () -> configuredService.modifyBatch(new ConfiguredBatchFrom(
                ConfiguredCategory.SYSTEM, List.of(item(systemName.getId(), "New name", null),
                        item(notification.getId(), "true", null)))));

        verify(configuredMapper, never()).updateById(any(Configured.class));
    }

    @Test
    void shouldHashSecretValuesAndPreserveBlankSecretValuesInBatch() {
        var defaultPassword = setting(CONFIG_ID, "user.default-password", ConfiguredValueType.SECRET, "existing-hash");
        var existingIntegrationSecret = setting(UUID.fromString("018f3f2a-7c44-7d31-8c21-9a48de15f122"),
                "security.integration-secret", ConfiguredValueType.SECRET, "existing-integration-hash");
        when(configuredMapper.selectById(defaultPassword.getId())).thenReturn(defaultPassword);
        when(configuredMapper.selectById(existingIntegrationSecret.getId())).thenReturn(existingIntegrationSecret);
        when(securityPasswordPolicyProvider.current()).thenReturn(new PasswordPolicy(12, true, true, true, true, null));
        when(passwordEncoder.encode("ValidPassword1!")).thenReturn("new-password-hash");
        when(configuredMapper.updateById(defaultPassword)).thenReturn(1);
        when(configuredMapper.updateById(existingIntegrationSecret)).thenReturn(1);

        configuredService.modifyBatch(new ConfiguredBatchFrom(ConfiguredCategory.SECURITY, List.of(
                item(defaultPassword.getId(), "ValidPassword1!", "默认密码"),
                item(existingIntegrationSecret.getId(), "", "集成秘密"))));

        assertEquals("new-password-hash", defaultPassword.getValue());
        assertEquals("existing-integration-hash", existingIntegrationSecret.getValue());
        verify(configuredMapper).updateById(defaultPassword);
        verify(passwordEncoder, never()).encode("");
    }

    @Test
    void shouldRejectSystemManagedNotificationKeyFromBatch() {
        var encryptionKey = setting(CONFIG_ID, "notification.address-encryption-key", ConfiguredValueType.TEXT, "managed-key");
        when(configuredMapper.selectById(encryptionKey.getId())).thenReturn(encryptionKey);

        assertThrows(DataException.class, () -> configuredService.modifyBatch(new ConfiguredBatchFrom(
                ConfiguredCategory.NOTIFICATION, List.of(item(CONFIG_ID, "changed", null)))));

        verify(configuredMapper, never()).updateById(any(Configured.class));
    }

    @Test
    void shouldRejectNotificationProviderSettingsFromGenericBatch() {
        var provider = setting(CONFIG_ID, "notification.provider.sms", ConfiguredValueType.TEXT, "provider-config");
        when(configuredMapper.selectById(provider.getId())).thenReturn(provider);

        assertThrows(DataException.class, () -> configuredService.modifyBatch(new ConfiguredBatchFrom(
                ConfiguredCategory.NOTIFICATION, List.of(item(CONFIG_ID, "changed", null)))));

        verify(configuredMapper, never()).updateById(any(Configured.class));
    }

    @Test
    void shouldRejectSecretOutsideTheCurrentPasswordPolicyInBatch() {
        var defaultPassword = setting(CONFIG_ID, "user.default-password", ConfiguredValueType.SECRET, "existing-hash");
        when(configuredMapper.selectById(defaultPassword.getId())).thenReturn(defaultPassword);
        when(securityPasswordPolicyProvider.current()).thenReturn(new PasswordPolicy(12, true, true, true, true, null));

        assertThrows(DataException.class, () -> configuredService.modifyBatch(new ConfiguredBatchFrom(
                ConfiguredCategory.SECURITY, List.of(item(defaultPassword.getId(), "weak", "默认密码")))));

        assertEquals("existing-hash", defaultPassword.getValue());
        verify(configuredMapper, never()).updateById(defaultPassword);
    }

    @Test
    void shouldSaveCategorySettingsWithinOneTransaction() throws NoSuchMethodException {
        var method = ConfiguredServiceImpl.class.getDeclaredMethod("modifyBatch", ConfiguredBatchFrom.class);

        assertTrue(method.isAnnotationPresent(Transactional.class));
    }

    private static Configured setting(UUID id, String key, ConfiguredValueType type, String value) {
        var configured = new Configured();
        configured.setId(id);
        configured.setKey(key);
        configured.setType(type);
        configured.setValue(value);
        return configured;
    }

    private static ConfiguredBatchItemFrom item(UUID id, String value, String remarks) {
        return new ConfiguredBatchItemFrom(id, value, remarks);
    }

    private static Configured secretConfiguration(String value) {
        var configured = new Configured();
        configured.setId(CONFIG_ID);
        configured.setType(ConfiguredValueType.valueOf("SECRET"));
        configured.setValue(value);
        return configured;
    }

}
