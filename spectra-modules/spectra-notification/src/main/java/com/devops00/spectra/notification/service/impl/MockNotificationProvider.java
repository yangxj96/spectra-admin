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

package com.devops00.spectra.notification.service.impl;

import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.notification.configuration.NotificationPayloadProtector;
import com.devops00.spectra.notification.javabean.domain.ChannelSendResult;
import com.devops00.spectra.notification.javabean.domain.NotificationProviderConfiguration;
import com.devops00.spectra.notification.javabean.domain.NotificationProviderHealth;
import com.devops00.spectra.notification.javabean.entity.NotificationTaskEntity;
import com.devops00.spectra.notification.service.NotificationProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * 内置通知模拟 Provider；除最终发送动作外，其余流程与真实 Provider 完全一致。
 *
 * <p>模拟发送只输出脱敏收件人、标题和正文快照，不访问网络、不写额外业务表，便于本地和测试环境在没有第三方服务时
 * 验证 Worker、Delivery 和管理页面的完整链路。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/24
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MockNotificationProvider implements NotificationProvider {

    /** Provider 类型编码。 */
    private static final String CODE = "MOCK";

    /** 通知地址解密器。 */
    private final NotificationPayloadProtector payloadProtector;

    @Override
    public String code() {
        return CODE;
    }

    @Override
    public boolean supports(NotificationChannel channel) {
        return channel == NotificationChannel.SMS || channel == NotificationChannel.EMAIL;
    }

    @Override
    public NotificationProviderHealth health(NotificationProviderConfiguration configuration) {
        var checkedAt = Instant.now();
        if (configuration == null || !configuration.enabled() || !CODE.equals(configuration.providerType())) {
            return new NotificationProviderHealth("BLOCKED", "PROVIDER_CONFIGURATION_INVALID", checkedAt);
        }
        return new NotificationProviderHealth("HEALTHY", "MOCK_PROVIDER_READY", checkedAt);
    }

    @Override
    public ChannelSendResult send(NotificationTaskEntity task, NotificationProviderConfiguration configuration) {
        if (configuration == null || !configuration.enabled() || !CODE.equals(configuration.providerType())) {
            return new ChannelSendResult("BLOCKED", CODE, null, "PROVIDER_CONFIGURATION_INVALID");
        }
        final String recipient;
        try {
            recipient = payloadProtector.unprotectAddress(task.getRecipientCiphertext());
        } catch (RuntimeException exception) {
            return new ChannelSendResult("BLOCKED", CODE, null, "RECIPIENT_ADDRESS_UNAVAILABLE");
        }
        var taskId = task.getId() == null ? "none" : task.getId().toString();
        var recipientMasked = task.getRecipientMasked();
        if (recipientMasked == null || recipientMasked.isBlank()) {
            recipientMasked = mask(recipient);
        }
        log.info("通知模拟发送: channel={}, taskId={}, recipient={}, title={}, content={}",
                task.getChannel(), taskId, recipientMasked, task.getTitle(), task.getContent());
        return new ChannelSendResult("SENT", CODE, "mock-" + taskId, "MOCK_ACCEPTED");
    }

    private String mask(String address) {
        if (address == null || address.isBlank()) {
            return "***";
        }
        var at = address.indexOf('@');
        if (at > 0) {
            var prefixLength = Math.min(2, at);
            return address.substring(0, prefixLength) + "***" + address.substring(at);
        }
        if (address.length() <= 7) {
            return "***";
        }
        return address.substring(0, 3) + "****" + address.substring(address.length() - 4);
    }
}
