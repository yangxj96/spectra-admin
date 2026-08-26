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

package com.devops00.spectra.core.scheduler;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 调度表字段注释迁移契约测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/26
 */
class SchedulerColumnCommentMigrationTest {

    private static final List<String> MIGRATION_FILES = List.of(
            "V5__comment_scheduler_columns.sql", "V6__create_scheduler_operation_audit.sql");
    private static final Map<String, List<String>> SCHEDULER_COLUMNS = columns();

    @Test
    void shouldDocumentEverySchedulerTableAndColumn() throws IOException {
        String migration = MIGRATION_FILES.stream()
                .map(SchedulerColumnCommentMigrationTest::findMigration)
                .peek(path -> assertNotNull(path, "找不到调度字段注释迁移"))
                .map(path -> {
                    try {
                        return Files.readString(path);
                    } catch (IOException exception) {
                        throw new IllegalStateException(exception);
                    }
                })
                .reduce("", (left, right) -> left + System.lineSeparator() + right);

        for (String table : SCHEDULER_COLUMNS.keySet()) {
            assertTrue(migration.contains("COMMENT ON TABLE spectra_core." + table + " IS '"), table);
            for (String column : SCHEDULER_COLUMNS.get(table)) {
                assertTrue(migration.contains(
                        "COMMENT ON COLUMN spectra_core." + table + "." + column + " IS '"),
                        table + "." + column);
            }
        }
    }

    private static Map<String, List<String>> columns() {
        Map<String, List<String>> columns = new LinkedHashMap<>();
        columns.put("scheduler_job", List.of(
                "id", "job_key", "name", "module", "description", "handler_key", "job_type", "run_scope",
                "definition_status", "desired_state", "schedule_kind", "cron_expression", "fixed_delay_ms",
                "initial_delay_ms", "next_fire_at", "misfire_policy", "concurrency_policy", "execution_policy",
                "parameters", "revision", "created_by", "created_at", "updated_by", "updated_at", "deleted",
                "version"));
        columns.put("scheduler_execution", List.of(
                "id", "job_id", "fire_key", "trigger_type", "status", "job_revision", "handler_version",
                "schedule_kind_snapshot", "schedule_expression_snapshot", "parameters_snapshot", "effect_type",
                "scheduled_at", "queued_at", "started_at", "finished_at", "next_retry_at", "deadline_at",
                "attempt_no", "max_attempts", "locked_by", "locked_at", "lease_expires_at", "last_heartbeat_at",
                "last_error_code", "last_error_message", "result_summary", "original_execution_id",
                "resolution_status", "resolution_reason", "resolved_by", "resolved_at", "created_by", "created_at",
                "updated_by", "updated_at", "deleted", "version"));
        columns.put("scheduler_loop_runtime", List.of(
                "id", "job_id", "session_key", "instance_id", "status", "started_at", "stopped_at",
                "last_heartbeat_at", "lease_expires_at", "last_cycle_at", "last_progress_at", "drain_deadline_at",
                "total_cycles", "total_processed", "total_failed", "consecutive_error_count", "last_error_code",
                "last_error_message", "state_reason", "created_by", "created_at", "updated_by", "updated_at",
                "deleted", "version"));
        columns.put("scheduler_control_command", List.of(
                "id", "job_id", "target_runtime_id", "target_session_key", "expected_runtime_version",
                "command_type", "status", "idempotency_key", "reason", "requested_by", "requested_at",
                "deadline_at", "applied_at", "finished_at", "result_code", "result_message", "created_by",
                "created_at", "updated_by", "updated_at", "deleted", "version"));
        columns.put("scheduler_loop_error", List.of(
                "id", "job_id", "instance_id", "runtime_id", "error_fingerprint", "error_code", "error_message",
                "status", "first_seen_at", "last_seen_at", "last_logged_at", "occurrence_count", "suppressed_count",
                "last_context", "resolved_by", "resolved_at", "resolution_reason", "created_by", "created_at",
                "updated_by", "updated_at", "deleted", "version"));
        columns.put("scheduler_operation_audit", List.of(
                "id", "job_id", "execution_id", "operation_type", "status", "idempotency_key", "reason",
                "requested_by", "requested_at", "finished_at", "result_code", "result_message", "created_by",
                "created_at", "updated_by", "updated_at", "deleted", "version"));
        return columns;
    }

    private static Path findMigration(String migrationFile) {
        for (Path candidate : List.of(
                Path.of("spectra-admin", "spectra-config", "src", "main", "resources", "db", "migration",
                        migrationFile),
                Path.of("spectra-config", "src", "main", "resources", "db", "migration", migrationFile),
                Path.of("..", "spectra-config", "src", "main", "resources", "db", "migration", migrationFile),
                Path.of("..", "..", "spectra-config", "src", "main", "resources", "db", "migration",
                        migrationFile),
                Path.of("..", "..", "..", "spectra-config", "src", "main", "resources", "db", "migration",
                        migrationFile))) {
            if (Files.isRegularFile(candidate)) {
                return candidate;
            }
        }
        return null;
    }
}
