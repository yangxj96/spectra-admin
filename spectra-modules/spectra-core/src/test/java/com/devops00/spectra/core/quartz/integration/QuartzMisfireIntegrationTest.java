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

import com.devops00.spectra.common.port.quartz.QuartzTriggerTemplate;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.quartz.CronTrigger;
import org.quartz.SimpleTrigger;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

/** 在真实 Quartz 构造器上验证时区、错过策略和 Simple Trigger 语义。 */
@Testcontainers
@Tag("manual-integration")
@EnabledIfEnvironmentVariable(named = "SPECTRA_QUARTZ_FLYWAY_POSTGRES_TEST", matches = "true")
class QuartzMisfireIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:18-alpine")
            .withDatabaseName("spectra_quartz_misfire")
            .withUsername("quartz_test")
            .withPassword("quartz_test");

    @BeforeAll
    static void migrateSchema() {
        QuartzPostgresIntegrationSupport.migrate(POSTGRES);
    }

    @Test
    void cronMustKeepIanaTimezoneAndDoNothingMisfireInstruction() {
        var trigger = (CronTrigger) QuartzTriggerTemplate.cron("0 0 12 * * ?", ZoneId.of("Asia/Shanghai"),
                QuartzTriggerTemplate.MisfirePolicy.DO_NOTHING)
                .build(new org.quartz.TriggerKey("cron-trigger", "integration"),
                        new org.quartz.JobKey("cron-job", "integration"));

        assertThat(trigger.getTimeZone().toZoneId()).isEqualTo(ZoneId.of("Asia/Shanghai"));
        assertThat(trigger.getMisfireInstruction()).isEqualTo(CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING);
    }

    @Test
    void simpleTriggerMustDistinguishOneShotAndFixedInterval() {
        var jobKey = new org.quartz.JobKey("simple-job", "integration");
        var oneShot = (SimpleTrigger) QuartzTriggerTemplate.oneShot(
                QuartzTriggerTemplate.MisfirePolicy.FIRE_ONCE_NOW)
                .build(new org.quartz.TriggerKey("one-shot", "integration"), jobKey);
        var fixedInterval = (SimpleTrigger) QuartzTriggerTemplate.fixedInterval(Duration.ofSeconds(30),
                QuartzTriggerTemplate.MisfirePolicy.NEXT_WITH_REMAINING_COUNT)
                .build(new org.quartz.TriggerKey("fixed-interval", "integration"), jobKey);

        assertThat(oneShot.getRepeatCount()).isZero();
        assertThat(oneShot.getMisfireInstruction()).isEqualTo(SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
        assertThat(fixedInterval.getRepeatCount()).isEqualTo(SimpleTrigger.REPEAT_INDEFINITELY);
        assertThat(fixedInterval.getRepeatInterval()).isEqualTo(30_000L);
        assertThat(fixedInterval.getMisfireInstruction())
                .isEqualTo(SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT);
    }
}
