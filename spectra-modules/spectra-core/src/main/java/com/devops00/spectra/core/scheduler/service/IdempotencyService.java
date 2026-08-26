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

import com.devops00.spectra.core.scheduler.javabean.entity.SchedulerJobEntity;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.UUID;

/**
 * 调度触发和重试使用的幂等键生成器。
 */
@Component
public class IdempotencyService {

    /**
     * 由任务键、版本和计划时间组成稳定的调度 fire key。
     */
    public String fireKey(SchedulerJobEntity job, Instant scheduledAt) {
        if (job == null || job.getJobKey() == null || scheduledAt == null) {
            throw new IllegalArgumentException("生成 fire_key 缺少任务或计划时间");
        }
        return "%s:%d:%s".formatted(job.getJobKey(), job.getRevision(), scheduledAt);
    }

    /**
     * 为人工重试生成不复用原记录的幂等键。
     */
    public String retryKey(UUID originalExecutionId, UUID newExecutionId) {
        if (originalExecutionId == null || newExecutionId == null) {
            throw new IllegalArgumentException("生成重试幂等键缺少执行 ID");
        }
        return "retry:%s:%s".formatted(originalExecutionId, newExecutionId);
    }

    /**
     * 为人工触发生成长度受控且可重复计算的幂等键。
     */
    public String manualKey(UUID jobId, String idempotencyKey) {
        if (jobId == null || idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("生成手工触发幂等键缺少任务或幂等键");
        }
        return "manual:%s:%s".formatted(jobId, sha256(idempotencyKey.trim()));
    }

    /**
     * 为人工重试生成可重复计算的幂等键。
     */
    public String retryKey(UUID originalExecutionId, String idempotencyKey) {
        if (originalExecutionId == null || idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("生成重试幂等键缺少执行或幂等键");
        }
        return "retry:%s:%s".formatted(originalExecutionId, sha256(idempotencyKey.trim()));
    }

    private static String sha256(String value) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            var bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            var result = new StringBuilder(bytes.length * 2);
            for (var item : bytes) {
                result.append(String.format("%02x", item));
            }
            return result.toString();
        } catch (java.security.NoSuchAlgorithmException exception) {
            throw new IllegalStateException("JVM 不支持 SHA-256", exception);
        }
    }
}
