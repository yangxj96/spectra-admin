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

package com.devops00.spectra.launch.security;

import com.devops00.spectra.launch.LaunchApplication;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.server.context.WebServerApplicationContext;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 验证应用以无 DDL 权限的运行角色启动并服务，DDL 操作被数据库拒绝。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/16
 */
@EnabledIfSystemProperty(named = "spectra.test.real-deps", matches = "true")
class RestrictedRuntimeRolePostgresTest {

    private static final String ROLE = "spectra_runtime_check";

    @Test
    void applicationStartsWithNoDdlRole() throws Exception {
        requireIsolatedDatabase();
        String url = System.getenv("DB_URL");
        String adminUser = System.getenv("DB_USERNAME");
        String adminPassword = System.getenv("DB_PASSWORD");
        String password = "Rt" + UUID.randomUUID().toString().replace("-", "");

        Flyway.configure().dataSource(url, adminUser, adminPassword).locations("classpath:db/migration").load().migrate();

        try (Connection admin = DriverManager.getConnection(url, adminUser, adminPassword)) {
            provisionRole(admin, password);
        }
        try {
            var application = new SpringApplicationBuilder(LaunchApplication.class)
                    .run("--server.port=0", "--server.address=127.0.0.1", "--server.ssl.enabled=false",
                            "--spring.quartz.auto-startup=false", "--flowable.async-executor-activate=false",
                            "--spring.flyway.enabled=false",
                            "--spring.datasource.username=" + ROLE,
                            "--spring.datasource.password=" + password);
            try (application; HttpClient client = HttpClient.newHttpClient()) {
                int port = ((WebServerApplicationContext) application).getWebServer().getPort();
                assertTrue(port > 0, "无 DDL 运行角色下应用应成功启动");
                var response = client.send(HttpRequest.newBuilder(
                        URI.create("http://127.0.0.1:" + port + "/api/security/context"))
                        .header("Api-Version", "1.0.0")
                        .GET()
                        .build(), HttpResponse.BodyHandlers.discarding());
                assertEquals(401, response.statusCode(), "运行角色可正常处理业务请求");
            }
            assertDdlIsRejected(url, password);
        } finally {
            try (Connection admin = DriverManager.getConnection(url, adminUser, adminPassword)) {
                dropRole(admin);
            }
        }
    }

    private static void provisionRole(Connection admin, String password) throws Exception {
        dropRole(admin);
        try (Statement statement = admin.createStatement()) {
            statement.execute("CREATE ROLE " + ROLE + " LOGIN PASSWORD '" + password
                    + "' NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT");
            statement.execute("""
                    DO $$
                    DECLARE schema_name text;
                    BEGIN
                      FOR schema_name IN
                        SELECT nspname FROM pg_namespace
                        WHERE nspname LIKE 'spectra_%' OR nspname = 'public'
                      LOOP
                        EXECUTE format('GRANT USAGE ON SCHEMA %I TO %I', schema_name, 'spectra_runtime_check');
                        EXECUTE format('GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA %I TO %I',
                          schema_name, 'spectra_runtime_check');
                        EXECUTE format('GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA %I TO %I',
                          schema_name, 'spectra_runtime_check');
                      END LOOP;
                    END $$;
                    """);
        }
    }

    private static void assertDdlIsRejected(String url, String password) throws Exception {
        try (Connection runtime = DriverManager.getConnection(url, ROLE, password);
                Statement statement = runtime.createStatement()) {
            assertThrows(Exception.class,
                    () -> statement.execute("CREATE TABLE spectra_core.restricted_role_ddl_check (id int)"));
        }
    }

    private static void dropRole(Connection admin) throws Exception {
        try (Statement statement = admin.createStatement()) {
            statement.execute("""
                    DO $$
                    BEGIN
                      IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'spectra_runtime_check') THEN
                        EXECUTE 'DROP OWNED BY spectra_runtime_check';
                        EXECUTE 'DROP ROLE spectra_runtime_check';
                      END IF;
                    END $$;
                    """);
        }
    }

    private static void requireIsolatedDatabase() {
        String url = System.getenv("DB_URL");
        if (url == null || !url.matches("^jdbc:postgresql://[^/]+/devops00_spectra_db_test(?:\\?.*)?$")) {
            throw new IllegalStateException("真实依赖测试只允许 devops00_spectra_db_test");
        }
    }
}
