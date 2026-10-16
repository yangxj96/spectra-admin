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

package com.devops00.spectra.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 验证行政区划导入目标数据库守卫在连接前的允许列表判定。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/16
 */
class RegionImportDatabaseGuardTest {

    @Test
    void allowedIsolatedDatabasePasses() {
        assertEquals("devops00_spectra_db_test", RegionImportDatabaseGuard.validate(
                "jdbc:postgresql://127.0.0.1:5432/devops00_spectra_db_test", "devops00_spectra_db_test"));
    }

    @Test
    void parsesDatabaseFromUrlWithParameters() {
        assertEquals("spectra_region_import_test", RegionImportDatabaseGuard.validate(
                "jdbc:postgresql://127.0.0.1:5432/spectra_region_import_test?sslmode=disable",
                "other_db, spectra_region_import_test"));
    }

    @Test
    void missingAllowListRejectsBeforeConnect() {
        assertThrows(IllegalStateException.class, () -> RegionImportDatabaseGuard.validate(
                "jdbc:postgresql://127.0.0.1:5432/devops00_spectra_db_test", null));
        assertThrows(IllegalStateException.class, () -> RegionImportDatabaseGuard.validate(
                "jdbc:postgresql://127.0.0.1:5432/devops00_spectra_db_test", "  ,  "));
    }

    @Test
    void nonAllowedDatabaseIsRejected() {
        assertThrows(IllegalStateException.class, () -> RegionImportDatabaseGuard.validate(
                "jdbc:postgresql://127.0.0.1:5432/spectra_db", "devops00_spectra_db_test"));
    }

    @Test
    void missingUrlIsRejected() {
        assertThrows(IllegalStateException.class,
                () -> RegionImportDatabaseGuard.validate(" ", "devops00_spectra_db_test"));
    }
}
