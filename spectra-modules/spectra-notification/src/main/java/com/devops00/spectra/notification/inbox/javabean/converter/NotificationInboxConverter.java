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

package com.devops00.spectra.notification.inbox.javabean.converter;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devops00.spectra.framework.configure.mapstruct.GlobalMapperConfig;
import com.devops00.spectra.framework.configure.mapstruct.TimeMapper;
import com.devops00.spectra.notification.inbox.javabean.entity.NotificationInboxEntity;
import com.devops00.spectra.notification.inbox.javabean.vo.NotificationInboxVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** 消息中心实体转换器。 */
@Mapper(uses = TimeMapper.class, config = GlobalMapperConfig.class)
public interface NotificationInboxConverter {

    @Mapping(target = "type", expression = "java(toLegacyType(source.getPurpose()))")
    @Mapping(source = "senderUserId", target = "senderId")
    @Mapping(source = "recipientUserId", target = "receiverId")
    @Mapping(target = "isRead", expression = "java(source.getReadAt() != null)")
    NotificationInboxVO toVO(NotificationInboxEntity source);

    @Mapping(target = "pages", ignore = true)
    Page<NotificationInboxVO> toVOPage(Page<NotificationInboxEntity> source);

    default String toLegacyType(String purpose) {
        if (purpose == null) {
            return "system";
        }
        return switch (purpose) {
            case "WORKFLOW_TODO", "WORKFLOW_RESULT" -> "workflow";
            case "OA_NOTICE", "OA_REMINDER" -> "oa";
            case "INNER_MESSAGE" -> "inner_mail";
            case "SECURITY_ALERT" -> "security";
            default -> "system";
        };
    }
}
