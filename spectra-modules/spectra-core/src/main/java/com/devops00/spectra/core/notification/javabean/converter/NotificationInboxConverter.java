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

package com.devops00.spectra.core.notification.javabean.converter;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devops00.spectra.framework.configure.mapstruct.GlobalMapperConfig;
import com.devops00.spectra.framework.configure.mapstruct.TimeMapper;
import com.devops00.spectra.core.notification.javabean.entity.NotificationInboxEntity;
import com.devops00.spectra.core.notification.javabean.vo.NotificationInboxVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 消息中心实体转换器。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/11
 */
@Mapper(uses = TimeMapper.class, config = GlobalMapperConfig.class)
public interface NotificationInboxConverter {

    /**
     * 将收件箱实体转换为当前消息中心响应对象。
     */
    NotificationInboxVO toVO(NotificationInboxEntity source);

    /**
     * 将收件箱分页实体转换为响应分页结果。
     */
    @Mapping(target = "pages", ignore = true)
    Page<NotificationInboxVO> toVOPage(Page<NotificationInboxEntity> source);

}
