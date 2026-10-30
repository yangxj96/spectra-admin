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

package com.devops00.spectra.core.notification.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.core.notification.javabean.entity.NotificationTemplateEntity;
import com.devops00.spectra.core.notification.javabean.from.NotificationTemplateQueryFrom;
import com.devops00.spectra.core.notification.javabean.from.NotificationTemplateSaveFrom;
import com.devops00.spectra.core.notification.javabean.vo.NotificationTemplateVO;
import com.devops00.spectra.framework.persistence.pagination.PageFrom;

import java.util.UUID;

/**
 * 通知模板管理服务。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/30
 */
public interface NotificationTemplateService {

    /**
     * 分页查询模板。
     *
     * @param page  分页参数
     * @param query 查询条件
     * @return 模板分页
     */
    IPage<NotificationTemplateVO> page(PageFrom page, NotificationTemplateQueryFrom query);

    /**
     * 查询模板详情。
     *
     * @param id 模板 ID
     * @return 模板详情
     */
    NotificationTemplateVO detail(UUID id);

    /**
     * 创建模板。
     *
     * @param from 保存参数
     * @return 新模板 ID
     */
    UUID created(NotificationTemplateSaveFrom from);

    /**
     * 修改模板。
     *
     * @param id   模板 ID
     * @param from 保存参数
     */
    void modify(UUID id, NotificationTemplateSaveFrom from);

    /**
     * 删除模板。
     *
     * @param id 模板 ID
     */
    void deleteById(UUID id);

    /**
     * 启用或停用模板。
     *
     * @param id      模板 ID
     * @param enabled 是否启用
     */
    void changeEnabled(UUID id, boolean enabled);

    /**
     * 按编码和渠道获取启用中的模板。
     *
     * @param code    模板编码
     * @param channel 发送渠道
     * @return 模板实体
     */
    NotificationTemplateEntity require(String code, NotificationChannel channel);
}
