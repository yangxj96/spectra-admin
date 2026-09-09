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

package com.devops00.spectra.core.quartz.schema;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** Quartz DDL 和旧调度表清理迁移的静态契约。 */
class QuartzSchemaContractTest {

    private static final List<String> QUARTZ_TABLES = List.of(
            "QRTZ_JOB_DETAILS",
            "QRTZ_TRIGGERS",
            "QRTZ_SIMPLE_TRIGGERS",
            "QRTZ_CRON_TRIGGERS",
            "QRTZ_SIMPROP_TRIGGERS",
            "QRTZ_BLOB_TRIGGERS",
            "QRTZ_CALENDARS",
            "QRTZ_PAUSED_TRIGGER_GRPS",
            "QRTZ_FIRED_TRIGGERS",
            "QRTZ_SCHEDULER_STATE",
            "QRTZ_LOCKS");

    private static final Map<String, List<String>> QUARTZ_COLUMNS = Map.ofEntries(
            Map.entry("QRTZ_JOB_DETAILS", List.of("SCHED_NAME", "JOB_NAME", "JOB_GROUP", "DESCRIPTION",
                    "JOB_CLASS_NAME", "IS_DURABLE", "IS_NONCONCURRENT", "IS_UPDATE_DATA", "REQUESTS_RECOVERY", "JOB_DATA")),
            Map.entry("QRTZ_TRIGGERS", List.of("SCHED_NAME", "TRIGGER_NAME", "TRIGGER_GROUP", "JOB_NAME", "JOB_GROUP",
                    "DESCRIPTION", "NEXT_FIRE_TIME", "PREV_FIRE_TIME", "PRIORITY", "TRIGGER_STATE", "TRIGGER_TYPE",
                    "START_TIME", "END_TIME", "CALENDAR_NAME", "MISFIRE_INSTR", "JOB_DATA")),
            Map.entry("QRTZ_SIMPLE_TRIGGERS", List.of("SCHED_NAME", "TRIGGER_NAME", "TRIGGER_GROUP", "REPEAT_COUNT",
                    "REPEAT_INTERVAL", "TIMES_TRIGGERED")),
            Map.entry("QRTZ_CRON_TRIGGERS", List.of("SCHED_NAME", "TRIGGER_NAME", "TRIGGER_GROUP", "CRON_EXPRESSION",
                    "TIME_ZONE_ID")),
            Map.entry("QRTZ_SIMPROP_TRIGGERS", List.of("SCHED_NAME", "TRIGGER_NAME", "TRIGGER_GROUP", "STR_PROP_1",
                    "STR_PROP_2", "STR_PROP_3", "INT_PROP_1", "INT_PROP_2", "LONG_PROP_1", "LONG_PROP_2", "DEC_PROP_1",
                    "DEC_PROP_2", "BOOL_PROP_1", "BOOL_PROP_2")),
            Map.entry("QRTZ_BLOB_TRIGGERS", List.of("SCHED_NAME", "TRIGGER_NAME", "TRIGGER_GROUP", "BLOB_DATA")),
            Map.entry("QRTZ_CALENDARS", List.of("SCHED_NAME", "CALENDAR_NAME", "CALENDAR")),
            Map.entry("QRTZ_PAUSED_TRIGGER_GRPS", List.of("SCHED_NAME", "TRIGGER_GROUP")),
            Map.entry("QRTZ_FIRED_TRIGGERS", List.of("SCHED_NAME", "ENTRY_ID", "TRIGGER_NAME", "TRIGGER_GROUP",
                    "INSTANCE_NAME", "FIRED_TIME", "SCHED_TIME", "PRIORITY", "STATE", "JOB_NAME", "JOB_GROUP",
                    "IS_NONCONCURRENT", "REQUESTS_RECOVERY")),
            Map.entry("QRTZ_SCHEDULER_STATE", List.of("SCHED_NAME", "INSTANCE_NAME", "LAST_CHECKIN_TIME", "CHECKIN_INTERVAL")),
            Map.entry("QRTZ_LOCKS", List.of("SCHED_NAME", "LOCK_NAME")));

    private static final List<String> HISTORY_COLUMNS = List.of("id", "fire_instance_id", "job_key", "trigger_key", "job_type",
            "job_class_name", "trigger_type", "status", "scheduled_fire_at", "actual_fire_at", "started_at", "finished_at",
            "duration_ms", "scheduler_instance", "correlation_id", "parameter_version", "parameter_sha256", "result_summary",
            "error_code", "error_message", "created_by", "created_at", "updated_by", "updated_at", "deleted", "version");

    @Test
    void quartzMigrationMustContainTheCompletePostgresTableSet() throws IOException {
        var migration = readMigration("V14__create_quartz_schema.sql");

        assertThat(migration).contains("CREATE SCHEMA IF NOT EXISTS spectra_quartz");
        for (var table : QUARTZ_TABLES) {
            assertThat(migration).contains("CREATE TABLE spectra_quartz." + table);
        }
        assertThat(migration).contains("FOREIGN KEY");
        assertThat(migration).contains("CREATE INDEX IDX_QRTZ_J_REQ_RECOVERY");
        assertThat(migration).doesNotContain("DROP TABLE");
        for (var table : QUARTZ_TABLES) {
            assertThat(migration).containsPattern("(?s)COMMENT ON TABLE spectra_quartz\\." + table + " IS '[^']+';");
            for (var column : QUARTZ_COLUMNS.get(table)) {
                assertThat(migration).containsPattern("(?s)COMMENT ON COLUMN spectra_quartz\\." + table + "\\." + column
                        + " IS '[^']+';");
            }
        }
    }

    @Test
    void executionHistoryMigrationMustContainRequiredSafetyConstraints() throws IOException {
        var migration = readMigration("V15__create_quartz_execution_history.sql");

        assertThat(migration).contains("CREATE TABLE spectra_core.quartz_job_execution_history");
        assertThat(migration)
                .contains("fire_instance_id    VARCHAR(255) CONSTRAINT __canonical_quartz_job_execution_history_fire_instance_id_not_null NOT NULL");
        assertThat(migration).contains("parameter_sha256    VARCHAR(64)");
        assertThat(migration).contains("created_by          UUID");
        assertThat(migration).contains("created_at          TIMESTAMP(6) WITH TIME ZONE");
        assertThat(migration).contains("updated_by          UUID");
        assertThat(migration).contains("updated_at          TIMESTAMP(6) WITH TIME ZONE");
        assertThat(migration).contains("deleted             TIMESTAMP(6) WITH TIME ZONE");
        assertThat(migration).contains("scheduled_fire_at   TIMESTAMP(6) WITH TIME ZONE");
        assertThat(migration).contains("actual_fire_at      TIMESTAMP(6) WITH TIME ZONE");
        assertThat(migration).contains("started_at          TIMESTAMP(6) WITH TIME ZONE");
        assertThat(migration).contains("finished_at         TIMESTAMP(6) WITH TIME ZONE");
        assertThat(migration).contains("version             BIGINT DEFAULT 0");
        assertThat(migration).contains("COMMENT ON COLUMN spectra_core.quartz_job_execution_history.deleted");
        assertThat(migration).contains("status IN ('RUNNING', 'SUCCEEDED', 'FAILED', 'VETOED', 'ABANDONED')");
        assertThat(migration).contains("duration_ms IS NULL OR duration_ms >= 0");
        assertThat(migration).contains("finished_at >= started_at");
        assertThat(migration).contains("idx_quartz_history_job_started");
        assertThat(migration).contains("idx_quartz_history_created_at");
        assertThat(migration).doesNotContain("JOB_DATA", "parameters jsonb", "parameters_snapshot");
        assertThat(migration).contains("DEFAULT uuidv7()");
        assertThat(migration).contains("pk_quartz_job_execution_history");
        assertThat(migration).contains("uk_quartz_job_execution_history_fire_instance_id");
        assertThat(migration).containsPattern("(?s)COMMENT ON TABLE spectra_core\\.quartz_job_execution_history IS '[^']+';");
        for (var column : HISTORY_COLUMNS) {
            assertThat(migration).containsPattern("(?s)COMMENT ON COLUMN spectra_core\\.quartz_job_execution_history\\." + column
                    + " IS '[^']+';");
        }
        var tableBody = migration.substring(migration.indexOf("CREATE TABLE spectra_core.quartz_job_execution_history"),
                migration.indexOf("CONSTRAINT ck_quartz_history_status"));
        var previous = -1;
        for (var column : HISTORY_COLUMNS) {
            var position = tableBody.indexOf("    " + column);
            assertThat(position).as("历史表字段缺少或顺序不正确: %s", column).isGreaterThan(previous);
            previous = position;
        }
    }

    @Test
    void legacyMigrationMustOnlyDropKnownTablesInDependencyOrder() throws IOException {
        var migration = readMigration("V16__drop_legacy_scheduler_tables.sql");
        var operationAudit = migration.indexOf("scheduler_operation_audit");
        var loopError = migration.indexOf("scheduler_loop_error");
        var controlCommand = migration.indexOf("scheduler_control_command");
        var loopRuntime = migration.indexOf("scheduler_loop_runtime");
        var execution = migration.indexOf("scheduler_execution");
        var job = migration.indexOf("scheduler_job");

        assertThat(operationAudit).isGreaterThanOrEqualTo(0);
        assertThat(operationAudit).isLessThan(loopError);
        assertThat(loopError).isLessThan(controlCommand);
        assertThat(controlCommand).isLessThan(loopRuntime);
        assertThat(loopRuntime).isLessThan(execution);
        assertThat(execution).isLessThan(job);
        assertThat(migration).doesNotContain("INSERT INTO", "UPDATE ", "SELECT ");
    }

    private String readMigration(String name) throws IOException {
        try (var stream = getClass().getClassLoader().getResourceAsStream("db/migration/" + name)) {
            assertThat(stream).as("缺少 migration 资源: %s", name).isNotNull();
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
