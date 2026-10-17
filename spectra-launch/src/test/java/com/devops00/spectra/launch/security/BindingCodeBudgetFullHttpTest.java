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

import com.devops00.spectra.common.port.security.SecurityVerificationCodeStore;
import com.devops00.spectra.core.common.constant.RedisCacheKey;
import com.devops00.spectra.core.security.authentication.crypto.VerificationCodeDigest;
import com.devops00.spectra.core.security.authentication.identity.AuthenticationIdentifierHash;
import com.devops00.spectra.core.security.secret.service.SecretManagementService;
import com.devops00.spectra.core.security.secret.service.SecretRuntimeService;
import com.devops00.spectra.framework.security.properties.SecurityProperties;
import com.devops00.spectra.launch.LaunchApplication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import tools.jackson.databind.ObjectMapper;

import javax.sql.DataSource;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * 使用正式 HTTP、PostgreSQL 与 Redis DB 5 验证认证身份绑定验证码的用途/地址/用户绑定与原子尝试预算耗尽。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/16
 */
@EnabledIfSystemProperty(named = "spectra.test.real-deps", matches = "true")
class BindingCodeBudgetFullHttpTest {

    private static final String WRONG_CODE = "111111";

    @Test
    void bindingCodeIsBoundByAddressAndExhaustsAtomically() throws Exception {
        requireIsolatedDependencies();
        try (var application = new SpringApplicationBuilder(LaunchApplication.class)
                .run("--server.port=0", "--server.address=127.0.0.1", "--server.ssl.enabled=false",
                        "--spring.quartz.auto-startup=false", "--flowable.async-executor-activate=false",
                        "--spectra.security.secret.master-key=" + randomSecret())) {
            int port = ((WebServerApplicationContext) application).getWebServer().getPort();
            var mapper = application.getBean(ObjectMapper.class);
            var jdbc = new JdbcTemplate(application.getBean(DataSource.class));
            var codes = application.getBean(SecurityVerificationCodeStore.class);
            var secrets = application.getBean(SecretRuntimeService.class);
            var secretManagement = application.getBean(SecretManagementService.class);
            var properties = application.getBean(SecurityProperties.class);
            var redis = securityRedisTemplate(application);
            var fixture = new Fixture(application);
            TestUser user = TestUser.create();
            int maxAttempts = properties.getVerificationCodeMaxAttempts();
            UUID secretVersionId = null;
            try (HttpClient client = HttpClient.newHttpClient()) {
                secretVersionId = secretManagement.createPending("security.verification-code-hmac",
                        randomSecret(), "GENERATED").getId();
                secretManagement.publish("security.verification-code-hmac", secretVersionId);
                fixture.insert(user);
                String token = login(client, port, mapper, user.username(), user.password());

                String emailA = uniqueEmail();
                String codeA = "246813";
                seedBindingCode(codes, secrets, emailA, user.id(), codeA);
                for (int attempt = 0; attempt < maxAttempts - 1; attempt++) {
                    assertEquals(400, bindEmail(client, port, mapper, token, emailA, WRONG_CODE).statusCode(),
                            "错误验证码应被拒绝");
                }
                assertEquals(200, bindEmail(client, port, mapper, token, emailA, codeA).statusCode(),
                        "预算内的正确验证码应绑定成功");
                assertEquals(1L, jdbc.queryForObject("SELECT count(*) FROM spectra_security.sec_authentication_identity "
                        + "WHERE user_id = ? AND method_code = 'EMAIL' AND identifier_hash = ?", Long.class,
                        user.id(), AuthenticationIdentifierHash.digest(emailA)));

                String emailB = uniqueEmail();
                String codeB = "975310";
                seedBindingCode(codes, secrets, emailB, user.id(), codeB);
                for (int attempt = 0; attempt < maxAttempts; attempt++) {
                    assertEquals(400, bindEmail(client, port, mapper, token, emailB, WRONG_CODE).statusCode());
                }
                assertFalse(Boolean.TRUE.equals(redis.hasKey(RedisCacheKey.BIND_EMAIL_CODE + emailB)),
                        "达到预算后验证码应被原子失效");
                assertEquals(400, bindEmail(client, port, mapper, token, emailB, codeB).statusCode(),
                        "耗尽后即使正确验证码也应被拒绝");

                String emailC = uniqueEmail();
                String emailD = uniqueEmail();
                String codeC = "864209";
                seedBindingCode(codes, secrets, emailC, user.id(), codeC);
                assertEquals(400, bindEmail(client, port, mapper, token, emailD, codeC).statusCode(),
                        "验证码按地址绑定，不得用于其他地址");
                assertEquals(200, bindEmail(client, port, mapper, token, emailC, codeC).statusCode(),
                        "其他地址的失败尝试不应消耗本地址预算");
            } finally {
                fixture.clean();
                if (secretVersionId != null) {
                    jdbc.update("DELETE FROM spectra_security.sec_secret_version WHERE id = ?", secretVersionId);
                }
            }
        }
    }

    private static String randomSecret() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    private static void seedBindingCode(SecurityVerificationCodeStore codes, SecretRuntimeService secrets,
                                        String email, UUID userId, String code) {
        String hmac = secrets.requireActiveValue("security.verification-code-hmac");
        codes.save(RedisCacheKey.BIND_EMAIL_CODE + email,
                userId + ":" + VerificationCodeDigest.digest(code, hmac), Duration.ofMinutes(5));
    }

    private static HttpResponse<String> bindEmail(HttpClient client, int port, ObjectMapper mapper, String token,
                                                  String email, String code)
            throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port
                + "/api/security/identities/email"))
                .header("Api-Version", "1.0.0")
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(Map.of(
                        "email", email, "code", code))))
                .build(), HttpResponse.BodyHandlers.ofString());
    }

    private static String login(HttpClient client, int port, ObjectMapper mapper, String username, String password)
            throws Exception {
        var response = client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port
                + "/api/security/authentication/login"))
                .header("Api-Version", "1.0.0")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(Map.of(
                        "type", "PASSWORD", "username", username, "password", password))))
                .build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        String token = mapper.readTree(response.body()).path("data").path("access_token").asText();
        return token;
    }

    private static String uniqueEmail() {
        return "b02bind" + UUID.randomUUID().toString().replace("-", "") + "@example.test";
    }

    @SuppressWarnings("unchecked")
    private static RedisTemplate<String, Object> securityRedisTemplate(ApplicationContext application) {
        return (RedisTemplate<String, Object>) application.getBean("securityRedisTemplate");
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

    private record TestUser(UUID id, String username, String password) {
        private static TestUser create() {
            return new TestUser(UUID.randomUUID(),
                    "b02bind" + UUID.randomUUID().toString().replace("-", ""),
                    "Bd!" + UUID.randomUUID());
        }
    }

    private static final class Fixture {
        private final JdbcTemplate jdbc;
        private final PasswordEncoder encoder;
        private final UUID operatorRole;
        private final List<TestUser> users = new java.util.ArrayList<>();

        private Fixture(ApplicationContext application) {
            this.jdbc = new JdbcTemplate(application.getBean(DataSource.class));
            this.encoder = application.getBean(PasswordEncoder.class);
            this.operatorRole = jdbc.queryForObject("SELECT id FROM spectra_security.sec_role WHERE code = 'ROLE_DEV_OPS'",
                    UUID.class);
        }

        private void insert(TestUser user) {
            jdbc.update("INSERT INTO spectra_core.sys_user (id, username, employee_no, real_name) "
                    + "VALUES (?, ?, ?, ?)", user.id(), user.username(), user.username(), "synthetic binding user");
            jdbc.update("INSERT INTO spectra_security.sec_authentication_identity "
                    + "(user_id, method_code, identifier_hash) VALUES (?, 'PASSWORD', ?)",
                    user.id(), AuthenticationIdentifierHash.digest(user.username()));
            jdbc.update("INSERT INTO spectra_security.sec_password_credential "
                    + "(user_id, password_hash, changed_at) VALUES (?, ?, CURRENT_TIMESTAMP)",
                    user.id(), encoder.encode(user.password()));
            jdbc.update("INSERT INTO spectra_security.sec_role_assignment (id, user_id, role_id) VALUES (?, ?, ?)",
                    UUID.randomUUID(), user.id(), operatorRole);
            users.add(user);
        }

        private void clean() {
            for (TestUser user : users) {
                jdbc.update("DELETE FROM spectra_security.sec_user_contact WHERE user_id = ?", user.id());
                jdbc.update("DELETE FROM spectra_security.sec_assignment_permission_boundary WHERE assignment_id IN "
                        + "(SELECT id FROM spectra_security.sec_role_assignment WHERE user_id = ?)", user.id());
                jdbc.update("DELETE FROM spectra_security.sec_assignment_grant_boundary WHERE assignment_id IN "
                        + "(SELECT id FROM spectra_security.sec_role_assignment WHERE user_id = ?)", user.id());
                jdbc.update("DELETE FROM spectra_security.sec_role_assignment WHERE user_id = ?", user.id());
                jdbc.update("DELETE FROM spectra_security.sec_password_credential WHERE user_id = ?", user.id());
                jdbc.update("DELETE FROM spectra_security.sec_authentication_identity WHERE user_id = ?", user.id());
                jdbc.update("DELETE FROM spectra_core.sys_user WHERE id = ?", user.id());
            }
        }
    }
}
