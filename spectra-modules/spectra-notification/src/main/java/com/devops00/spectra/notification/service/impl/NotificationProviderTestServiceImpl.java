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

import com.github.f4b6a3.uuid.UuidCreator;
import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.framework.configure.mapstruct.TimeMapper;
import com.devops00.spectra.notification.configuration.NotificationPayloadProtector;
import com.devops00.spectra.notification.javabean.domain.ChannelSendResult;
import com.devops00.spectra.notification.javabean.domain.NotificationTaskStatus;
import com.devops00.spectra.notification.javabean.entity.NotificationTaskEntity;
import com.devops00.spectra.notification.javabean.from.NotificationProviderTestFrom;
import com.devops00.spectra.notification.javabean.vo.NotificationProviderTestVO;
import com.devops00.spectra.notification.provider.NotificationProviderRuntime;
import com.devops00.spectra.notification.service.NotificationProviderTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Provider 测试发送服务实现；测试任务只在内存中组装，不进入业务 Request/Task/Delivery 链路。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/23
 */
@Service
@RequiredArgsConstructor
public class NotificationProviderTestServiceImpl implements NotificationProviderTestService {

    /**
     * Provider 运行时协调器；复用健康门禁和 Provider 选择逻辑。
     */
    private final NotificationProviderRuntime runtime;

    /**
     * 通知地址保护器；测试目标不以明文传入 Provider。
     */
    private final NotificationPayloadProtector payloadProtector;

    /** 用户时区时间转换器。 */
    private final TimeMapper timeMapper;

    /**
     * 执行一次 Provider 测试发送。
     */
    @Override
    public NotificationProviderTestVO send(NotificationChannel channel, NotificationProviderTestFrom params) {
        validate(channel, params);
        var task = new NotificationTaskEntity();
        task.setId(UuidCreator.getTimeOrderedEpoch());
        task.setChannel(channel.name());
        task.setRecipientCiphertext(payloadProtector.protectAddress(params.getRecipientAddress().trim()));
        task.setTitle(params.getTitle().trim());
        task.setContent(params.getContent().trim());
        task.setStatus(NotificationTaskStatus.TEST.name());
        var result = runtime.send(channel, task);
        return NotificationProviderTestVO.builder()
                .channel(channel.name())
                .providerCode(result.providerCode())
                .status(result.status().name())
                .providerMessageId(result.providerMessageId())
                .summary(result.summary())
                .testedAt(timeMapper.toLocalDateTime(Instant.now()))
                .build();
    }

    private void validate(NotificationChannel channel, NotificationProviderTestFrom params) {
        if (channel == null || channel == NotificationChannel.IN_APP) {
            throw new DataSaveException("仅支持 SMS 或 EMAIL Provider 测试发送");
        }
        if (params == null) {
            throw new DataSaveException("Provider 测试发送参数不能为空");
        }
        if (!"SEND_TEST".equals(params.getConfirmation())) {
            throw new DataSaveException("Provider 测试发送必须输入确认词 SEND_TEST");
        }
        if (params.getRecipientAddress() == null || params.getRecipientAddress().isBlank()) {
            throw new DataSaveException("测试收件地址不能为空");
        }
        if (params.getTitle() == null || params.getTitle().isBlank()) {
            throw new DataSaveException("测试标题不能为空");
        }
        if (params.getContent() == null || params.getContent().isBlank()) {
            throw new DataSaveException("测试正文不能为空");
        }
    }
}
