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

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/** Quartz JDBC Cluster 的应用级安全配置。 */
@Data
@ConfigurationProperties(prefix = "spectra.scheduler.quartz")
public class QuartzSchedulerProperties {

    /** 是否启用 Quartz 生命周期管理。 */
    private boolean enabled = true;

    /** Quartz JDBC 表的完整前缀，必须固定在独立 schema。 */
    private String tablePrefix = "spectra_quartz.QRTZ_";

    /** 是否启用 Quartz 集群协调。 */
    private boolean clustered = true;

    /** 是否使用 JDBC JobStore。 */
    private boolean jdbcStore = true;

    /** 允许的 JobStore 实现类名。 */
    private String jobStoreClass = "org.springframework.scheduling.quartz.LocalDataSourceJobStore";

    /** Quartz 工作线程数。 */
    private int threadCount = 10;

    /** 执行历史保留天数。 */
    private int historyRetentionDays = 90;

    /** 单次执行历史清理的最大行数。 */
    private int historyCleanupBatchSize = 500;

    /** RUNNING 执行被认定为节点中断的最长时间。 */
    private Duration historyRunningTimeout = Duration.ofHours(24);

    /** 数据库不可用时生命周期重试间隔。 */
    private Duration databaseRetryInterval = Duration.ofSeconds(10);

    /** 绑定完成后校验不可安全覆盖的 Quartz 设置。 */
    @PostConstruct
    public void validateConfiguration() {
        if (tablePrefix == null || !tablePrefix.startsWith("spectra_quartz.QRTZ_")) {
            throw new IllegalArgumentException("Quartz 表前缀必须使用 spectra_quartz.QRTZ_ 开头");
        }
        if (!clustered) {
            throw new IllegalArgumentException("Quartz 必须启用集群模式");
        }
        if (!jdbcStore) {
            throw new IllegalArgumentException("Quartz 必须使用 JDBC JobStore");
        }
        if (jobStoreClass == null
                || jobStoreClass.isBlank()
                || jobStoreClass.contains("RAMJobStore")) {
            throw new IllegalArgumentException("Quartz 禁止使用内存 RAMJobStore");
        }
        if (threadCount <= 0) {
            throw new IllegalArgumentException("Quartz 工作线程数必须大于 0");
        }
        if (historyRetentionDays <= 0) {
            throw new IllegalArgumentException("执行历史保留天数必须大于 0");
        }
        if (historyCleanupBatchSize <= 0) {
            throw new IllegalArgumentException("执行历史清理批次必须大于 0");
        }
        if (databaseRetryInterval == null
                || databaseRetryInterval.isZero()
                || databaseRetryInterval.isNegative()) {
            throw new IllegalArgumentException("Quartz 数据库重试间隔必须大于 0");
        }
        if (historyRunningTimeout == null
                || historyRunningTimeout.isZero()
                || historyRunningTimeout.isNegative()
                || historyRunningTimeout.minus(databaseRetryInterval).isZero()
                || historyRunningTimeout.minus(databaseRetryInterval).isNegative()) {
            throw new IllegalArgumentException("执行历史运行超时必须大于数据库重试间隔");
        }
    }
}
