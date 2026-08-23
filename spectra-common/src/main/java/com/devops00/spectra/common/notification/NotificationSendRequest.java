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
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 业务调用方使用的快捷通知请求。
 *
 * <p>模板编码、投递渠道、收件人和参数是常用必填项；业务追踪、计划时间、敏感参数等字段按场景补充。
 * 请求最终仍由通知网关执行幂等、偏好、模板和安全校验。</p>
 *
 * @param requestId           调用方生成的外部请求 ID，可为空
 * @param idempotencyKey      业务幂等键
 * @param purpose             通知用途
 * @param channels            投递渠道
 * @param recipientUserIds    收件用户 ID
 * @param directAddresses     直接投递地址，仅安全用途允许使用
 * @param templateGroupCode   模板组编码
 * @param parameters          普通模板参数
 * @param sensitiveParameters 敏感模板参数
 * @param businessType        业务类型
 * @param businessId          业务 ID
 * @param sourceModule        来源模块
 * @param sourceDepartmentId  来源部门 ID
 * @param scheduledAt         计划投递时间
 * @param expiresAt           投递截止时间
 * @param priority            任务优先级
 * @param link                站内跳转路径
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/24
 */
public record NotificationSendRequest(UUID requestId, String idempotencyKey, NotificationPurpose purpose,
                                      List<NotificationChannel> channels, List<UUID> recipientUserIds,
                                      List<NotificationDirectAddress> directAddresses, String templateGroupCode,
                                      Map<String, Object> parameters, Map<String, Object> sensitiveParameters,
                                      String businessType, String businessId, String sourceModule,
                                      UUID sourceDepartmentId, Instant scheduledAt, Instant expiresAt, Integer priority,
                                      String link) {

    /**
     * 归一化调用方传入的集合和参数，避免发送前被外部修改。
     */
    public NotificationSendRequest {
        channels = channels == null ? List.of() : List.copyOf(channels);
        recipientUserIds = recipientUserIds == null ? List.of() : List.copyOf(recipientUserIds);
        directAddresses = directAddresses == null ? List.of() : List.copyOf(directAddresses);
        parameters = parameters == null ? Map.of() : Map.copyOf(parameters);
        sensitiveParameters = sensitiveParameters == null ? Map.of() : Map.copyOf(sensitiveParameters);
    }

    /**
     * 创建请求构造器。
     *
     * @return 请求构造器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 创建站内信请求构造器。
     *
     * @param idempotencyKey    业务幂等键
     * @param purpose           通知用途
     * @param recipientUserIds  收件用户 ID
     * @param templateGroupCode 模板组编码
     * @return 已设置站内信渠道的构造器
     */
    public static Builder inApp(String idempotencyKey, NotificationPurpose purpose,
                                Collection<UUID> recipientUserIds, String templateGroupCode) {
        return builder()
                .idempotencyKey(idempotencyKey)
                .purpose(purpose)
                .channels(NotificationChannel.IN_APP)
                .recipientUserIds(recipientUserIds)
                .templateGroupCode(templateGroupCode);
    }

    /**
     * 创建直接地址请求构造器。
     *
     * @param idempotencyKey    业务幂等键
     * @param purpose           通知用途
     * @param channel           投递渠道
     * @param address           直接投递地址
     * @param templateGroupCode 模板组编码
     * @return 已设置直接地址和渠道的构造器
     */
    public static Builder direct(String idempotencyKey, NotificationPurpose purpose, NotificationChannel channel,
                                 String address, String templateGroupCode) {
        return builder()
                .idempotencyKey(idempotencyKey)
                .purpose(purpose)
                .channels(channel)
                .directAddress(new NotificationDirectAddress(channel, address))
                .templateGroupCode(templateGroupCode);
    }

    /**
     * 创建多渠道直接地址请求构造器。
     *
     * @param idempotencyKey    业务幂等键
     * @param purpose           通知用途
     * @param directAddresses   渠道与直接地址集合
     * @param templateGroupCode 模板组编码
     * @return 已设置多渠道直接地址的构造器
     */
    public static Builder direct(String idempotencyKey, NotificationPurpose purpose,
                                 Collection<NotificationDirectAddress> directAddresses, String templateGroupCode) {
        return builder()
                .idempotencyKey(idempotencyKey)
                .purpose(purpose)
                .directAddresses(directAddresses)
                .templateGroupCode(templateGroupCode);
    }

    /**
     * 快捷通知请求构造器。
     */
    public static final class Builder {

        private UUID requestId;

        private String idempotencyKey;

        private NotificationPurpose purpose;

        private final List<NotificationChannel> channels = new ArrayList<>();

        private final List<UUID> recipientUserIds = new ArrayList<>();

        private final List<NotificationDirectAddress> directAddresses = new ArrayList<>();

        private String templateGroupCode;

        private final Map<String, Object> parameters = new LinkedHashMap<>();

        private final Map<String, Object> sensitiveParameters = new LinkedHashMap<>();

        private String businessType;

        private String businessId;

        private String sourceModule;

        private UUID sourceDepartmentId;

        private Instant scheduledAt;

        private Instant expiresAt;

        private Integer priority;

        private String link;

        /**
         * 设置外部请求 ID。
         *
         * @param requestId 外部请求 ID
         * @return 当前构造器
         */
        public Builder requestId(UUID requestId) {
            this.requestId = requestId;
            return this;
        }

        /**
         * 设置业务幂等键。
         *
         * @param idempotencyKey 业务幂等键
         * @return 当前构造器
         */
        public Builder idempotencyKey(String idempotencyKey) {
            this.idempotencyKey = idempotencyKey;
            return this;
        }

        /**
         * 设置通知用途。
         *
         * @param purpose 通知用途
         * @return 当前构造器
         */
        public Builder purpose(NotificationPurpose purpose) {
            this.purpose = purpose;
            return this;
        }

        /**
         * 添加投递渠道。
         *
         * @param channel 投递渠道
         * @return 当前构造器
         */
        public Builder channels(NotificationChannel... channel) {
            if (channel != null) {
                for (var item : channel) {
                    if (item != null && !channels.contains(item)) {
                        channels.add(item);
                    }
                }
            }
            return this;
        }

        /**
         * 设置投递渠道集合。
         *
         * @param channels 投递渠道集合
         * @return 当前构造器
         */
        public Builder channels(Collection<NotificationChannel> channels) {
            if (channels != null) {
                channels(channels.toArray(NotificationChannel[]::new));
            }
            return this;
        }

        /**
         * 设置用户收件人集合。
         *
         * @param recipientUserIds 用户 ID 集合
         * @return 当前构造器
         */
        public Builder recipientUserIds(Collection<UUID> recipientUserIds) {
            if (recipientUserIds != null) {
                this.recipientUserIds.addAll(recipientUserIds);
            }
            return this;
        }

        /**
         * 添加直接投递地址。
         *
         * @param directAddress 直接投递地址
         * @return 当前构造器
         */
        public Builder directAddress(NotificationDirectAddress directAddress) {
            if (directAddress != null) {
                channels(directAddress.channel());
                directAddresses.add(directAddress);
            }
            return this;
        }

        /**
         * 添加多个直接投递地址，并自动补齐对应渠道。
         *
         * @param directAddresses 直接投递地址集合
         * @return 当前构造器
         */
        public Builder directAddresses(Collection<NotificationDirectAddress> directAddresses) {
            if (directAddresses != null) {
                directAddresses.forEach(this::directAddress);
            }
            return this;
        }

        /**
         * 设置模板组编码。
         *
         * @param templateGroupCode 模板组编码
         * @return 当前构造器
         */
        public Builder templateGroupCode(String templateGroupCode) {
            this.templateGroupCode = templateGroupCode;
            return this;
        }

        /**
         * 添加普通模板参数。
         *
         * @param name  参数名
         * @param value 参数值
         * @return 当前构造器
         */
        public Builder parameter(String name, Object value) {
            parameters.put(name, value);
            return this;
        }

        /**
         * 合并普通模板参数。
         *
         * @param parameters 模板参数
         * @return 当前构造器
         */
        public Builder parameters(Map<String, ?> parameters) {
            if (parameters != null) {
                this.parameters.putAll(parameters);
            }
            return this;
        }

        /**
         * 添加敏感模板参数。
         *
         * @param name  参数名
         * @param value 参数值
         * @return 当前构造器
         */
        public Builder sensitiveParameter(String name, Object value) {
            sensitiveParameters.put(name, value);
            return this;
        }

        /**
         * 合并敏感模板参数。
         *
         * @param parameters 敏感模板参数
         * @return 当前构造器
         */
        public Builder sensitiveParameters(Map<String, ?> parameters) {
            if (parameters != null) {
                this.sensitiveParameters.putAll(parameters);
            }
            return this;
        }

        /**
         * 设置业务弱引用。
         *
         * @param businessType 业务类型
         * @param businessId   业务 ID
         * @return 当前构造器
         */
        public Builder businessReference(String businessType, String businessId) {
            this.businessType = businessType;
            this.businessId = businessId;
            return this;
        }

        /**
         * 设置来源模块。
         *
         * @param sourceModule 来源模块
         * @return 当前构造器
         */
        public Builder sourceModule(String sourceModule) {
            this.sourceModule = sourceModule;
            return this;
        }

        /**
         * 设置来源部门。
         *
         * @param sourceDepartmentId 来源部门 ID
         * @return 当前构造器
         */
        public Builder sourceDepartmentId(UUID sourceDepartmentId) {
            this.sourceDepartmentId = sourceDepartmentId;
            return this;
        }

        /**
         * 设置计划投递时间。
         *
         * @param scheduledAt 计划投递时间
         * @return 当前构造器
         */
        public Builder scheduledAt(Instant scheduledAt) {
            this.scheduledAt = scheduledAt;
            return this;
        }

        /**
         * 设置投递截止时间。
         *
         * @param expiresAt 投递截止时间
         * @return 当前构造器
         */
        public Builder expiresAt(Instant expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        /**
         * 设置任务优先级。
         *
         * @param priority 任务优先级
         * @return 当前构造器
         */
        public Builder priority(Integer priority) {
            this.priority = priority;
            return this;
        }

        /**
         * 设置站内跳转链接。
         *
         * @param link 站内跳转链接
         * @return 当前构造器
         */
        public Builder link(String link) {
            this.link = link;
            return this;
        }

        /**
         * 构建不可变快捷通知请求。
         *
         * @return 快捷通知请求
         */
        public NotificationSendRequest build() {
            return new NotificationSendRequest(requestId, idempotencyKey, purpose, channels, recipientUserIds,
                    directAddresses, templateGroupCode, parameters, sensitiveParameters, businessType, businessId,
                    sourceModule, sourceDepartmentId, scheduledAt, expiresAt, priority, link);
        }
    }
}
