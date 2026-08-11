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

package com.devops00.spectra.notification.configuration;

import com.devops00.spectra.notification.properties.NotificationModuleProperties;
import java.util.Base64;
import java.util.Map;

import com.devops00.spectra.common.exception.DataSaveException;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** 通知地址和敏感参数不能以明文落库的测试。 */
class NotificationPayloadProtectorTest {

    @Test
    void shouldEncryptAddressAndSensitiveParameters() {
        var key = Base64.getEncoder().encodeToString(new byte[32]);
        var properties = new NotificationModuleProperties(true, key, key);
        var protector = new NotificationPayloadProtector(properties, new ObjectMapper());

        var address = protector.protectAddress("13800138000");
        var payload = protector.protectParameters(Map.of("code", "123456"));

        assertNotEquals("13800138000", address);
        assertNotEquals("123456", payload);
        assertNotEquals(address, payload);
    }

    @Test
    void shouldRejectMissingEncryptionKey() {
        var properties = new NotificationModuleProperties(true, "", "");
        var protector = new NotificationPayloadProtector(properties, new ObjectMapper());

        assertThrows(DataSaveException.class, () -> protector.protectAddress("13800138000"));
        assertThrows(DataSaveException.class, () -> protector.protectParameters(Map.of("code", "123456")));
    }
}
