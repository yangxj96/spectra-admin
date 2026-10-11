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

package com.devops00.spectra.launch.scope;

import com.devops00.spectra.common.constant.ClientType;
import com.devops00.spectra.common.port.security.SecurityPrincipal;
import com.devops00.spectra.common.port.security.SecurityUserLoader;
import com.devops00.spectra.common.security.authorization.AuthorizationAssignment;
import com.devops00.spectra.common.security.authorization.AuthorizationScope;
import com.devops00.spectra.common.security.authorization.AuthorizationSnapshot;
import com.devops00.spectra.common.security.authorization.AuthorizationSnapshotProvider;
import com.devops00.spectra.common.security.authorization.PermissionBoundary;
import com.devops00.spectra.common.security.authorization.ScopeMode;
import com.devops00.spectra.core.security.authentication.javabean.entity.SecurityUser;
import com.devops00.spectra.framework.security.session.lifecycle.SecuritySessionIssuer;
import com.devops00.spectra.framework.security.session.lifecycle.SecuritySessionRevoker;
import com.devops00.spectra.launch.LaunchApplication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import tools.jackson.databind.ObjectMapper;

import javax.sql.DataSource;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 在正式 Flyway schema 和完整应用过滤器链上验证会议分页。 */
@EnabledIfSystemProperty(named = "spectra.test.real-deps", matches = "true")
class OaMeetingFullHttpFlywayTest {

    @Test
    void migratedSchemaAndHttpSecurityScopeRevocation() throws Exception {
        String url = System.getenv("DB_URL");
        if (url == null || !url.matches("^jdbc:postgresql://[^/]+/devops00_spectra_db_test(?:\\?.*)?$")) {
            throw new IllegalStateException("真实依赖测试只允许 devops00_spectra_db_test");
        }
        if (!"5".equals(System.getenv("REDIS_DB"))) {
            throw new IllegalStateException("真实依赖测试只允许 Redis DB 5");
        }
        try (var context = new SpringApplicationBuilder(LaunchApplication.class, SyntheticConfiguration.class)
                .run("--server.port=0", "--server.ssl.enabled=false", "--spring.quartz.auto-startup=false",
                        "--flowable.async-executor-activate=false", "--spectra.test.meeting-http=true")) {
            try (Connection connection = context.getBean(DataSource.class).getConnection()) {
                try (var statement = connection.createStatement()) {
                    try (var result = statement.executeQuery("SELECT count(*) FROM public.flyway_schema_history "
                            + "WHERE success = true")) {
                        assertTrue(result.next());
                        assertEquals(8L, result.getLong(1));
                    }
                }
            }
            int port = ((WebServerApplicationContext) context).getWebServer().getPort();
            var identity = context.getBean(SyntheticIdentity.class);
            var issuer = context.getBean(SecuritySessionIssuer.class);
            var revoker = context.getBean(SecuritySessionRevoker.class);
            var mapper = context.getBean(ObjectMapper.class);
            var source = context.getBean(DataSource.class);
            UUID owner = UUID.randomUUID();
            UUID visible = UUID.randomUUID();
            UUID hidden = UUID.randomUUID();
            try (HttpClient client = HttpClient.newHttpClient()) {
                assertEquals(401, send(client, port, null, "").statusCode());
                insertMeetings(source, identity.id, owner, visible, hidden);
                String token = issuer.createToken(identity.load(identity.id), ClientType.WEB).getAccessToken();
                try {
                    var permitted = send(client, port, token, "?pageNum=1&pageSize=1");
                    assertEquals(200, permitted.statusCode());
                    var page = mapper.readTree(permitted.body()).path("data");
                    assertEquals(1L, page.path("total").asLong());
                    assertEquals(visible.toString(), page.path("records").get(0).path("id").asString());
                    assertFalse(permitted.body().contains(hidden.toString()));

                    identity.snapshot.set(AuthorizationSnapshot.of(List.of()));
                    var restricted = send(client, port, token, "");
                    assertEquals(200, restricted.statusCode());
                    assertEquals(0L, mapper.readTree(restricted.body()).path("data").path("total").asLong());

                    identity.snapshot.set(selfScope());
                    identity.permission.set(false);
                    assertEquals(403, send(client, port, token, "").statusCode());
                    identity.permission.set(true);
                    assertEquals(400, send(client, port, token, "?orders%5B0%5D.column=id%3Bselect%201")
                            .statusCode());
                    assertEquals(200, send(client, port, token, "").statusCode());
                } finally {
                    revoker.deleteToken(token);
                }
                assertEquals(401, send(client, port, token, "").statusCode());
            } finally {
                deleteMeetings(source, visible, hidden);
            }
        }
    }

    private static HttpResponse<String> send(HttpClient client, int port, String token, String query) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/api/oa/meeting/page" + query))
                .header("Api-Version", "1.0.0")
                .GET();
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private static void insertMeetings(DataSource source, UUID subject, UUID owner, UUID visible, UUID hidden)
            throws SQLException {
        try (Connection connection = source.getConnection()) {
            execute(connection, "INSERT INTO spectra_oa.oa_meeting "
                    + "(id, title, initiator_id, start_time, end_time, created_by, created_at, updated_at) "
                    + "VALUES (?, ?, ?, now(), now(), ?, now(), now())",
                    visible, "synthetic visible", owner, owner);
            execute(connection, "INSERT INTO spectra_oa.oa_meeting "
                    + "(id, title, initiator_id, start_time, end_time, created_by, created_at, updated_at) "
                    + "VALUES (?, ?, ?, now(), now(), ?, now(), now())",
                    hidden, "synthetic hidden", owner, owner);
            execute(connection, "INSERT INTO spectra_oa.oa_meeting_participant "
                    + "(id, meeting_id, user_id, created_at, updated_at) VALUES (?, ?, ?, now(), now())",
                    UUID.randomUUID(), visible, subject);
        }
    }

    private static void deleteMeetings(DataSource source, UUID visible, UUID hidden) throws SQLException {
        try (Connection connection = source.getConnection()) {
            execute(connection, "DELETE FROM spectra_oa.oa_meeting_participant WHERE meeting_id IN (?, ?)",
                    visible, hidden);
            execute(connection, "DELETE FROM spectra_oa.oa_meeting WHERE id IN (?, ?)", visible, hidden);
        }
    }

    private static void execute(Connection connection, String sql, Object... values) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int index = 0; index < values.length; index++) {
                statement.setObject(index + 1, values[index]);
            }
            statement.executeUpdate();
        }
    }

    private static AuthorizationSnapshot selfScope() {
        var boundary = new PermissionBoundary("oa:meeting:read", new AuthorizationScope(ScopeMode.SELF, Set.of(), false));
        var assignment = new AuthorizationAssignment(UUID.randomUUID(), "TEST_SCOPE", 1,
                Map.of("oa:meeting:read", boundary), Map.of());
        return AuthorizationSnapshot.of(List.of(assignment));
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(name = "spectra.test.meeting-http", havingValue = "true")
    static class SyntheticConfiguration {
        @Bean
        @Primary
        SyntheticIdentity syntheticIdentity() {
            return new SyntheticIdentity();
        }

        @Bean
        @Primary
        AuthorizationSnapshotProvider syntheticSnapshotProvider(SyntheticIdentity identity) {
            return identity::snapshot;
        }
    }

    static class SyntheticIdentity implements SecurityUserLoader {
        private final UUID id = UUID.randomUUID();
        private final AtomicReference<AuthorizationSnapshot> snapshot = new AtomicReference<>(selfScope());
        private final AtomicBoolean permission = new AtomicBoolean(true);

        @Override
        public SecurityPrincipal load(UUID userId) {
            if (!id.equals(userId)) {
                return null;
            }
            var user = new SecurityUser();
            user.setId(id);
            user.setUsername("synthetic-meeting-user");
            user.setName("synthetic meeting user");
            user.setTimezone("UTC");
            user.setAuthorities(permission.get()
                    ? List.of(new SimpleGrantedAuthority("oa:meeting:read"))
                    : List.of());
            return user;
        }

        AuthorizationSnapshot snapshot(UUID userId) {
            return id.equals(userId) ? snapshot.get() : AuthorizationSnapshot.of(List.of());
        }
    }
}
