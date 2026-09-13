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

package com.devops00.spectra.core.quartz.integration;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.TriggerBuilder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 在两个 Quartz 节点共享同一数据库时验证单次领取和非并发语义。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@Testcontainers
@Tag("manual-integration")
@EnabledIfEnvironmentVariable(named = "SPECTRA_QUARTZ_FLYWAY_POSTGRES_TEST", matches = "true")
class QuartzClusterExecutionIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:18-alpine")
            .withDatabaseName("spectra_quartz_cluster")
            .withUsername("quartz_test")
            .withPassword("quartz_test");

    private static final AtomicInteger EXECUTIONS = new AtomicInteger();
    private static CountDownLatch executionLatch;

    @BeforeAll
    static void migrateSchema() {
        QuartzPostgresIntegrationSupport.migrate(POSTGRES);
    }

    @BeforeEach
    void resetProbe() {
        EXECUTIONS.set(0);
        executionLatch = new CountDownLatch(1);
    }

    @Test
    void clusteredNodesMustClaimOneDueNonConcurrentFire() throws Exception {
        Scheduler first = QuartzPostgresIntegrationSupport.scheduler(POSTGRES, "cluster-node-1");
        Scheduler second = QuartzPostgresIntegrationSupport.scheduler(POSTGRES, "cluster-node-2");
        try {
            var jobKey = new org.quartz.JobKey("cluster-probe", "integration");
            var triggerKey = new org.quartz.TriggerKey("cluster-probe-trigger", "integration");
            var detail = JobBuilder.newJob(ClusterProbeJob.class).withIdentity(jobKey).build();
            var trigger = TriggerBuilder.newTrigger()
                    .withIdentity(triggerKey)
                    .forJob(jobKey)
                    .startAt(Date.from(Instant.now().plusSeconds(2)))
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule().withRepeatCount(0))
                    .build();
            first.scheduleJob(detail, trigger);
            first.start();
            second.start();

            assertThat(executionLatch.await(10, TimeUnit.SECONDS)).isTrue();
            Thread.sleep(1000L);
            assertThat(EXECUTIONS).hasValue(1);
            assertThat(first.getMetaData().isJobStoreClustered()).isTrue();
            assertThat(second.getMetaData().isJobStoreClustered()).isTrue();
        } finally {
            first.shutdown(true);
            second.shutdown(true);
        }
    }

    /**
     * 用于确认集群同一 Trigger 只被领取一次的非并发 Job。
     *
     * @author yangxj96
     * @version 1.0
     * @since 2026/09/13
     */
    @DisallowConcurrentExecution
    public static class ClusterProbeJob implements Job {

        @Override
        public void execute(JobExecutionContext context) {
            EXECUTIONS.incrementAndGet();
            executionLatch.countDown();
        }
    }
}
