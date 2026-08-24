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

package com.devops00.spectra.notification.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.notification.javabean.from.NotificationTemplateActionFrom;
import com.devops00.spectra.notification.javabean.from.NotificationTemplatePageFrom;
import com.devops00.spectra.notification.javabean.from.NotificationTemplatePreviewFrom;
import com.devops00.spectra.notification.javabean.from.NotificationTemplateSaveFrom;
import com.devops00.spectra.notification.javabean.vo.NotificationTemplatePreviewVO;
import com.devops00.spectra.notification.javabean.vo.NotificationTemplateVO;

import java.util.List;
import java.util.UUID;

/**
 * 通知模板生命周期服务。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/23
 */
public interface NotificationTemplateService {

    /**
     * 查询或获取目标数据（{@code page}）。
     */
    IPage<NotificationTemplateVO> page(PageFrom page, NotificationTemplatePageFrom params);

    /**
     * 查询或获取目标数据（{@code detail}）。
     */
    NotificationTemplateVO detail(UUID id);

    /**
     * 创建或构建目标数据（{@code create}）。
     */
    NotificationTemplateVO create(NotificationTemplateSaveFrom params);

    /**
     * 转换、解析或规范化数据（{@code copy}）。
     */
    NotificationTemplateVO copy(UUID id);

    /**
     * 更新或推进目标状态（{@code update}）。
     */
    NotificationTemplateVO update(NotificationTemplateSaveFrom params);

    /**
     * 更新或推进目标状态（{@code publish}）。
     */
    void publish(UUID id, NotificationTemplateActionFrom params);

    /**
     * 更新或推进目标状态（{@code disable}）。
     */
    void disable(UUID id, NotificationTemplateActionFrom params);

    /**
     * 更新或推进目标状态（{@code archive}）。
     */
    void archive(UUID id, NotificationTemplateActionFrom params);

    /**
     * 处理内部业务逻辑（{@code versions}）。
     */
    List<NotificationTemplateVO> versions(UUID id);

    /**
     * 更新或推进目标状态（{@code rollback}）。
     */
    NotificationTemplateVO rollback(UUID id);

    /**
     * 处理内部业务逻辑（{@code preview}）。
     */
    NotificationTemplatePreviewVO preview(NotificationTemplatePreviewFrom params);
}
