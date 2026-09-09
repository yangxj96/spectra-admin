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

package com.devops00.spectra.core.scheduler.quartz.configuration;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Quartz 调度配置的安全默认值和非法值契约。 */
class QuartzSchedulerPropertiesTest {

    @Test
    void defaultConfigurationMustBeValid() {
        assertThatCode(() -> new QuartzSchedulerProperties().validateConfiguration())
                .doesNotThrowAnyException();
    }

    @Test
    void tablePrefixMustRemainInTheQuartzSchema() {
        var properties = new QuartzSchedulerProperties();
        properties.setTablePrefix("public.QRTZ_");

        assertThatThrownBy(properties::validateConfiguration)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("spectra_quartz");
    }

    @Test
    void clusterAndJdbcStoreCannotBeDisabled() {
        var properties = new QuartzSchedulerProperties();
        properties.setClustered(false);
        assertThatThrownBy(properties::validateConfiguration)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("集群");

        properties = new QuartzSchedulerProperties();
        properties.setJdbcStore(false);
        assertThatThrownBy(properties::validateConfiguration)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("JDBC");
    }

    @Test
    void ramJobStoreAndNonPositiveRetentionAreRejected() {
        var properties = new QuartzSchedulerProperties();
        properties.setJobStoreClass("org.quartz.simpl.RAMJobStore");
        assertThatThrownBy(properties::validateConfiguration)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("RAMJobStore");

        properties = new QuartzSchedulerProperties();
        properties.setHistoryRetentionDays(0);
        assertThatThrownBy(properties::validateConfiguration)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("保留");
    }

    @Test
    void runningTimeoutMustBeLongerThanRetryInterval() {
        var properties = new QuartzSchedulerProperties();
        properties.setDatabaseRetryInterval(Duration.ofMinutes(10));
        properties.setHistoryRunningTimeout(Duration.ofMinutes(10));

        assertThatThrownBy(properties::validateConfiguration)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("运行超时");
    }
}
