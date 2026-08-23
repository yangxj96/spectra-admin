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

package com.devops00.spectra.core.system.javabean.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

/**
 * 单体服务监控历史采样。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/08/23
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_service_monitor_sample", schema = "spectra_core")
public class ServiceMonitorSample extends BaseEntity {

    /** 采集时间。 */
    @TableField("collected_at")
    private Instant collectedAt;

    /** CPU 使用率。 */
    @TableField("cpu_usage")
    private double cpuUsage;

    /** CPU 逻辑核心数。 */
    @TableField("cpu_logical_cores")
    private int cpuLogicalCores;

    /** 系统内存使用率。 */
    @TableField("system_memory_usage")
    private double systemMemoryUsage;

    /** 系统内存总量。 */
    @TableField("system_memory_total_bytes")
    private long systemMemoryTotalBytes;

    /** 系统内存已使用量。 */
    @TableField("system_memory_used_bytes")
    private long systemMemoryUsedBytes;

    /** 系统内存可用量。 */
    @TableField("system_memory_available_bytes")
    private long systemMemoryAvailableBytes;

    /** JVM 堆内存使用率。 */
    @TableField("jvm_heap_usage")
    private double jvmHeapUsage;

    /** JVM 堆内存已使用量。 */
    @TableField("jvm_heap_used_bytes")
    private long jvmHeapUsedBytes;

    /** JVM 堆内存最大量。 */
    @TableField("jvm_heap_max_bytes")
    private long jvmHeapMaxBytes;

    /** JVM 非堆内存已使用量。 */
    @TableField("jvm_non_heap_used_bytes")
    private long jvmNonHeapUsedBytes;

    /** 当前活动线程数。 */
    @TableField("live_thread_count")
    private int liveThreadCount;

    /** 峰值线程数。 */
    @TableField("peak_thread_count")
    private int peakThreadCount;

    /** GC 次数。 */
    @TableField("gc_count")
    private long gcCount;

    /** 每秒请求数。 */
    @TableField("qps")
    private double qps;

    /** 错误率。 */
    @TableField("error_rate")
    private double errorRate;

    /** P95 响应时间。 */
    @TableField("p95_response_ms")
    private double p95ResponseMs;

    /** 是否采集到 HTTP 请求指标。 */
    @TableField("request_metrics_available")
    private boolean requestMetricsAvailable;

    /** 采样时的服务状态。 */
    private String status;
}
