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

package com.devops00.spectra.launch.migration;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 在隔离的一次性数据库上验证 Flyway V1–V9 空库装配与 V7→V9 升级。
 *
 * <p>只用专用测试库凭据创建并删除临时库 {@code devops00_spectra_db_migration_check}，不触碰业务库或其他数据库。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/16
 */
@EnabledIfSystemProperty(named = "spectra.test.real-deps", matches = "true")
class FlywayMigrationAssemblyTest {

    private static final String THROWAWAY_DATABASE = "devops00_spectra_db_migration_check";

    @Test
    void emptyDatabaseAppliesAllMigrationsAndUpgradeFromV7() throws Exception {
        String databaseUrl = requireIsolatedDependencies();
        String user = System.getenv("DB_USERNAME");
        String password = System.getenv("DB_PASSWORD");
        String maintenanceUrl = databaseUrl.replaceFirst("/devops00_spectra_db_test(?=\\?|$)", "/postgres");
        String throwawayUrl = databaseUrl.replaceFirst("/devops00_spectra_db_test(?=\\?|$)",
                "/" + THROWAWAY_DATABASE);

        recreateDatabase(maintenanceUrl, user, password);
        try {
            Flyway emptyToLatest = Flyway.configure()
                    .dataSource(throwawayUrl, user, password)
                    .locations("classpath:db/migration")
                    .load();
            assertEquals(9, emptyToLatest.migrate().migrationsExecuted,
                    "空库应应用 V1–V9 共 9 个迁移");
            emptyToLatest.validate();
            assertEquals(9, emptyToLatest.info().applied().length);
            emptyToLatest.validate();

            recreateDatabase(maintenanceUrl, user, password);
            Flyway toV7 = Flyway.configure()
                    .dataSource(throwawayUrl, user, password)
                    .locations("classpath:db/migration")
                    .target(MigrationVersion.fromVersion("7"))
                    .load();
            assertEquals(7, toV7.migrate().migrationsExecuted, "应先应用 V1–V7");
            Flyway toV8 = Flyway.configure()
                    .dataSource(throwawayUrl, user, password)
                    .locations("classpath:db/migration")
                    .load();
            assertEquals(2, toV8.migrate().migrationsExecuted, "V7→V9 升级只应用 V8–V9");
            toV8.validate();
            assertEquals(9, toV8.info().applied().length);
            assertTrue(toV8.info().applied()[8].getVersion().getVersion().equals("9"),
                    "最后一个迁移应为 V9");
        } finally {
            dropDatabase(maintenanceUrl, user, password);
        }
    }

    private static void recreateDatabase(String maintenanceUrl, String user, String password) throws Exception {
        dropDatabase(maintenanceUrl, user, password);
        try (Connection connection = DriverManager.getConnection(maintenanceUrl, user, password);
                Statement statement = connection.createStatement()) {
            statement.execute("CREATE DATABASE " + THROWAWAY_DATABASE);
        }
    }

    private static void dropDatabase(String maintenanceUrl, String user, String password) throws Exception {
        try (Connection connection = DriverManager.getConnection(maintenanceUrl, user, password);
                Statement statement = connection.createStatement()) {
            statement.execute("SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = '"
                    + THROWAWAY_DATABASE + "' AND pid <> pg_backend_pid()");
            statement.execute("DROP DATABASE IF EXISTS " + THROWAWAY_DATABASE);
        }
    }

    private static String requireIsolatedDependencies() {
        String url = System.getenv("DB_URL");
        if (url == null || !url.matches("^jdbc:postgresql://[^/]+/devops00_spectra_db_test(?:\\?.*)?$")) {
            throw new IllegalStateException("真实依赖测试只允许 devops00_spectra_db_test");
        }
        if (!"5".equals(System.getenv("REDIS_DB"))) {
            throw new IllegalStateException("真实依赖测试只允许 Redis DB 5");
        }
        return url;
    }
}
