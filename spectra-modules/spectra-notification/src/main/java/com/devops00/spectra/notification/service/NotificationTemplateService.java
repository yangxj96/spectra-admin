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

    IPage<NotificationTemplateVO> page(PageFrom page, NotificationTemplatePageFrom params);

    NotificationTemplateVO detail(UUID id);

    NotificationTemplateVO create(NotificationTemplateSaveFrom params);

    NotificationTemplateVO update(NotificationTemplateSaveFrom params);

    void publish(UUID id, NotificationTemplateActionFrom params);

    void disable(UUID id, NotificationTemplateActionFrom params);

    void archive(UUID id, NotificationTemplateActionFrom params);

    List<NotificationTemplateVO> versions(UUID id);

    NotificationTemplateVO rollback(UUID id);

    NotificationTemplatePreviewVO preview(NotificationTemplatePreviewFrom params);
}
