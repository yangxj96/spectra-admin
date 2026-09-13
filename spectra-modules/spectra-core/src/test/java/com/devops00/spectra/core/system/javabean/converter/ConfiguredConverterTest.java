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

package com.devops00.spectra.core.system.javabean.converter;

import com.devops00.spectra.core.system.javabean.entity.Configured;
import com.devops00.spectra.core.system.javabean.enums.ConfiguredValueType;
import com.devops00.spectra.core.system.javabean.enums.ConfiguredCategory;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * 系统秘密配置响应脱敏回归测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
class ConfiguredConverterTest {

    private final ConfiguredConverter converter = Mappers.getMapper(ConfiguredConverter.class);

    @Test
    void shouldHideSecretHashAndExposeOnlyItsConfiguredState() throws Exception {
        var source = new Configured();
        source.setType(ConfiguredValueType.valueOf("SECRET"));
        source.setValue("$2a$12$encoded-default-password-hash");

        var result = converter.toVO(source);

        assertNull(result.getValue());
        assertTrue((boolean) result.getClass().getMethod("isConfigured").invoke(result));
    }

    @Test
    void shouldReportUnconfiguredWhenTheSecretValueIsBlank() throws Exception {
        var source = new Configured();
        source.setType(ConfiguredValueType.valueOf("SECRET"));
        source.setValue("");

        var result = converter.toVO(source);

        assertNull(result.getValue());
        assertEquals(false, result.getClass().getMethod("isConfigured").invoke(result));
    }

    @Test
    void shouldGroupSettingsByBusinessCategory() {
        var system = setting("copyright.name", ConfiguredValueType.TEXT, "Spectra");
        var security = setting("user.default-password", ConfiguredValueType.SECRET, "encoded-hash");
        var notification = setting("notification.enabled", ConfiguredValueType.BOOL, "true");
        var securityProfile = setting("security.profile", ConfiguredValueType.SELECT, "STANDARD");
        securityProfile.setDictCode("sys_security_profile");

        assertEquals(ConfiguredCategory.SYSTEM, converter.toVO(system).getCategory());
        assertEquals(ConfiguredCategory.SECURITY, converter.toVO(security).getCategory());
        assertEquals(ConfiguredCategory.NOTIFICATION, converter.toVO(notification).getCategory());
        assertEquals("sys_security_profile", converter.toVO(securityProfile).getDictCode());
    }

    @Test
    void shouldHideSystemManagedNotificationKeysAndMarkThemReadOnly() {
        var source = setting("notification.address-encryption-key", ConfiguredValueType.TEXT, "generated-key");

        var result = converter.toVO(source);

        assertNull(result.getValue());
        assertFalse(result.isEditable());
    }

    @Test
    void shouldKeepProviderConfigurationOutOfTheGenericSettingsForm() {
        var source = setting("notification.provider.sms", ConfiguredValueType.TEXT, "provider-config");

        var result = converter.toVO(source);

        assertNull(result.getValue());
        assertFalse(result.isEditable());
    }

    private static Configured setting(String key, ConfiguredValueType type, String value) {
        var configured = new Configured();
        configured.setKey(key);
        configured.setType(type);
        configured.setValue(value);
        return configured;
    }
}
