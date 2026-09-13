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

package com.devops00.spectra.core.upload.schema;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 文件上传数据库基线契约测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
class FileUploadSqlContractTest {

    @Test
    void baselineDefinesTheCurrentFileTablesAndOaAssetReferences() throws IOException {
        var sql = Files.readString(baselinePath());

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
                .contains("version")
                .contains("WHERE deleted IS NULL AND status = 'READY'")
                .contains("UNIQUE (upload_session_id, part_number)")
                .contains("REFERENCES spectra_core.file_asset(id)")
                .contains("uk_oa_application_attachment_asset")
                .contains("oa_contract_version_file_asset_fk")
                .contains("oa_document_version_file_asset_fk");
        assertThat(sql).doesNotContain("CREATE TABLE spectra_core.file_info")
                .doesNotContain("CREATE TABLE spectra_core.file_upload_chunk")
                .doesNotContain("CREATE TABLE spectra_core.file_upload_task");
        for (var table : List.of("oa_application_attachment", "oa_contract_version", "oa_document_version")) {
            var start = sql.indexOf("CREATE TABLE spectra_oa." + table + " (");
            var end = sql.indexOf("\n);", start);
            assertThat(sql.substring(start, end)).contains("file_asset_id uuid").doesNotContain("file_id uuid");
        }
    }

    @Test
    void baselineContainsCanonicalFileTypeSeedsWithoutRepairStatements() throws IOException {
        var sql = Files.readString(baselinePath());

        assertThat(sql).contains("INSERT INTO spectra_core.file_type")
                .contains("'XLSX', 'Excel 文档', '[\".xlsx\"]'::jsonb")
                .contains("[\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet\"]'::jsonb")
                .contains("COMMENT ON TABLE spectra_core.file_type IS '文件类型策略'");
        assertThat(sql).doesNotContain("UPDATE spectra_core.file_type")
                .doesNotContain("DROP TABLE IF EXISTS spectra_core.file_info")
                .doesNotContain("ELF可执行");
    }

    @Test
    void baselineContainsTheFinalFileManagementMenuAndPermissionSeeds() throws IOException {
        var sql = Files.readString(baselinePath());

        assertThat(sql).contains("DevopsFileUpload")
                .contains("DevopsStorage")
                .contains("DevopsUploadTasks")
                .contains("DevopsFileReferences")
                .contains("DevopsFileTypes")
                .contains("file:admin:manage")
                .contains("019fdba9-f00a-7716-918c-0ca1ae929b84")
                .contains("DevopsSecretManagement");
        assertThat(sql).doesNotContain("UPDATE spectra_core.sys_menu")
                .doesNotContain("DevopsEncryptionKey");
    }

    @Test
    void baselineDocumentsEveryFileDomainColumn() throws IOException {
        assertFileDomainComments(Files.readString(baselinePath()));
    }

    /**
     * 处理文件相关数据。
     */
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

    /**
     * 处理路径相关数据。
     */
    private static Path baselinePath() {
        var current = Path.of("").toAbsolutePath();
        while (current != null) {
            var candidate = current.resolve(Path.of("spectra-admin", "spectra-config", "src", "main", "resources", "db", "migration",
                    "V1__init_db.sql"));
            if (Files.exists(candidate)) {
                return candidate;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("找不到 Flyway V1 基线");
    }
}
