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

package com.devops00.spectra.core.scheduler.service;

import com.devops00.spectra.core.scheduler.javabean.entity.SchedulerLoopErrorEntity;
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerLoopErrorStatus;
import com.devops00.spectra.core.scheduler.mapper.SchedulerLoopErrorMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** 高频循环错误聚合器；重复错误不逐条输出普通日志。 */
@Slf4j
@Service
public class LoopErrorAggregator {

    private static final Duration DEFAULT_LOG_INTERVAL = Duration.ofMinutes(1);
    private final SchedulerLoopErrorMapper errorMapper;
    private final Clock clock;

    @Autowired
    public LoopErrorAggregator(SchedulerLoopErrorMapper errorMapper) {
        this(errorMapper, Clock.systemUTC());
    }

    LoopErrorAggregator(SchedulerLoopErrorMapper errorMapper, Clock clock) {
        this.errorMapper = errorMapper;
        this.clock = clock;
    }

    /** 记录一次错误，返回是否应输出本次聚合日志。 */
    public LoopErrorOccurrence record(UUID jobId,
                                      UUID runtimeId,
                                      String instanceId,
                                      String errorCode,
                                      String errorMessage,
                                      Map<String, Object> context,
                                      Duration logInterval,
                                      Instant seenAt) {
        if (jobId == null
                || runtimeId == null
                || instanceId == null
                || instanceId.isBlank()
                || errorCode == null
                || errorCode.isBlank()) {
            throw new IllegalArgumentException("循环错误缺少任务、会话、实例或错误码");
        }
        var now = seenAt == null ? clock.instant() : seenAt;
        var sanitizedCode = sanitizeCode(errorCode);
        var sanitizedMessage = sanitizeMessage(errorMessage);
        var fingerprint = fingerprint(sanitizedCode, sanitizedMessage);
        var error = new SchedulerLoopErrorEntity();
        error.setId(UUID.randomUUID());
        error.setJobId(jobId);
        error.setRuntimeId(runtimeId);
        error.setInstanceId(instanceId.trim());
        error.setErrorFingerprint(fingerprint);
        error.setErrorCode(sanitizedCode);
        error.setErrorMessage(sanitizedMessage == null ? "循环周期失败" : sanitizedMessage);
        error.setStatus(SchedulerLoopErrorStatus.OPEN);
        error.setFirstSeenAt(now);
        error.setLastSeenAt(now);
        error.setLastLoggedAt(now);
        error.setOccurrenceCount(1L);
        error.setSuppressedCount(0L);
        error.setLastContext(sanitizeContext(context));
        error.setCreatedAt(now);
        error.setUpdatedAt(now);
        error.setVersion(0L);
        var interval = logInterval == null ? DEFAULT_LOG_INTERVAL : logInterval;
        if (interval.isNegative() || interval.isZero()) {
            throw new IllegalArgumentException("循环错误日志限流间隔必须大于 0");
        }
        var updated = errorMapper.upsertOccurrence(error, interval.toMillis());
        if (updated == null) {
            throw new IllegalStateException("循环错误聚合写入失败");
        }
        boolean shouldLog = updated.getLastLoggedAt() != null
                && updated.getLastSeenAt() != null
                && updated.getLastLoggedAt().equals(updated.getLastSeenAt());
        if (shouldLog) {
            log.error("循环任务错误聚合: jobId={}, instanceId={}, errorCode={}, occurrences={}, suppressed={}",
                    jobId, instanceId, updated.getErrorCode(), updated.getOccurrenceCount(), updated.getSuppressedCount());
        } else {
            log.debug("循环任务重复错误已聚合: jobId={}, instanceId={}, errorCode={}", jobId, instanceId, sanitizedCode);
        }
        return new LoopErrorOccurrence(updated, shouldLog);
    }

    private static String sanitizeCode(String code) {
        var value = code.trim().toUpperCase();
        return value.matches("[A-Z0-9_.:-]{1,100}") ? value : "LOOP_HANDLER_ERROR";
    }

    private static String sanitizeMessage(String message) {
        if (message == null || message.isBlank()) {
            return null;
        }
        var value = message.replaceAll("[\\r\\n\\t]+", " ").trim();
        return value.length() <= 500 ? value : value.substring(0, 500);
    }

    private static Map<String, Object> sanitizeContext(Map<String, Object> context) {
        if (context == null || context.isEmpty()) {
            return Map.of();
        }
        var sanitized = new LinkedHashMap<String, Object>();
        context.entrySet().stream().limit(20).forEach(entry -> {
            var key = entry.getKey();
            if (key != null && key.matches("[A-Za-z0-9_.:-]{1,100}")) {
                var value = entry.getValue();
                if (value == null || value instanceof Number || value instanceof Boolean) {
                    sanitized.put(key, value);
                } else {
                    sanitized.put(key, sanitizeMessage(String.valueOf(value)));
                }
            }
        });
        return Map.copyOf(sanitized);
    }

    private static String fingerprint(String code, String message) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            var bytes = digest.digest((code + "\n" + (message == null ? "" : message))
                    .getBytes(StandardCharsets.UTF_8));
            var builder = new StringBuilder(bytes.length * 2);
            for (byte value : bytes) {
                builder.append(String.format("%02x", value));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 不可用", exception);
        }
    }

    /** 一次错误聚合后的持久化结果。 */
    public record LoopErrorOccurrence(SchedulerLoopErrorEntity error, boolean shouldLog) {
    }
}
