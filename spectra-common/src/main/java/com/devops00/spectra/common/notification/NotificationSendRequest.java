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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 业务调用方使用的同步通知发送请求。
 *
 * @param templateCode        模板编码
 * @param channels            发送渠道
 * @param recipientUserIds    收件用户 ID
 * @param directAddresses     直接地址，未登录场景使用
 * @param parameters          普通模板参数
 * @param sensitiveParameters 敏感模板参数，仅用于渲染
 * @param businessType        业务类型
 * @param businessId          业务 ID
 * @param sourceModule        来源模块
 * @param link                站内跳转路径
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/30
 */
public record NotificationSendRequest(String templateCode, List<NotificationChannel> channels,
                                      List<UUID> recipientUserIds, List<NotificationDirectAddress> directAddresses,
                                      Map<String, Object> parameters, Map<String, Object> sensitiveParameters,
                                      String businessType, String businessId, String sourceModule, String link) {

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
     * @param templateCode     模板编码
     * @param recipientUserIds 收件用户 ID
     * @return 请求构造器
     */
    public static Builder inApp(String templateCode, Collection<UUID> recipientUserIds) {
        return builder()
                .templateCode(templateCode)
                .channels(NotificationChannel.IN_APP)
                .recipientUserIds(recipientUserIds);
    }

    /**
     * 创建单渠道直接地址请求构造器。
     *
     * @param templateCode 模板编码
     * @param channel      发送渠道
     * @param address      直接地址
     * @return 请求构造器
     */
    public static Builder direct(String templateCode, NotificationChannel channel, String address) {
        return builder()
                .templateCode(templateCode)
                .directAddress(new NotificationDirectAddress(channel, address));
    }

    /**
     * 创建多渠道直接地址请求构造器。
     *
     * @param templateCode    模板编码
     * @param directAddresses 直接地址集合
     * @return 请求构造器
     */
    public static Builder direct(String templateCode, Collection<NotificationDirectAddress> directAddresses) {
        return builder()
                .templateCode(templateCode)
                .directAddresses(directAddresses);
    }

    /**
     * 同步通知请求构造器。
     *
     * @author yangxj96
     * @version 1.0
     * @since 2026/10/30
     */
    public static final class Builder {

        private String templateCode;

        private final List<NotificationChannel> channels = new ArrayList<>();

        private final List<UUID> recipientUserIds = new ArrayList<>();

        private final List<NotificationDirectAddress> directAddresses = new ArrayList<>();

        private final Map<String, Object> parameters = new LinkedHashMap<>();

        private final Map<String, Object> sensitiveParameters = new LinkedHashMap<>();

        private String businessType;

        private String businessId;

        private String sourceModule;

        private String link;

        /**
         * 设置模板编码。
         *
         * @param templateCode 模板编码
         * @return 当前构造器
         */
        public Builder templateCode(String templateCode) {
            this.templateCode = templateCode;
            return this;
        }

        /**
         * 添加发送渠道。
         *
         * @param channel 发送渠道
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
         * 设置发送渠道集合。
         *
         * @param channels 发送渠道集合
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
         * 添加直接地址并补齐对应渠道。
         *
         * @param directAddress 直接地址
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
         * 添加多个直接地址并补齐对应渠道。
         *
         * @param directAddresses 直接地址集合
         * @return 当前构造器
         */
        public Builder directAddresses(Collection<NotificationDirectAddress> directAddresses) {
            if (directAddresses != null) {
                directAddresses.forEach(this::directAddress);
            }
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
         * 构建不可变同步通知请求。
         *
         * @return 同步通知请求
         */
        public NotificationSendRequest build() {
            return new NotificationSendRequest(templateCode, channels, recipientUserIds, directAddresses, parameters,
                    sensitiveParameters, businessType, businessId, sourceModule, link);
        }
    }
}
