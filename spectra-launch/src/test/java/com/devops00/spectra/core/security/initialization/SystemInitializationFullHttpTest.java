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

package com.devops00.spectra.core.security.initialization;

import com.devops00.spectra.common.port.security.SecurityInitializationTokenStore;
import com.devops00.spectra.common.port.security.SecuritySessionRevocationPort;
import com.devops00.spectra.core.security.root.RootPolicyRepository;
import com.devops00.spectra.launch.LaunchApplication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.ObjectMapper;

import javax.sql.DataSource;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 在重建的专用测试库和 Redis DB 5 上验证首次初始化的完整 HTTP 状态机。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/16
 */
@EnabledIfSystemProperty(named = "spectra.test.real-deps", matches = "true")
class SystemInitializationFullHttpTest {

    @Test
    void concurrentStartAndCompleteCreateExactlyOneEffectiveRoot() throws Exception {
        requireIsolatedDependencies();
        try (var application = new SpringApplicationBuilder(LaunchApplication.class)
                .run("--server.port=0", "--server.address=127.0.0.1", "--server.ssl.enabled=false",
                        "--spring.quartz.auto-startup=false", "--flowable.async-executor-activate=false",
                        "--logging.level.com.devops00.spectra.core.security.initialization.service.impl.SystemInitializationTokenManager=ERROR")) {
            var jdbc = new JdbcTemplate(application.getBean(DataSource.class));
            var rootPolicy = application.getBean(RootPolicyRepository.class);
            var tokens = application.getBean(SecurityInitializationTokenStore.class);
            var revocation = application.getBean(SecuritySessionRevocationPort.class);
            ObjectMapper mapper = application.getBean(ObjectMapper.class);
            int port = ((WebServerApplicationContext) application).getWebServer().getPort();
            assertEquals("UNINITIALIZED", systemState(jdbc));
            assertEquals(0L, jdbc.queryForObject("SELECT count(*) FROM spectra_core.sys_user", Long.class));
            assertEquals(0L, rootPolicy.countEffectiveDevOpsUsers());

            String token = UUID.randomUUID().toString() + UUID.randomUUID();
            String password = "B02a!" + UUID.randomUUID().toString().substring(0, 10);
            String username = "b02init" + UUID.randomUUID().toString().replace("-", "");
            var startBody = Map.of("username", username, "password", password, "real_name", "synthetic root",
                    "system_name", "B02 synthetic initialization", "system_short_name", "B02 test",
                    "default_locale", "zh-CN", "default_timezone", "Asia/Shanghai",
                    "security_profile", "STANDARD");
            tokens.clear();
            assertTrue(tokens.putIfAbsent(digest(token)));
            UUID createdUserId = null;
            try (HttpClient client = HttpClient.newHttpClient()) {
                assertEquals("UNINITIALIZED", status(client, port, mapper));
                assertEquals(403, client.send(startRequest(port, mapper, startBody, "invalid-token"),
                        HttpResponse.BodyHandlers.ofString()).statusCode());
                assertEquals("UNINITIALIZED", systemState(jdbc));

                var first = client.sendAsync(startRequest(port, mapper, startBody, token),
                        HttpResponse.BodyHandlers.ofString());
                var second = client.sendAsync(startRequest(port, mapper, startBody, token),
                        HttpResponse.BodyHandlers.ofString());
                var firstResult = first.get(20, TimeUnit.SECONDS);
                var secondResult = second.get(20, TimeUnit.SECONDS);
                assertEquals(1L, List.of(firstResult.statusCode(), secondResult.statusCode())
                        .stream()
                        .filter(code -> code == 200)
                        .count());
                assertEquals(1L, List.of(firstResult.statusCode(), secondResult.statusCode())
                        .stream()
                        .filter(code -> code == 409)
                        .count());
                String responseBody = firstResult.statusCode() == 200 ? firstResult.body() : secondResult.body();
                String initializationId = mapper.readTree(responseBody).path("data").path("initialization_id").asText();
                assertFalse(initializationId.isBlank());
                assertEquals("INITIALIZING", status(client, port, mapper));
                assertEquals(0L, rootPolicy.countEffectiveDevOpsUsers());
                assertEquals("LOCKED", jdbc.queryForObject("SELECT status FROM spectra_core.sys_user "
                        + "WHERE username = ?", String.class, username));
                createdUserId = jdbc.queryForObject("SELECT id FROM spectra_core.sys_user "
                        + "WHERE username = ?", UUID.class, username);

                var rejected = client.send(completeRequest(port, mapper, UUID.randomUUID().toString()),
                        HttpResponse.BodyHandlers.ofString());
                assertEquals(409, rejected.statusCode());
                assertEquals("INITIALIZING", systemState(jdbc));
                assertEquals(0L, rootPolicy.countEffectiveDevOpsUsers());

                var completeFirst = client.sendAsync(completeRequest(port, mapper, initializationId),
                        HttpResponse.BodyHandlers.ofString());
                var completeSecond = client.sendAsync(completeRequest(port, mapper, initializationId),
                        HttpResponse.BodyHandlers.ofString());
                var completeFirstResult = completeFirst.get(30, TimeUnit.SECONDS);
                var completeSecondResult = completeSecond.get(30, TimeUnit.SECONDS);
                assertEquals(1L, List.of(completeFirstResult.statusCode(), completeSecondResult.statusCode())
                        .stream()
                        .filter(code -> code == 200)
                        .count());
                assertEquals(1L, List.of(completeFirstResult.statusCode(), completeSecondResult.statusCode())
                        .stream()
                        .filter(code -> code == 409)
                        .count());
                assertEquals("INITIALIZED", status(client, port, mapper));
                assertEquals(1L, rootPolicy.countEffectiveDevOpsUsers());
                assertEquals("ACTIVE", jdbc.queryForObject("SELECT status FROM spectra_core.sys_user "
                        + "WHERE username = ?", String.class, username));
                assertTrue(tokens.getDigest().isEmpty());
                assertEquals(200, client.send(loginRequest(port, mapper, username, password),
                        HttpResponse.BodyHandlers.ofString()).statusCode());
            } finally {
                try {
                    if (createdUserId != null) {
                        revocation.revokeUserSessions(createdUserId);
                    }
                } finally {
                    tokens.clear();
                }
            }
        }
    }

    private static String systemState(JdbcTemplate jdbc) {
        return jdbc.queryForObject("SELECT state FROM spectra_core.sys_system_state WHERE state_key = 'SYSTEM'",
                String.class);
    }

    private static String status(HttpClient client, int port, ObjectMapper mapper) throws Exception {
        var response = client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port
                + "/api/system/initialization/status"))
                .header("Api-Version", "1.0.0")
                .GET()
                .build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        return mapper.readTree(response.body()).path("data").path("state").asText();
    }

    private static HttpRequest startRequest(int port, ObjectMapper mapper, Map<String, String> body, String token)
            throws Exception {
        return HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/api/system/initialization/start"))
                .header("Api-Version", "1.0.0")
                .header("X-Spectra-Initialization-Token", token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                .build();
    }

    private static HttpRequest completeRequest(int port, ObjectMapper mapper, String initializationId)
            throws Exception {
        return HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port
                + "/api/system/initialization/complete"))
                .header("Api-Version", "1.0.0")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(Map.of(
                        "initialization_id", initializationId))))
                .build();
    }

    private static HttpRequest loginRequest(int port, ObjectMapper mapper, String username, String password)
            throws Exception {
        return HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port
                + "/api/security/authentication/login"))
                .header("Api-Version", "1.0.0")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(Map.of(
                        "type", "PASSWORD", "username", username, "password", password))))
                .build();
    }

    private static String digest(String token) throws Exception {
        return HexFormat.of()
                .formatHex(MessageDigest.getInstance("SHA-256")
                        .digest(token.getBytes(StandardCharsets.UTF_8)));
    }

    private static void requireIsolatedDependencies() {
        String url = System.getenv("DB_URL");
        if (url == null || !url.matches("^jdbc:postgresql://[^/]+/devops00_spectra_db_test(?:\\?.*)?$")) {
            throw new IllegalStateException("真实依赖测试只允许 devops00_spectra_db_test");
        }
        if (!"5".equals(System.getenv("REDIS_DB"))) {
            throw new IllegalStateException("真实依赖测试只允许 Redis DB 5");
        }
    }
}
