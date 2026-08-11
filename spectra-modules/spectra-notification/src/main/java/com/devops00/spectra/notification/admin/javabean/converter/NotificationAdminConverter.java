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

package com.devops00.spectra.notification.admin.javabean.converter;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devops00.spectra.framework.configure.mapstruct.GlobalMapperConfig;
import com.devops00.spectra.framework.configure.mapstruct.TimeMapper;
import com.devops00.spectra.notification.admin.javabean.vo.NotificationDeliveryAdminVO;
import com.devops00.spectra.notification.admin.javabean.vo.NotificationRequestAdminVO;
import com.devops00.spectra.notification.admin.javabean.vo.NotificationTaskAdminVO;
import com.devops00.spectra.notification.dispatch.javabean.entity.NotificationDeliveryEntity;
import com.devops00.spectra.notification.dispatch.javabean.entity.NotificationTaskEntity;
import com.devops00.spectra.notification.request.javabean.entity.NotificationRequestEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/** 通知管理视图转换器；地址和错误信息始终经过脱敏。 */
@Mapper(uses = TimeMapper.class, config = GlobalMapperConfig.class)
public interface NotificationAdminConverter {

    NotificationRequestAdminVO toRequestVO(NotificationRequestEntity source);

    @Mapping(target = "records", expression = "java(source.getRecords().stream().map(this::toRequestVO).toList())")
    Page<NotificationRequestAdminVO> toRequestPage(Page<NotificationRequestEntity> source);

    @Mapping(target = "recipientAddress", source = "recipientAddress", qualifiedByName = "maskAddress")
    @Mapping(target = "lastError", source = "lastError", qualifiedByName = "maskText")
    NotificationTaskAdminVO toTaskVO(NotificationTaskEntity source);

    @Mapping(target = "records", expression = "java(source.getRecords().stream().map(this::toTaskVO).toList())")
    Page<NotificationTaskAdminVO> toTaskPage(Page<NotificationTaskEntity> source);

    @Mapping(target = "responseSummary", source = "responseSummary", qualifiedByName = "maskText")
    NotificationDeliveryAdminVO toDeliveryVO(NotificationDeliveryEntity source);

    @Mapping(target = "records", expression = "java(source.getRecords().stream().map(this::toDeliveryVO).toList())")
    Page<NotificationDeliveryAdminVO> toDeliveryPage(Page<NotificationDeliveryEntity> source);

    @Named("maskAddress")
    default String maskAddress(String value) {
        return value == null || value.isBlank() ? null : "[已加密]";
    }

    @Named("maskText")
    default String maskText(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        return value.replaceAll("(?i)[\\w.+-]+@[\\w.-]+\\.[a-z]{2,}", "[邮箱已脱敏]")
                .replaceAll("(?<!\\d)1\\d{10}(?!\\d)", "[手机号已脱敏]")
                .replaceAll("(?i)(code|captcha|token|secret|password)\\s*[:=]\\s*[^,; ]+", "$1=[敏感值已脱敏]");
    }
}
