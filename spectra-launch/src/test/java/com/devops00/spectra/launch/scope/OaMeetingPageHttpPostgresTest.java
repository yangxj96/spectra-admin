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

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.devops00.spectra.common.notification.NotificationService;
import com.devops00.spectra.common.port.directory.DirectoryQueryPort;
import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import com.devops00.spectra.common.security.authorization.AuthorizationAssignment;
import com.devops00.spectra.common.security.authorization.AuthorizationScope;
import com.devops00.spectra.common.security.authorization.AuthorizationSnapshot;
import com.devops00.spectra.common.security.authorization.AuthorizationSnapshotProvider;
import com.devops00.spectra.common.security.authorization.PermissionBoundary;
import com.devops00.spectra.common.security.authorization.ScopeMode;
import com.devops00.spectra.framework.persistence.scope.authorization.DataScopeInnerInterceptor;
import com.devops00.spectra.framework.persistence.scope.context.DataScopeEntityRegistry;
import com.devops00.spectra.framework.serialization.mapper.TimeMapper;
import com.devops00.spectra.framework.web.advice.exception.CommonExceptionAdvice;
import com.devops00.spectra.oa.meeting.controller.MeetingController;
import com.devops00.spectra.oa.meeting.javabean.converter.MeetingConverterImpl;
import com.devops00.spectra.oa.meeting.mapper.MeetingMapper;
import com.devops00.spectra.oa.meeting.mapper.MeetingParticipantMapper;
import com.devops00.spectra.oa.meeting.mapper.MeetingRecordMapper;
import com.devops00.spectra.oa.meeting.service.impl.MeetingServiceImpl;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.accept.DefaultApiVersionStrategy;
import org.springframework.web.accept.SemanticApiVersionParser;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 在专用 PostgreSQL 库验证正式会议分页 Service 与 MVC 参数绑定。 */
@EnabledIfSystemProperty(named = "spectra.test.real-deps", matches = "true")
class OaMeetingPageHttpPostgresTest {

    @Test
    void pageCountAndRecordsRespectParticipantScopeAndRevocation() throws Exception {
        inFixture(fixture -> {
            fixture.mvc.perform(get("/oa/meeting/page").param("pageNum", "1").param("pageSize", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.total").value(1))
                    .andExpect(jsonPath("$.records[0].id").value(fixture.visible.toString()))
                    .andExpect(jsonPath("$.records[0].title").value("synthetic visible"))
                    .andExpect(content().string(not(containsString(fixture.hidden.toString()))));

            fixture.snapshot.set(AuthorizationSnapshot.of(List.of()));
            fixture.mvc.perform(get("/oa/meeting/page"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.total").value(0))
                    .andExpect(jsonPath("$.records").isEmpty());
        });
    }

    @Test
    void unsafeSortIsRejectedAndSafeRequestStillWorks() throws Exception {
        inFixture(fixture -> {
            fixture.mvc.perform(get("/oa/meeting/page").param("orders[0].column", "id;select 1"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("不支持的分页排序字段")))
                    .andExpect(content().string(not(containsString("id;select 1"))));
            fixture.mvc.perform(get("/oa/meeting/page")
                    .param("orders[0].column", "start_time")
                    .param("orders[0].asc", "false"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.total").value(1));
        });
    }

    private static void inFixture(FixtureAssertion assertion) throws Exception {
        try (Connection connection = dataSource().getConnection()) {
            connection.setAutoCommit(false);
            try {
                prepareTables(connection);
                UUID subject = UUID.randomUUID();
                UUID owner = UUID.randomUUID();
                UUID visible = UUID.randomUUID();
                UUID hidden = UUID.randomUUID();
                insertMeeting(connection, visible, owner, "synthetic visible");
                insertMeeting(connection, hidden, owner, "synthetic hidden");
                insertParticipant(connection, visible, subject);
                insertParticipant(connection, hidden, owner);
                var snapshot = new AtomicReference<>(snapshot());
                SecurityContextAccessor securityContext = mock(SecurityContextAccessor.class);
                when(securityContext.currentUserId()).thenReturn(subject);
                when(securityContext.currentUserZoneId()).thenReturn("UTC");
                try (SqlSession session = openSession(connection, securityContext, snapshot)) {
                    var timeMapper = new TimeMapper(securityContext);
                    var service = new MeetingServiceImpl(new MeetingConverterImpl(timeMapper),
                            mock(MeetingParticipantMapper.class), mock(MeetingRecordMapper.class),
                            mock(NotificationService.class), mock(DirectoryQueryPort.class),
                            timeMapper, securityContext);
                    ReflectionTestUtils.setField(service, "baseMapper", session.getMapper(MeetingMapper.class));
                    MockMvc mvc = MockMvcBuilders.standaloneSetup(new MeetingController(service))
                            .setControllerAdvice(new CommonExceptionAdvice())
                            .setApiVersionStrategy(new DefaultApiVersionStrategy(
                                    List.of(request -> request.getHeader("Api-Version")),
                                    new SemanticApiVersionParser(), false, "1.0.0", true, null, null))
                            .build();
                    assertion.check(new Fixture(mvc, snapshot, visible, hidden));
                    session.rollback(true);
                }
            } finally {
                if (!connection.isClosed()) {
                    connection.rollback();
                }
            }
        }
    }

    private static SqlSession openSession(Connection connection, SecurityContextAccessor securityContext,
                                          AtomicReference<AuthorizationSnapshot> snapshot)
            throws Exception {
        var registry = new DataScopeEntityRegistry();
        registry.initialize();
        var beans = new StaticListableBeanFactory();
        beans.addBean("snapshotProvider", (AuthorizationSnapshotProvider) ignored -> snapshot.get());
        var interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new DataPermissionInterceptor(new DataScopeInnerInterceptor(
                beans.getBeanProvider(AuthorizationSnapshotProvider.class), registry, securityContext)));
        var pagination = new PaginationInnerInterceptor();
        pagination.setDbType(DbType.POSTGRE_SQL);
        pagination.setOverflow(true);
        pagination.setMaxLimit(500L);
        interceptor.addInnerInterceptor(pagination);
        var factory = new MybatisSqlSessionFactoryBean();
        factory.setDataSource(dataSource());
        factory.setTypeHandlersPackage("com.devops00.spectra.framework.persistence.mybatis");
        factory.setTransactionFactory(new JdbcTransactionFactory());
        factory.setPlugins(interceptor);
        factory.afterPropertiesSet();
        var sqlSessionFactory = factory.getObject();
        sqlSessionFactory.getConfiguration().addMapper(MeetingMapper.class);
        return sqlSessionFactory.openSession(connection);
    }

    private static DriverManagerDataSource dataSource() {
        String url = System.getenv("DB_URL");
        if (url == null || !url.matches("^jdbc:postgresql://[^/]+/devops00_spectra_db_test(?:\\?.*)?$")) {
            throw new IllegalStateException("真实依赖测试只允许 devops00_spectra_db_test");
        }
        String username = System.getenv("DB_USERNAME");
        if (username == null || username.isBlank()) {
            throw new IllegalStateException("真实依赖测试缺少 DB_USERNAME");
        }
        return new DriverManagerDataSource(url, username, System.getenv("DB_PASSWORD"));
    }

    private static AuthorizationSnapshot snapshot() {
        var boundary = new PermissionBoundary("oa:meeting:read", new AuthorizationScope(ScopeMode.SELF, Set.of(), false));
        var assignment = new AuthorizationAssignment(UUID.randomUUID(), "TEST_SCOPE", 1,
                Map.of("oa:meeting:read", boundary), Map.of());
        return AuthorizationSnapshot.of(List.of(assignment), Set.of());
    }

    private static void prepareTables(Connection connection) throws SQLException {
        try (var statement = connection.createStatement()) {
            statement.execute("CREATE SCHEMA IF NOT EXISTS spectra_oa");
            statement.execute("CREATE TABLE IF NOT EXISTS spectra_oa.oa_meeting (id uuid PRIMARY KEY, "
                    + "title text NOT NULL, initiator_id uuid NOT NULL, start_time timestamptz NOT NULL, "
                    + "end_time timestamptz NOT NULL, location text, content text, status text DEFAULT 'draft', "
                    + "process_instance_id text, approval_status text DEFAULT 'draft', department_id uuid, "
                    + "created_by uuid, created_at timestamptz NOT NULL, updated_by uuid, "
                    + "updated_at timestamptz NOT NULL, deleted timestamptz, version bigint DEFAULT 0)");
            statement.execute("CREATE TABLE IF NOT EXISTS spectra_oa.oa_meeting_participant (id uuid PRIMARY KEY, "
                    + "meeting_id uuid NOT NULL, user_id uuid NOT NULL, created_at timestamptz NOT NULL, "
                    + "updated_at timestamptz NOT NULL)");
        }
    }

    private static void insertMeeting(Connection connection, UUID id, UUID creator, String title) throws SQLException {
        execute(connection, "INSERT INTO spectra_oa.oa_meeting "
                + "(id, title, initiator_id, start_time, end_time, created_by, created_at, updated_at) "
                + "VALUES (?, ?, ?, now(), now(), ?, now(), now())", id, title, creator, creator);
    }

    private static void insertParticipant(Connection connection, UUID meetingId, UUID userId) throws SQLException {
        execute(connection, "INSERT INTO spectra_oa.oa_meeting_participant "
                + "(id, meeting_id, user_id, created_at, updated_at) VALUES (?, ?, ?, now(), now())",
                UUID.randomUUID(), meetingId, userId);
    }

    private static void execute(Connection connection, String sql, Object... values) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int index = 0; index < values.length; index++) {
                statement.setObject(index + 1, values[index]);
            }
            statement.executeUpdate();
        }
    }

    private record Fixture(MockMvc mvc, AtomicReference<AuthorizationSnapshot> snapshot, UUID visible, UUID hidden) {
    }

    @FunctionalInterface
    private interface FixtureAssertion {
        void check(Fixture fixture) throws Exception;
    }
}
