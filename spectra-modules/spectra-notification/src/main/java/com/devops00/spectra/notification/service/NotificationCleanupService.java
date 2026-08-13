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

package com.devops00.spectra.notification.service;

import com.devops00.spectra.notification.mapper.NotificationRequestMapper;
import com.devops00.spectra.notification.mapper.NotificationTaskMapper;
import com.devops00.spectra.notification.properties.NotificationCleanupProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * 清理已进入终态或已过期记录中的敏感渲染载荷。
 */
@Service
@RequiredArgsConstructor
public class NotificationCleanupService {

    private final NotificationRequestMapper requestMapper;

    private final NotificationTaskMapper taskMapper;

    private final NotificationCleanupProperties properties;

    /**
     * 定时执行清理；关闭时保持完全无副作用。
     */
    @Scheduled(fixedDelayString = "${spectra.notification.cleanup.fixed-delay-ms:3600000}")
    public void scheduledCleanup() {
        cleanupSensitivePayloads();
    }

    /**
     * 批量清理请求和任务的敏感密文，并返回匿名计数。
     */
    @Transactional
    public NotificationCleanupResult cleanupSensitivePayloads() {
        if (!properties.enabled()) {
            return new NotificationCleanupResult(0, 0);
        }
        var now = Instant.now();
        var cutoff = now.minusSeconds(properties.retentionSeconds());
        var requestCount = requestMapper.clearSensitivePayloads(now, cutoff, properties.batchSize());
        var taskCount = taskMapper.clearSensitivePayloads(now, cutoff, properties.batchSize());
        return new NotificationCleanupResult(requestCount, taskCount);
    }

    /**
     * 清理结果；不包含任何业务 ID、正文或地址。
     */
    public record NotificationCleanupResult(int requestCount, int taskCount) {
    }
}
