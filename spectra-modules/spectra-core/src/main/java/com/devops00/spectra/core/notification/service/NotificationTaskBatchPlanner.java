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

import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.common.notification.NotificationRequest;
import com.devops00.spectra.core.notification.security.NotificationDigest;
import com.devops00.spectra.core.notification.configuration.NotificationPayloadProtector;
import com.devops00.spectra.core.notification.javabean.domain.NotificationTaskStatus;
import com.devops00.spectra.core.notification.javabean.entity.NotificationTaskEntity;
import com.devops00.spectra.core.notification.policy.NotificationAddressMasker;
import com.github.f4b6a3.uuid.UuidCreator;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 通知任务批量规划器，负责把收件人和渠道快照转换为可持久化任务草稿。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/9/7
 */
@Component
public class NotificationTaskBatchPlanner {

    private final NotificationPayloadProtector payloadProtector;

    public NotificationTaskBatchPlanner(NotificationPayloadProtector payloadProtector) {
        this.payloadProtector = payloadProtector;
    }

    /**
     * 按去重后的接收人和渠道集合规划任务，不执行数据库读写。
     *
     * @param request           原始通知请求
     * @param requestId         已持久化的通知请求 ID
     * @param now               当前时间快照
     * @param auditUserId       创建和更新审计人
     * @param targets           去重后的接收人和渠道目标
     * @param templateSnapshots 各渠道渲染模板快照
     * @return 带有稳定幂等键的任务草稿
     */
    public List<TaskDraft> plan(NotificationRequest request, UUID requestId, Instant now, UUID auditUserId,
                                Collection<TaskTarget> targets,
                                Map<NotificationChannel, TemplateSnapshot> templateSnapshots) {
        var hasSensitivePayload = !request.sensitiveParameters().isEmpty();
        return targets.stream().map(target -> {
            var rendered = templateSnapshots.get(target.channel());
            var task = new NotificationTaskEntity();
            var recipientKeyHash = recipientKeyHash(target.recipientUserId(), target.channel(), target.address());
            task.setId(UuidCreator.getTimeOrderedEpoch());
            task.setCreatedBy(auditUserId);
            task.setCreatedAt(now);
            task.setUpdatedBy(auditUserId);
            task.setUpdatedAt(now);
            task.setNotificationRequestId(requestId);
            task.setReceiverUserId(target.recipientUserId());
            task.setRecipientKeyHash(recipientKeyHash);
            task.setRecipientMasked(NotificationAddressMasker.maskAddress(target.address()));
            task.setRecipientCiphertext(target.address() == null ? null : payloadProtector.protectAddress(target.address()));
            task.setChannel(target.channel().name());
            task.setPurpose(request.purpose().name());
            task.setTemplateId(rendered.templateId());
            task.setTemplateVersionNo(rendered.versionNo());
            task.setTemplateVersionDigest(rendered.versionDigest());
            task.setTitle(hasSensitivePayload ? "安全通知" : rendered.title());
            task.setContent(hasSensitivePayload ? "敏感通知内容已加密" : rendered.content());
            task.setLink(request.link());
            var taskParameters = new LinkedHashMap<String, Object>(request.parameters());
            if (rendered.providerTemplateCode() != null && !rendered.providerTemplateCode().isBlank()) {
                taskParameters.put("__provider_template_code", rendered.providerTemplateCode());
            }
            task.setExtra(taskParameters);
            if (hasSensitivePayload) {
                var protectedPayload = new LinkedHashMap<String, Object>();
                protectedPayload.put("title", rendered.title());
                protectedPayload.put("content", rendered.content());
                protectedPayload.put("parameters", request.sensitiveParameters());
                task.setSensitiveParametersCiphertext(payloadProtector.protectParameters(protectedPayload));
            }
            task.setPriority(normalizePriority(request.priority()));
            task.setAttemptCount(0);
            task.setMaxAttempts(3);
            task.setScheduledAt(request.scheduledAt() == null ? now : request.scheduledAt());
            task.setNextRetryAt(task.getScheduledAt());
            task.setExpiresAt(request.expiresAt());
            task.setStatus(NotificationTaskStatus.PENDING.name());
            return new TaskDraft(task, recipientKeyHash, target.channel().name());
        }).toList();
    }

    /**
     * 处理键哈希值相关数据。
     */
    private String recipientKeyHash(UUID recipientUserId, NotificationChannel channel, String address) {
        var key = recipientUserId == null ? channel.name() + ":" + address : recipientUserId.toString();
        return NotificationDigest.hash(key);
    }

    /**
     * 规范化通知任务批次。
     */
    private int normalizePriority(Integer priority) {
        return priority == null ? 0 : priority;
    }

    /**
     * 定义任务目标相关的应用服务契约。
     *
     * @param recipientUserId 用户标识
     * @param channel         通知任务使用的发送渠道
     * @param address         通知渠道使用的接收方地址
     * @author yangxj96
     * @version 1.0
     * @since 2026/09/13
     */
    public record TaskTarget(UUID recipientUserId, NotificationChannel channel, String address) {
    }

    /**
     * 定义模板快照相关的应用服务契约。
     *
     * @param templateId           模板标识
     * @param versionNo            通知模板定义的版本号
     * @param versionDigest        版本摘要
     * @param providerTemplateCode 提供器模板编码
     * @param title                通知消息的标题
     * @param content              通知模板的正文内容
     * @author yangxj96
     * @version 1.0
     * @since 2026/09/13
     */
    public record TemplateSnapshot(UUID templateId, Integer versionNo, String versionDigest,
                                   String providerTemplateCode, String title, String content) {
    }

    /**
     * 定义任务相关的应用服务契约。
     *
     * @param task             尚未持久化的待发送通知任务
     * @param recipientKeyHash 键哈希值
     * @param channel          通知任务使用的发送渠道
     * @author yangxj96
     * @version 1.0
     * @since 2026/09/13
     */
    public record TaskDraft(NotificationTaskEntity task, String recipientKeyHash, String channel) {

        /**
         * 返回与数据库唯一索引一致的任务键。
         *
         * @return 接收人哈希和渠道组合键
         */
        public String idempotencyKey() {
            return recipientKeyHash + "\u0000" + channel;
        }
    }
}
