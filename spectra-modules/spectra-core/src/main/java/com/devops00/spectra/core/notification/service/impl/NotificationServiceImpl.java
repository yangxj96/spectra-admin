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

package com.devops00.spectra.core.notification.service.impl;

import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.common.notification.NotificationGateway;
import com.devops00.spectra.common.notification.NotificationReceipt;
import com.devops00.spectra.common.notification.NotificationRequest;
import com.devops00.spectra.common.notification.NotificationSendRequest;
import com.devops00.spectra.common.notification.NotificationService;
import com.devops00.spectra.common.notification.NotificationSystemActor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

/**
 * 快捷通知服务实现，将业务友好的请求转换为统一通知网关请求。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/24
 */
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationGateway notificationGateway;

    @Override
    public NotificationReceipt send(NotificationSendRequest request) {
        return send(request, null);
    }

    @Override
    public NotificationReceipt sendAsSystem(NotificationSendRequest request, NotificationSystemActor actor) {
        if (actor == null) {
            throw new AccessDeniedException("通知缺少系统运行身份");
        }
        return send(request, actor);
    }

    private NotificationReceipt send(NotificationSendRequest request, NotificationSystemActor actor) {
        if (request == null) {
            throw new DataSaveException("通知发送请求不能为空");
        }
        var gatewayRequest = new NotificationRequest(
                request.requestId(), request.idempotencyKey(), request.purpose(), request.channels(),
                request.recipientUserIds(), request.directAddresses(), request.templateGroupCode(), request.parameters(),
                request.sensitiveParameters(), request.businessType(), request.businessId(), request.sourceModule(),
                request.sourceDepartmentId(), request.scheduledAt(), request.expiresAt(), request.priority(), request.link());
        if (actor != null && !actor.permits(gatewayRequest)) {
            throw new AccessDeniedException("系统通知运行身份与来源、用途或收件人不匹配");
        }
        for (var channel : request.channels()) {
            var availability = notificationGateway.availability(channel);
            if (availability == null || !availability.available()) {
                var reason = availability == null ? "渠道状态未知" : availability.reason();
                throw new DataSaveException("通知渠道暂不可用: " + channel + "，" + reason);
            }
        }
        return actor == null
                ? notificationGateway.enqueue(gatewayRequest)
                : notificationGateway.enqueueAsSystem(gatewayRequest, actor);
    }
}
