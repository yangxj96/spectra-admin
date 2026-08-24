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

package com.devops00.spectra.notification.javabean.converter;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devops00.spectra.framework.configure.mapstruct.GlobalMapperConfig;
import com.devops00.spectra.framework.configure.mapstruct.TimeMapper;
import com.devops00.spectra.notification.javabean.entity.NotificationDeliveryEntity;
import com.devops00.spectra.notification.javabean.entity.NotificationRequestEntity;
import com.devops00.spectra.notification.javabean.entity.NotificationTaskEntity;
import com.devops00.spectra.notification.javabean.vo.NotificationDeliveryAdminVO;
import com.devops00.spectra.notification.javabean.vo.NotificationRequestAdminVO;
import com.devops00.spectra.notification.javabean.vo.NotificationTaskAdminVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Map;

/**
 * 通知管理视图转换器；地址和错误信息始终经过脱敏。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/11
 */
@Mapper(uses = TimeMapper.class, config = GlobalMapperConfig.class)
public interface NotificationAdminConverter {

    /**
     * 将通知请求实体转换为脱敏管理视图。
     */
    @Mapping(target = "templateCode", source = "templateGroupCode")
    NotificationRequestAdminVO toRequestVO(NotificationRequestEntity source);

    /**
     * 将通知请求分页实体转换为管理视图分页结果。
     */
    @Mapping(target = "records", expression = "java(source.getRecords().stream().map(this::toRequestVO).toList())")
    Page<NotificationRequestAdminVO> toRequestPage(Page<NotificationRequestEntity> source);

    /**
     * 将通知任务实体转换为脱敏管理视图。
     */
    @Mapping(target = "requestId", source = "notificationRequestId")
    @Mapping(target = "recipientUserId", source = "receiverUserId")
    @Mapping(target = "recipientAddress", source = "recipientMasked")
    @Mapping(target = "retryCount", source = "attemptCount")
    @Mapping(target = "lastError", source = "lastErrorCode", qualifiedByName = "maskText")
    NotificationTaskAdminVO toTaskVO(NotificationTaskEntity source);

    /**
     * 将通知任务分页实体转换为管理视图分页结果。
     */
    @Mapping(target = "records", expression = "java(source.getRecords().stream().map(this::toTaskVO).toList())")
    Page<NotificationTaskAdminVO> toTaskPage(Page<NotificationTaskEntity> source);

    /**
     * 将投递记录实体转换为脱敏管理视图。
     */
    @Mapping(target = "taskId", source = "notificationTaskId")
    @Mapping(target = "providerCode", source = "provider")
    @Mapping(target = "status", source = "resultStatus")
    @Mapping(target = "responseSummary", source = "responseSummary", qualifiedByName = "maskSummary")
    @Mapping(target = "errorMessage", source = "errorMessageSanitized", qualifiedByName = "maskText")
    @Mapping(target = "sentAt", source = "completedAt")
    NotificationDeliveryAdminVO toDeliveryVO(NotificationDeliveryEntity source);

    /**
     * 将投递记录分页实体转换为管理视图分页结果。
     */
    @Mapping(target = "records", expression = "java(source.getRecords().stream().map(this::toDeliveryVO).toList())")
    Page<NotificationDeliveryAdminVO> toDeliveryPage(Page<NotificationDeliveryEntity> source);

    /**
     * 将收件地址替换为固定脱敏标记。
     */
    @Named("maskAddress")
    default String maskAddress(String value) {
        return value == null || value.isBlank() ? null : "[已加密]";
    }

    /**
     * 对错误文本和供应商响应中的常见敏感值进行脱敏。
     */
    @Named("maskText")
    default String maskText(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        return value.replaceAll("(?i)[\\w.+-]+@[\\w.-]+\\.[a-z]{2,}", "[邮箱已脱敏]")
                .replaceAll("(?<!\\d)1\\d{10}(?!\\d)", "[手机号已脱敏]")
                .replaceAll("(?<!\\d)\\d{6}(?!\\d)", "[验证码已脱敏]")
                .replaceAll("(?i)(code|captcha|token|secret|password)\\s*[:=]\\s*[^,; ]+", "$1=[敏感值已脱敏]");
    }

    /**
     * 将结构化供应商响应摘要转换为脱敏文本。
     */
    @Named("maskSummary")
    default String maskSummary(Map<String, Object> value) {
        return value == null ? null : maskText(value.toString());
    }
}
