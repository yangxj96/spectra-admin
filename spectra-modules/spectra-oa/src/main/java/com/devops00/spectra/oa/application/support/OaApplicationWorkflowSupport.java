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

package com.devops00.spectra.oa.application.support;

import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.common.notification.NotificationPurpose;
import com.devops00.spectra.common.notification.NotificationSendRequest;
import com.devops00.spectra.common.notification.NotificationService;
import com.devops00.spectra.common.notification.NotificationTemplateCode;
import com.devops00.spectra.oa.application.javabean.entity.Application;
import com.devops00.spectra.oa.application.service.ApplicationService;
import com.devops00.spectra.security.base.holder.SecurityContextAccessor;
import com.devops00.spectra.workflow.api.ProcessInstanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/**
 * OA 申请流程的共用边界逻辑。
 *
 * <p>请假、采购和报销都需要执行相同的申请归属校验、流程终止和状态通知。
 * 这些逻辑依赖多个基础服务，因此集中在组件中，避免各业务服务各自维护一份实现。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/24
 */
@Component
@RequiredArgsConstructor
public class OaApplicationWorkflowSupport {

    private final ApplicationService applicationService;
    private final ProcessInstanceService processInstanceService;
    private final NotificationService notificationService;
    private final SecurityContextAccessor securityContextAccessor;

    /**
     * 查询申请并校验当前用户是申请人。
     */
    public Application requireApplicantApplication(UUID applicationId, String notFoundMessage) {
        var application = applicationService.require(applicationId);
        if (!Objects.equals(application.getApplicantId(), securityContextAccessor.currentUserId())) {
            throw new DataNotExistException(notFoundMessage);
        }
        return application;
    }

    /**
     * 将流程回调传入的业务 KEY 解析为申请实体。
     */
    public Application requireApplication(String businessKey) {
        try {
            if (!StringUtils.hasText(businessKey)) {
                throw new IllegalArgumentException("业务 KEY 为空");
            }
            return applicationService.require(UUID.fromString(businessKey));
        } catch (IllegalArgumentException exception) {
            throw new DataNotExistException("审批业务KEY无效: " + businessKey, exception);
        }
    }

    /**
     * 终止申请关联的流程实例。
     */
    public void terminateProcess(Application application, String reason) {
        if (StringUtils.hasText(application.getProcessInstanceId())) {
            processInstanceService.terminate(application.getProcessInstanceId(), reason);
        }
    }

    /**
     * 发送申请状态通知。
     *
     * @param typeCode OA 业务类型编码，例如 {@code leave}
     */
    public void sendNotification(Application application, String typeCode, String title, String content) {
        var normalizedTypeCode = typeCode.toLowerCase(Locale.ROOT);
        notificationService.send(NotificationSendRequest.inApp("oa:" + normalizedTypeCode + ":" + application.getBizId() + ":" + title,
                NotificationPurpose.OA_NOTICE, List.of(application.getApplicantId()), NotificationTemplateCode.OA_APPLICATION_STATUS)
                .parameter("title", title)
                .parameter("content", content)
                .businessReference("OA_" + normalizedTypeCode.toUpperCase(Locale.ROOT), application.getBizId().toString())
                .sourceModule("OA")
                .link("/oa/" + normalizedTypeCode + "/" + application.getBizId())
                .build());
    }
}
