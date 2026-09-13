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

package com.devops00.spectra.core.notification.service;

import com.devops00.spectra.core.notification.mapper.NotificationRequestMapper;
import com.devops00.spectra.core.notification.mapper.NotificationSendPreviewMapper;
import com.devops00.spectra.core.notification.mapper.NotificationTaskMapper;
import com.devops00.spectra.core.notification.properties.NotificationCleanupProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * 清理已进入终态或已过期记录中的敏感渲染载荷。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@Service
@RequiredArgsConstructor
public class NotificationCleanupService {

    private final NotificationRequestMapper requestMapper;

    private final NotificationTaskMapper taskMapper;

    private final NotificationCleanupProperties properties;

    /**
     * 短时 Preview 快照 Mapper；精简测试运行时可以不注册。
     */
    private NotificationSendPreviewMapper previewMapper;

    /**
     * 更新或推进目标状态（{@code setPreviewMapper}）。
     */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public void setPreviewMapper(NotificationSendPreviewMapper previewMapper) {
        this.previewMapper = previewMapper;
    }

    /**
     * 批量清理过期请求和任务中的敏感密文，并返回清理数量；功能关闭时不执行清理。
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
        if (previewMapper != null) {
            previewMapper.deleteExpired(now, now.minusSeconds(3600));
        }
        return new NotificationCleanupResult(requestCount, taskCount);
    }

    /**
     * 定义通知清理结果相关的应用服务契约。
     *
     * @param requestCount 待清理的请求数量
     * @param taskCount    待处理的任务数量
     * @author yangxj96
     * @version 1.0
     * @since 2026/09/13
     */
    public record NotificationCleanupResult(int requestCount, int taskCount) {
    }
}
