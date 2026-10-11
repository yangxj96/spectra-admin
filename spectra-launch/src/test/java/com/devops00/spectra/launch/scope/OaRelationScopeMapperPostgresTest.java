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

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.devops00.spectra.common.security.authorization.AuthorizationAssignment;
import com.devops00.spectra.common.security.authorization.AuthorizationScope;
import com.devops00.spectra.common.security.authorization.AuthorizationSnapshot;
import com.devops00.spectra.common.security.authorization.AuthorizationSnapshotProvider;
import com.devops00.spectra.common.security.authorization.PermissionBoundary;
import com.devops00.spectra.common.security.authorization.ScopeMode;
import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import com.devops00.spectra.framework.persistence.scope.authorization.DataScopeInnerInterceptor;
import com.devops00.spectra.framework.persistence.scope.context.DataScopeEntityRegistry;
import com.devops00.spectra.oa.meeting.javabean.entity.Meeting;
import com.devops00.spectra.oa.meeting.mapper.MeetingMapper;
import com.devops00.spectra.oa.purchase.javabean.entity.PurchaseReceipt;
import com.devops00.spectra.oa.purchase.mapper.PurchaseReceiptMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** 在专用 PostgreSQL 库通过 OA 正式 Mapper 验证关系数据范围。 */
@EnabledIfSystemProperty(named = "spectra.test.real-deps", matches = "true")
class OaRelationScopeMapperPostgresTest {

    private static final String MEETING_PERMISSION = "oa:meeting:read";
    private static final String PURCHASE_PERMISSION = "oa:purchase:read";

    @Test
    void meetingMapperRestrictsRowsAndCountToParticipantOrOwner() throws Exception {
        try (Connection connection = dataSource().getConnection()) {
            connection.setAutoCommit(false);
            try {
                prepareTables(connection);
                UUID subject = UUID.randomUUID();
                UUID other = UUID.randomUUID();
                UUID participantMeeting = UUID.randomUUID();
                UUID hiddenMeeting = UUID.randomUUID();
                insertMeeting(connection, participantMeeting, other);
                insertMeeting(connection, hiddenMeeting, other);
                insertParticipant(connection, participantMeeting, subject);
                insertParticipant(connection, hiddenMeeting, other);

                var snapshot = new AtomicReference<>(snapshot(MEETING_PERMISSION,
                        new AuthorizationScope(ScopeMode.SELF, Set.of(), false), Set.of()));
                SqlSession session = openSession(connection, subject, snapshot);
                try {
                    MeetingMapper mapper = session.getMapper(MeetingMapper.class);
                    var filter = new QueryWrapper<Meeting>().in("id", List.of(participantMeeting, hiddenMeeting));
                    var visibleMeetings = mapper.selectList(filter);
                    assertEquals(1, visibleMeetings.size());
                    assertEquals("synthetic meeting", visibleMeetings.getFirst().getTitle());
                    assertEquals(participantMeeting, visibleMeetings.getFirst().getId());
                    assertEquals(1L, mapper.selectCount(new QueryWrapper<Meeting>()
                            .in("id", List.of(participantMeeting, hiddenMeeting))));

                    snapshot.set(AuthorizationSnapshot.of(List.of()));
                    assertTrue(mapper.selectList(filter).isEmpty());
                    assertEquals(0L, mapper.selectCount(new QueryWrapper<Meeting>()
                            .in("id", List.of(participantMeeting, hiddenMeeting))));
                } finally {
                    session.rollback(true);
                    session.close();
                }
            } finally {
                if (!connection.isClosed()) {
                    connection.rollback();
                }
            }
        }
    }

    @Test
    void purchaseReceiptMapperRestrictsRowsToAuthorizedPurchaseDepartment() throws Exception {
        try (Connection connection = dataSource().getConnection()) {
            connection.setAutoCommit(false);
            try {
                prepareTables(connection);
                UUID subject = UUID.randomUUID();
                UUID allowedDepartment = UUID.randomUUID();
                UUID deniedDepartment = UUID.randomUUID();
                UUID allowedPurchase = UUID.randomUUID();
                UUID deniedPurchase = UUID.randomUUID();
                UUID allowedReceipt = UUID.randomUUID();
                UUID deniedReceipt = UUID.randomUUID();
                insertPurchase(connection, allowedPurchase, allowedDepartment);
                insertPurchase(connection, deniedPurchase, deniedDepartment);
                insertReceipt(connection, allowedReceipt, allowedPurchase);
                insertReceipt(connection, deniedReceipt, deniedPurchase);

                var snapshot = new AtomicReference<>(snapshot(PURCHASE_PERMISSION,
                        new AuthorizationScope(ScopeMode.RULES, Set.of(allowedDepartment), false),
                        Set.of(allowedDepartment)));
                SqlSession session = openSession(connection, subject, snapshot);
                try {
                    PurchaseReceiptMapper mapper = session.getMapper(PurchaseReceiptMapper.class);
                    var filter = new QueryWrapper<PurchaseReceipt>().in("id", List.of(allowedReceipt, deniedReceipt));
                    var visibleReceipts = mapper.selectList(filter);
                    assertEquals(1, visibleReceipts.size());
                    assertEquals("synthetic-" + allowedReceipt, visibleReceipts.getFirst().getReceiptNo());
                    assertEquals(allowedReceipt, visibleReceipts.getFirst().getId());

                    snapshot.set(snapshot(PURCHASE_PERMISSION,
                            new AuthorizationScope(ScopeMode.RULES, Set.of(deniedDepartment), false),
                            Set.of(deniedDepartment)));
                    assertEquals(List.of(deniedReceipt), mapper.selectList(filter)
                            .stream()
                            .map(PurchaseReceipt::getId)
                            .toList());
                    snapshot.set(AuthorizationSnapshot.of(List.of()));
                    assertTrue(mapper.selectList(filter).isEmpty());
                } finally {
                    session.rollback(true);
                    session.close();
                }
            } finally {
                if (!connection.isClosed()) {
                    connection.rollback();
                }
            }
        }
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

    private static SqlSession openSession(Connection connection, UUID subject,
                                          AtomicReference<AuthorizationSnapshot> snapshot)
            throws Exception {
        var registry = new DataScopeEntityRegistry();
        registry.initialize();
        SecurityContextAccessor securityContext = mock(SecurityContextAccessor.class);
        when(securityContext.currentUserId()).thenReturn(subject);
        var beans = new StaticListableBeanFactory();
        beans.addBean("snapshotProvider", (AuthorizationSnapshotProvider) ignored -> snapshot.get());
        var interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new DataPermissionInterceptor(new DataScopeInnerInterceptor(
                beans.getBeanProvider(AuthorizationSnapshotProvider.class), registry, securityContext)));
        var factory = new MybatisSqlSessionFactoryBean();
        factory.setDataSource(dataSource());
        factory.setTypeHandlersPackage("com.devops00.spectra.framework.persistence.mybatis");
        factory.setTransactionFactory(new JdbcTransactionFactory());
        factory.setPlugins(interceptor);
        factory.afterPropertiesSet();
        var sqlSessionFactory = factory.getObject();
        sqlSessionFactory.getConfiguration().addMapper(MeetingMapper.class);
        sqlSessionFactory.getConfiguration().addMapper(PurchaseReceiptMapper.class);
        return sqlSessionFactory.openSession(connection);
    }

    private static AuthorizationSnapshot snapshot(String permission, AuthorizationScope scope, Set<UUID> memberships) {
        var boundary = new PermissionBoundary(permission, scope);
        var assignment = new AuthorizationAssignment(UUID.randomUUID(), "TEST_SCOPE", 1,
                Map.of(permission, boundary), Map.of());
        return AuthorizationSnapshot.of(List.of(assignment), memberships);
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
            statement.execute("CREATE TABLE IF NOT EXISTS spectra_oa.oa_purchase (id uuid PRIMARY KEY, "
                    + "application_id uuid NOT NULL, department_id uuid, purpose text NOT NULL, "
                    + "expected_date timestamptz NOT NULL, budget_amount numeric NOT NULL, "
                    + "created_at timestamptz NOT NULL, updated_at timestamptz NOT NULL)");
            statement.execute("CREATE TABLE IF NOT EXISTS spectra_oa.oa_purchase_receipt (id uuid PRIMARY KEY, "
                    + "purchase_id uuid NOT NULL, receipt_no text NOT NULL, received_date timestamptz NOT NULL, "
                    + "receiver_id uuid, status text DEFAULT 'PARTIAL', remark text, created_by uuid, "
                    + "created_at timestamptz NOT NULL, updated_by uuid, updated_at timestamptz NOT NULL, "
                    + "deleted timestamptz, version bigint DEFAULT 0)");
        }
    }

    private static void insertMeeting(Connection connection, UUID id, UUID creator) throws SQLException {
        execute(connection, "INSERT INTO spectra_oa.oa_meeting "
                + "(id, title, initiator_id, start_time, end_time, created_by, created_at, updated_at) "
                + "VALUES (?, 'synthetic meeting', ?, now(), now(), ?, now(), now())", id, creator, creator);
    }

    private static void insertParticipant(Connection connection, UUID meetingId, UUID userId) throws SQLException {
        execute(connection, "INSERT INTO spectra_oa.oa_meeting_participant "
                + "(id, meeting_id, user_id, created_at, updated_at) VALUES (?, ?, ?, now(), now())",
                UUID.randomUUID(), meetingId, userId);
    }

    private static void insertPurchase(Connection connection, UUID id, UUID department) throws SQLException {
        execute(connection, "INSERT INTO spectra_oa.oa_purchase "
                + "(id, application_id, department_id, purpose, expected_date, budget_amount, created_at, updated_at) "
                + "VALUES (?, ?, ?, 'synthetic purchase', now(), 1, now(), now())",
                id, UUID.randomUUID(), department);
    }

    private static void insertReceipt(Connection connection, UUID id, UUID purchaseId) throws SQLException {
        execute(connection, "INSERT INTO spectra_oa.oa_purchase_receipt "
                + "(id, purchase_id, receipt_no, received_date, created_at, updated_at) "
                + "VALUES (?, ?, ?, now(), now(), now())", id, purchaseId, "synthetic-" + id);
    }

    private static void execute(Connection connection, String sql, Object... values) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int index = 0; index < values.length; index++) {
                statement.setObject(index + 1, values[index]);
            }
            statement.executeUpdate();
        }
    }
}
