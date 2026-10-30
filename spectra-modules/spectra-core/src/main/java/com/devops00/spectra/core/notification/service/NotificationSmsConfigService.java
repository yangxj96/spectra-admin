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

import com.devops00.spectra.core.notification.javabean.entity.NotificationSmsConfigEntity;
import com.devops00.spectra.core.notification.javabean.from.NotificationSmsConfigSaveFrom;
import com.devops00.spectra.core.notification.javabean.vo.NotificationSmsConfigVO;

import java.util.List;
import java.util.UUID;

/**
 * 短信配置管理服务。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/30
 */
public interface NotificationSmsConfigService {

    /**
     * 查询全部短信配置。
     *
     * @return 配置列表
     */
    List<NotificationSmsConfigVO> list();

    /**
     * 查询短信配置详情。
     *
     * @param id 配置 ID
     * @return 配置详情
     */
    NotificationSmsConfigVO detail(UUID id);

    /**
     * 创建短信配置。
     *
     * @param from 保存参数
     * @return 新配置 ID
     */
    UUID created(NotificationSmsConfigSaveFrom from);

    /**
     * 修改短信配置。
     *
     * @param id   配置 ID
     * @param from 保存参数
     */
    void modify(UUID id, NotificationSmsConfigSaveFrom from);

    /**
     * 删除短信配置。
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
     * 查询当前启用中的短信配置。
     *
     * @return 启用配置；不存在时返回 {@code null}
     */
    NotificationSmsConfigEntity active();
}
