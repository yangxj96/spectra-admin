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

package com.devops00.spectra.core.security.secret.service;

import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.core.security.secret.javabean.entity.SecretDefinitionEntity;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 密钥导入导出传输包的口令、完整性和待启用契约测试。 */
class SecretTransferServiceTest {

    private SecretManagementService managementService;
    private SecretTransferService transferService;

    @BeforeEach
    void setUp() {
        managementService = mock(SecretManagementService.class);
        var definition = new SecretDefinitionEntity();
        definition.setCode("security.test-key");
        definition.setCategory("SECURITY_SIGNING");
        definition.setExportable(true);
        when(managementService.listDefinitions()).thenReturn(List.of(definition));
        when(managementService.currentActiveSecrets(null)).thenReturn(List.of(
                new SecretManagementService.ActiveSecret("security.test-key", "SECURITY_SIGNING", 2,
                        "fingerprint", "secret-value")));
        transferService = new SecretTransferService(managementService, new ObjectMapper());
    }

    @Test
    void exportsCurrentActiveOnlyAndImportsAsPending() {
        var exported = transferService.exportCurrent(null);

        assertEquals(1, exported.entryCount());
        assertFalse(exported.packageBase64().contains("secret-value"));
        var preview = transferService.preview(java.util.Base64.getDecoder().decode(exported.packageBase64()),
                exported.oneTimePassphrase());

        assertEquals(1, preview.entryCount());
        assertEquals("security.test-key", preview.entries().getFirst().code());
        assertEquals(List.of(), preview.conflicts());
        transferService.importPending(java.util.Base64.getDecoder().decode(exported.packageBase64()),
                exported.oneTimePassphrase());
        verify(managementService).createPending("security.test-key", "secret-value", "IMPORT");
    }

    @Test
    void rejectsWrongPassphraseWithoutWritingAnything() {
        var exported = transferService.exportCurrent(null);
        assertThrows(DataSaveException.class, () -> transferService.preview(
                java.util.Base64.getDecoder().decode(exported.packageBase64()), "wrong-passphrase-value"));
    }
}
