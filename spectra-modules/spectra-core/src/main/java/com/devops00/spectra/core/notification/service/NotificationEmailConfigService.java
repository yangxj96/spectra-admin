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

import com.devops00.spectra.core.notification.javabean.entity.NotificationEmailConfigEntity;
import com.devops00.spectra.core.notification.javabean.from.NotificationEmailConfigSaveFrom;
import com.devops00.spectra.core.notification.javabean.vo.NotificationEmailConfigVO;

import java.util.List;
import java.util.UUID;

/**
 * 邮件配置管理服务。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/30
 */
public interface NotificationEmailConfigService {

    /**
     * 查询全部邮件配置。
     *
     * @return 配置列表
     */
    List<NotificationEmailConfigVO> list();

    /**
     * 查询邮件配置详情。
     *
     * @param id 配置 ID
     * @return 配置详情
     */
    NotificationEmailConfigVO detail(UUID id);

    /**
     * 创建邮件配置。
     *
     * @param from 保存参数
     * @return 新配置 ID
     */
    UUID created(NotificationEmailConfigSaveFrom from);

    /**
     * 修改邮件配置。
     *
     * @param id   配置 ID
     * @param from 保存参数
     */
    void modify(UUID id, NotificationEmailConfigSaveFrom from);

    /**
     * 删除邮件配置。
     *
     * @param id 配置 ID
     */
    void deleteById(UUID id);

    /**
     * 启用指定配置，并停用其他配置。
     *
     * @param id 配置 ID
     */
    void activate(UUID id);

    /**
     * 查询当前启用中的邮件配置。
     *
     * @return 启用配置；不存在时返回 {@code null}
     */
    NotificationEmailConfigEntity active();
}
