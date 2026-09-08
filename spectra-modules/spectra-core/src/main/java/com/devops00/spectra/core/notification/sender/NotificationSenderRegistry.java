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

package com.devops00.spectra.core.notification.sender;

import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.common.notification.NotificationChannel;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 按通知渠道解析发送器的不可变 Registry。
 *
 * <p>Registry 只负责启动时校验和渠道索引，不判断发送器当前可用性，也不参与 Provider 配置和健康缓存。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/9/8
 */
@Component
public final class NotificationSenderRegistry {

    private final Map<NotificationChannel, NotificationSender> senders;

    /**
     * 复制并校验 Spring 收集的发送器列表。
     *
     * @param senderList Spring 按类型收集的通知发送器；不能为 null，且每个发送器必须声明唯一非空渠道
     * @throws IllegalArgumentException 发送器列表、发送器实例或渠道声明为空时抛出
     * @throws IllegalStateException    同一渠道存在多个发送器实现时抛出
     */
    public NotificationSenderRegistry(List<NotificationSender> senderList) {
        if (senderList == null) {
            throw new IllegalArgumentException("通知发送器列表不能为空");
        }
        var indexed = new EnumMap<NotificationChannel, NotificationSender>(NotificationChannel.class);
        for (var sender : senderList) {
            if (sender == null) {
                throw new IllegalArgumentException("通知发送器不能为空");
            }
            var channel = sender.channel();
            if (channel == null) {
                throw new IllegalArgumentException("通知发送器渠道不能为空");
            }
            if (indexed.putIfAbsent(channel, sender) != null) {
                throw new IllegalStateException("通知渠道发送器重复注册: " + channel);
            }
        }
        this.senders = Map.copyOf(indexed);
    }

    /**
     * 查找指定渠道的发送器。
     *
     * @param channel 要解析的通知渠道；为 null 时视为没有匹配发送器
     * @return 已注册的发送器；渠道为空或未注册时返回 {@link Optional#empty()}
     */
    public Optional<NotificationSender> find(NotificationChannel channel) {
        return channel == null ? Optional.empty() : Optional.ofNullable(senders.get(channel));
    }

    /**
     * 获取指定渠道的发送器，供必须具备渠道实现的调用方使用。
     *
     * @param channel 要解析的通知渠道；不能为 null
     * @return 已注册的发送器，即使该发送器当前因 Provider 状态不可用也仍返回
     * @throws DataSaveException 渠道为空或没有注册发送器时抛出，与现有通知不可用错误语义保持一致
     */
    public NotificationSender require(NotificationChannel channel) {
        if (channel == null) {
            throw new DataSaveException("通知渠道不能为空");
        }
        return find(channel).orElseThrow(() -> new DataSaveException(
                "通知渠道暂不可用: " + channel + "，CHANNEL_NOT_REGISTERED"));
    }
}
