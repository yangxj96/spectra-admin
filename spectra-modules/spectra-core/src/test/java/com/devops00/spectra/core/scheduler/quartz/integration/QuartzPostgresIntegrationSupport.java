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

package com.devops00.spectra.core.scheduler.quartz.integration;

import org.flywaydb.core.Flyway;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** 为 Quartz PostgreSQL 集成测试提供隔离数据库迁移和 Scheduler 工厂。 */
final class QuartzPostgresIntegrationSupport {

    private QuartzPostgresIntegrationSupport() {
    }

    /** 使用当前完整 Flyway 链初始化隔离测试数据库。 */
    static void migrate(PostgreSQLContainer<?> postgres) {
        Flyway.configure()
                .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .locations("classpath:db/migration")
                .baselineOnMigrate(false)
                .validateOnMigrate(true)
                .cleanDisabled(true)
                .load()
                .migrate();
    }

    /** 创建连接到隔离 PostgreSQL 的 Quartz JDBC Cluster Scheduler。 */
    static Scheduler scheduler(PostgreSQLContainer<?> postgres, String instanceId) throws SchedulerException {
        var properties = new Properties();
        properties.setProperty("org.quartz.scheduler.instanceName", "spectraQuartzIntegration");
        properties.setProperty("org.quartz.scheduler.instanceId", instanceId);
        properties.setProperty("org.quartz.scheduler.skipUpdateCheck", "true");
        properties.setProperty("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
        properties.setProperty("org.quartz.threadPool.threadCount", "2");
        properties.setProperty("org.quartz.threadPool.threadPriority", "5");
        properties.setProperty("org.quartz.jobStore.class", "org.quartz.impl.jdbcjobstore.JobStoreTX");
        properties.setProperty("org.quartz.jobStore.driverDelegateClass",
                "org.quartz.impl.jdbcjobstore.PostgreSQLDelegate");
        properties.setProperty("org.quartz.jobStore.tablePrefix", "spectra_quartz.QRTZ_");
        properties.setProperty("org.quartz.jobStore.isClustered", "true");
        properties.setProperty("org.quartz.jobStore.clusterCheckinInterval", "1000");
        properties.setProperty("org.quartz.jobStore.misfireThreshold", "2000");
        properties.setProperty("org.quartz.jobStore.dataSource", "spectra");
        properties.setProperty("org.quartz.dataSource.spectra.driver", "org.postgresql.Driver");
        properties.setProperty("org.quartz.dataSource.spectra.URL", postgres.getJdbcUrl());
        properties.setProperty("org.quartz.dataSource.spectra.user", postgres.getUsername());
        properties.setProperty("org.quartz.dataSource.spectra.password", postgres.getPassword());
        properties.setProperty("org.quartz.dataSource.spectra.maxConnections", "5");
        properties.setProperty("org.quartz.dataSource.spectra.validationQuery", "SELECT 1");
        return new StdSchedulerFactory(properties).getScheduler();
    }

    /** 打开测试数据库连接。 */
    static Connection connection(PostgreSQLContainer<?> postgres) throws SQLException {
        return DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
    }
}
