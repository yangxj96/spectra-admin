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

package com.devops00.spectra.upload.schema;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 文件上传数据库契约测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/08/30
 */
class FileUploadSqlContractTest {

    @Test
    void migrationDefinesFiveFileTablesInSpectraCoreWithAuditOrder() throws IOException {
        var sql = Files.readString(migrationPath());

        assertThat(sql).contains("CREATE TABLE spectra_core.file_type")
                .contains("CREATE TABLE spectra_core.file_asset")
                .contains("CREATE TABLE spectra_core.file_upload_session")
                .contains("CREATE TABLE spectra_core.file_upload_part")
                .contains("CREATE TABLE spectra_core.file_reference");
        assertThat(sql).contains("timestamp(6) with time zone")
                .contains("content_sha256")
                .contains("file_asset_id")
                .contains("created_by")
                .contains("created_at")
                .contains("updated_by")
                .contains("updated_at")
                .contains("deleted")
                .contains("version");
        assertThat(sql).contains("WHERE deleted IS NULL AND status = 'READY'")
                .contains("UNIQUE (upload_session_id, part_number)")
                .contains("REFERENCES spectra_core.file_asset(id)");
    }

    @Test
    void migrationDropsOldPhysicalFileTablesWithoutCopyingRows() throws IOException {
        var sql = Files.readString(migrationPath());

        assertThat(sql).contains("DROP TABLE IF EXISTS spectra_core.file_upload_chunk")
                .contains("DROP TABLE IF EXISTS spectra_core.file_upload_task")
                .contains("DROP TABLE IF EXISTS spectra_core.file_info");
        assertThat(sql).doesNotContain("INSERT INTO spectra_core.file_asset SELECT")
                .doesNotContain("INSERT INTO spectra_core.file_upload_session SELECT");
    }

    @Test
    void migrationRegistersFileUploadManagementMenu() throws IOException {
        var sql = Files.readString(fileUploadMenuMigrationPath());

        assertThat(sql).contains("INSERT INTO spectra_core.sys_menu")
                .contains("文件上传")
                .contains("DevopsFileUpload")
                .contains("019fdba9-f00a-7716-918c-0ca1ae929b69")
                .contains("menu_type")
                .contains("route_name");
    }

    @Test
    void migrationsDefineCanonicalXlsxPolicyAndRepairExistingRows() throws IOException {
        var baseline = Files.readString(migrationPath());
        var repair = Files.readString(fileUploadRepairMigrationPath());

        assertThat(baseline).contains("(gen_random_uuid(), 'XLSX', 'Excel 文档', '[\".xlsx\"]'::jsonb")
                .contains("[\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet\"]'::jsonb");
        assertThat(repair).contains("UPDATE spectra_core.file_type")
                .contains("allowed_extensions = '[\".xlsx\"]'::jsonb")
                .contains("allowed_content_types = '[\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet\"]'::jsonb")
                .contains("WHERE code = 'XLSX'")
                .contains("upload_enabled = true")
                .contains("enabled = true");
    }

    @Test
    void repairMigrationDocumentsEveryFileDomainColumn() throws IOException {
        assertFileDomainComments(Files.readString(fileUploadRepairMigrationPath()));
    }

    @Test
    void managementMigrationMovesFileMenusToAnIndependentSiblingDirectory() throws IOException {
        var sql = Files.readString(fileManagementMigrationPath());

        assertThat(sql).contains("DevopsFileManagement")
                .contains("文件管理")
                .contains("DevopsFileUpload")
                .contains("DevopsStorage")
                .contains("DevopsUploadTasks")
                .contains("DevopsFileReferences")
                .contains("DevopsFileTypes")
                .contains("file:admin:manage")
                .contains("019fdba9-f00a-7716-918c-0ca1ae929b65")
                .contains("UPDATE spectra_core.sys_menu");
    }

    private static void assertFileDomainComments(String sql) {
        var columns = List.of(
                List.of("file_type", "id", "code", "display_name", "allowed_extensions", "allowed_content_types", "magic_rules",
                        "max_size", "preview_enabled", "download_enabled", "upload_enabled", "dangerous", "enabled", "created_by",
                        "created_at", "updated_by", "updated_at", "deleted", "version"),
                List.of("file_asset", "id", "file_type_id", "original_name", "content_sha256", "size", "content_type",
                        "storage_provider", "storage_container", "storage_key", "status", "completed_at", "orphaned_at",
                        "cleanup_attempts", "next_cleanup_at", "created_by", "created_at", "updated_by", "updated_at", "deleted",
                        "version"),
                List.of("file_upload_session", "id", "owner_user_id", "original_name", "declared_content_type", "size",
                        "content_sha256", "chunk_size", "total_parts", "storage_provider", "transport_mode", "storage_container",
                        "staging_key", "provider_upload_id", "file_asset_id", "status", "expires_at", "last_activity_at",
                        "completed_at", "verify_started_at", "verify_finished_at", "verify_processed_bytes", "verify_total_bytes",
                        "failure_code", "cleanup_attempts", "next_cleanup_at", "created_by", "created_at", "updated_by", "updated_at",
                        "deleted", "version"),
                List.of("file_upload_part", "id", "upload_session_id", "part_number", "expected_size", "expected_sha256",
                        "uploaded_size", "actual_sha256", "provider_etag", "status", "upload_attempt", "uploaded_at", "created_by",
                        "created_at", "updated_by", "updated_at", "deleted", "version"),
                List.of("file_reference", "id", "file_asset_id", "reference_type", "reference_id", "purpose", "display_name",
                        "created_by", "created_at", "updated_by", "updated_at", "deleted", "version"));

        for (var tableColumns : columns) {
            var table = tableColumns.get(0);
            assertThat(sql).contains("COMMENT ON TABLE spectra_core." + table + " IS");
            for (var column : tableColumns.subList(1, tableColumns.size())) {
                assertThat(sql).contains("COMMENT ON COLUMN spectra_core." + table + "." + column + " IS");
            }
        }
    }

    private static Path migrationPath() {
        var current = Path.of("").toAbsolutePath();
        while (current != null) {
            var candidate = current.resolve(Path.of("spectra-admin", "spectra-config", "src", "main", "resources", "db", "migration",
                    "V7__rebuild_file_upload_domain.sql"));
            if (Files.exists(candidate)) {
                return candidate;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("找不到文件上传 V7 迁移");
    }

    private static Path fileUploadMenuMigrationPath() {
        var current = Path.of("").toAbsolutePath();
        while (current != null) {
            var candidate = current.resolve(Path.of("spectra-admin", "spectra-config", "src", "main", "resources", "db", "migration",
                    "V8__add_file_upload_menu.sql"));
            if (Files.exists(candidate)) {
                return candidate;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("找不到文件上传菜单 V8 迁移");
    }

    private static Path fileUploadRepairMigrationPath() {
        var current = Path.of("").toAbsolutePath();
        while (current != null) {
            var candidate = current.resolve(Path.of("spectra-admin", "spectra-config", "src", "main", "resources", "db", "migration",
                    "V9__repair_file_upload_metadata.sql"));
            if (Files.exists(candidate)) {
                return candidate;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("找不到文件上传元数据 V9 迁移");
    }

    private static Path fileManagementMigrationPath() {
        var current = Path.of("").toAbsolutePath();
        while (current != null) {
            var candidate = current.resolve(Path.of("spectra-admin", "spectra-config", "src", "main", "resources", "db", "migration",
                    "V11__complete_file_upload_management.sql"));
            if (Files.exists(candidate)) {
                return candidate;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("找不到文件上传管理 V11 迁移");
    }
}
