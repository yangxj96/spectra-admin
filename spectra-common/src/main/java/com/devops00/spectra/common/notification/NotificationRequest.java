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

package com.devops00.spectra.common.notification;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 业务模块提交的不可变通知请求契约。
 * 实现模块不得接受任务状态、Provider Bean 名或重试字段。
 *
 * @param requestId           调用方生成的外部请求 ID；为空时由通知模块生成
 * @param idempotencyKey      业务幂等键，同一租户内重复提交时返回原请求结果
 * @param purpose             通知用途，决定渠道策略、模板和用户偏好规则
 * @param channels            显式请求的投递渠道；空集合由通知策略选择默认渠道
 * @param recipientUserIds    系统用户 ID 列表，由收件人目录解析为可用地址
 * @param directAddresses     未登录场景的直接投递地址，仅允许安全用途使用
 * @param templateGroupCode   逻辑模板组编码，通知模块据此锁定渠道模板版本
 * @param parameters          可以进入日志和业务快照的非敏感模板参数
 * @param sensitiveParameters 验证码等敏感模板参数，持久化前必须加密且不得记录明文
 * @param businessType        业务对象类型，与 {@link #businessId()} 共同构成弱引用
 * @param businessId          业务对象 ID，仅用于追踪和跳转，不建立跨模块外键
 * @param sourceModule        发起请求的业务模块编码
 * @param sourceDepartmentId  发起请求时的来源部门 ID，无部门上下文时可为空
 * @param scheduledAt         计划开始投递时间；为空时表示尽快投递
 * @param expiresAt           投递截止时间；超过该时间的未完成任务不再发送
 * @param priority            任务优先级，数值越大越优先领取
 * @param link                客户端站内跳转路径，必须是不含路径穿越的站内相对路径
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/11
 */
public record NotificationRequest(UUID requestId, String idempotencyKey, NotificationPurpose purpose,
        List<NotificationChannel> channels, List<UUID> recipientUserIds,
        List<NotificationDirectAddress> directAddresses, String templateGroupCode,
        Map<String, Object> parameters, Map<String, Object> sensitiveParameters, String businessType,
        String businessId, String sourceModule, UUID sourceDepartmentId, Instant scheduledAt,
        Instant expiresAt, Integer priority, String link) {

    /**
     * 将可空的集合与参数快照归一化为不可变空集合，避免请求入队后被调用方继续修改。
     */
    public NotificationRequest {
        channels = channels == null ? List.of() : List.copyOf(channels);
        recipientUserIds = recipientUserIds == null ? List.of() : List.copyOf(recipientUserIds);
        directAddresses = directAddresses == null ? List.of() : List.copyOf(directAddresses);
        parameters = parameters == null ? Map.of() : Map.copyOf(parameters);
        sensitiveParameters = sensitiveParameters == null ? Map.of() : Map.copyOf(sensitiveParameters);
    }

    /**
     * 兼容尚未迁移的调用方；敏感参数默认为空，业务方不得把验证码等敏感值放入普通参数。
     *
     * @param requestId          调用方请求 ID
     * @param idempotencyKey     业务幂等键
     * @param purpose            通知用途
     * @param channels           投递渠道
     * @param recipientUserIds   接收用户 ID
     * @param templateGroupCode  模板组编码
     * @param parameters         非敏感模板参数
     * @param businessType       业务类型
     * @param businessId         业务 ID
     * @param sourceModule       来源模块
     * @param sourceDepartmentId 来源部门 ID
     * @param scheduledAt        计划投递时间
     * @param expiresAt          投递截止时间
     * @param priority           任务优先级
     * @param link               客户端站内跳转路径
     */
    public NotificationRequest(UUID requestId, String idempotencyKey, NotificationPurpose purpose,
            List<NotificationChannel> channels, List<UUID> recipientUserIds, String templateGroupCode,
            Map<String, Object> parameters, String businessType, String businessId, String sourceModule,
            UUID sourceDepartmentId, Instant scheduledAt, Instant expiresAt, Integer priority, String link) {
        this(requestId, idempotencyKey, purpose, channels, recipientUserIds, List.of(), templateGroupCode, parameters, Map.of(),
                businessType, businessId, sourceModule, sourceDepartmentId, scheduledAt, expiresAt, priority, link);
    }

    /**
     * 创建只投递站内信的业务通知请求，标题和正文作为非敏感模板参数进入通知模块。
     *
     * @param idempotencyKey    业务幂等键
     * @param purpose           通知用途
     * @param recipientUserIds  接收用户 ID
     * @param templateGroupCode 模板组编码
     * @param title             消息标题
     * @param content           消息正文
     * @param businessType      业务类型
     * @param businessId        业务 ID
     * @param sourceModule      来源模块
     * @param link              客户端站内跳转路径
     * @return 仅包含 {@link NotificationChannel#IN_APP} 渠道的不可变通知请求
     */
    public static NotificationRequest inApp(String idempotencyKey, NotificationPurpose purpose,
            List<UUID> recipientUserIds, String templateGroupCode, String title, String content,
            String businessType, String businessId, String sourceModule, String link) {
        var parameters = new java.util.HashMap<String, Object>();
        parameters.put("title", title == null ? "通知" : title);
        parameters.put("content", content == null ? "" : content);
        return new NotificationRequest(null, idempotencyKey, purpose, List.of(NotificationChannel.IN_APP),
                recipientUserIds, List.of(), templateGroupCode, parameters, Map.of(), businessType, businessId,
                sourceModule, null, null, null, 0, link);
    }
}
