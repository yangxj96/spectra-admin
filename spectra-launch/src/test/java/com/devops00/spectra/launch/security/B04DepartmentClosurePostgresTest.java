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

import com.devops00.spectra.core.system.mapper.DepartmentClosureMapper;
import com.devops00.spectra.launch.LaunchApplication;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 在隔离 PostgreSQL 上验证闭包重建只包含活动部门，并在事务回滚后恢复原闭包。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/28
 */
@EnabledIfSystemProperty(named = "spectra.test.real-deps", matches = "true")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class B04DepartmentClosurePostgresTest {

    private ConfigurableApplicationContext application;
    private JdbcTemplate jdbc;
    private DepartmentClosureMapper closureMapper;
    private TransactionTemplate transactions;

    @BeforeAll
    void startApplication() {
        requireIsolatedDependencies();
        application = new SpringApplicationBuilder(LaunchApplication.class)
                .run("--server.port=0", "--server.address=127.0.0.1", "--server.ssl.enabled=false",
                        "--spectra.scheduler.quartz.enabled=false", "--spring.quartz.auto-startup=false",
                        "--flowable.async-executor-activate=false");
        jdbc = new JdbcTemplate(application.getBean(DataSource.class));
        closureMapper = application.getBean(DepartmentClosureMapper.class);
        transactions = new TransactionTemplate(application.getBean(PlatformTransactionManager.class));
    }

    @AfterAll
    void stopApplication() {
        if (application != null) {
            application.close();
        }
    }

    @Test
    void rebuildUsesActiveDepartmentsOnlyAndRollsBack() {
        var root = UUID.randomUUID();
        var deleted = UUID.randomUUID();
        var activeChild = UUID.randomUUID();
        var activeGrandchild = UUID.randomUUID();
        var before = closureCount();

        transactions.executeWithoutResult(status -> {
            closureMapper.clearClosure();
            insertDepartment(root, null, false);
            insertDepartment(deleted, root, true);
            insertDepartment(activeChild, root, false);
            insertDepartment(activeGrandchild, activeChild, false);

            closureMapper.rebuildClosure();

            assertEquals(6, closureCount(root, deleted, activeChild, activeGrandchild));
            assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM spectra_core.sys_department_closure "
                    + "WHERE ancestor_id = ? OR descendant_id = ?", Integer.class, deleted, deleted));
            assertEquals(1, pairCount(root, activeGrandchild, 2));
            status.setRollbackOnly();
        });

        assertEquals(before, closureCount());
    }

    private void insertDepartment(UUID id, UUID pid, boolean deleted) {
        jdbc.update("INSERT INTO spectra_core.sys_department "
                + "(id, pid, name, code, created_at, updated_at, deleted) "
                + "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, "
                + (deleted ? "CURRENT_TIMESTAMP" : "NULL") + ")",
                id, pid, "b04-" + id, "B04" + id.toString().replace("-", ""));
    }

    private int closureCount() {
        return jdbc.queryForObject("SELECT count(*) FROM spectra_core.sys_department_closure", Integer.class);
    }

    private int closureCount(UUID... ids) {
        var placeholders = String.join(",", java.util.Collections.nCopies(ids.length, "?"));
        var sql = "SELECT count(*) FROM spectra_core.sys_department_closure "
                + "WHERE ancestor_id IN (" + placeholders + ") OR descendant_id IN (" + placeholders + ")";
        var arguments = new Object[ids.length * 2];
        for (int index = 0; index < ids.length; index++) {
            arguments[index] = ids[index];
            arguments[index + ids.length] = ids[index];
        }
        return jdbc.queryForObject(sql, Integer.class, arguments);
    }

    private Integer pairCount(UUID ancestor, UUID descendant, int depth) {
        return jdbc.queryForObject("SELECT count(*) FROM spectra_core.sys_department_closure "
                + "WHERE ancestor_id = ? AND descendant_id = ? AND depth = ?", Integer.class,
                ancestor, descendant, depth);
    }

    private static void requireIsolatedDependencies() {
        var url = System.getenv("DB_URL");
        if (url == null || !url.matches("^jdbc:postgresql://[^/]+/devops00_spectra_db_test(?:\\?.*)?$")) {
            throw new IllegalStateException("真实依赖测试只允许 devops00_spectra_db_test");
        }
        if (!"5".equals(System.getenv("REDIS_DB"))) {
            throw new IllegalStateException("真实依赖测试只允许 Redis DB 5");
        }
    }
}
