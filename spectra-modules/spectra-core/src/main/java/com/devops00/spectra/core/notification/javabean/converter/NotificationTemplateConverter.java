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
import com.devops00.spectra.core.notification.javabean.entity.NotificationTemplateEntity;
import com.devops00.spectra.core.notification.javabean.vo.NotificationTemplateVO;
import org.mapstruct.Mapper;

/**
 * 通知模板管理视图转换器。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/23
 */
@Mapper(uses = TimeMapper.class, config = GlobalMapperConfig.class)
public interface NotificationTemplateConverter {

    /**
     * 将模板实体转换为管理视图。
     */
    NotificationTemplateVO toVO(NotificationTemplateEntity source);

    /**
     * 将模板实体分页转换为管理视图分页。
     */
    Page<NotificationTemplateVO> toPage(Page<NotificationTemplateEntity> source);
}
