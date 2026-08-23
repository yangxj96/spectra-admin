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

package com.devops00.spectra.notification.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.Verify;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.common.exception.DataException;
import com.devops00.spectra.notification.javabean.from.NotificationTemplateActionFrom;
import com.devops00.spectra.notification.javabean.from.NotificationTemplatePageFrom;
import com.devops00.spectra.notification.javabean.from.NotificationTemplatePreviewFrom;
import com.devops00.spectra.notification.javabean.from.NotificationTemplateSaveFrom;
import com.devops00.spectra.notification.javabean.vo.NotificationTemplatePreviewVO;
import com.devops00.spectra.notification.javabean.vo.NotificationTemplateVO;
import com.devops00.spectra.notification.service.NotificationTemplateService;
import com.devops00.spectra.log.base.annotation.ULog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * 通知模板管理接口。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/23
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/notification/admin/templates")
public class NotificationTemplateAdminController {

    private final NotificationTemplateService service;

    @ULog("'查询通知模板列表'")
    @GetMapping(version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'notification:template:read')")
    public IPage<NotificationTemplateVO> page(PageFrom page, NotificationTemplatePageFrom params) {
        return service.page(page, params);
    }

    @ULog("'查询通知模板详情'")
    @GetMapping(value = "/{id}", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'notification:template:read')")
    public NotificationTemplateVO detail(@PathVariable UUID id) {
        return service.detail(id);
    }

    @ULog("'创建通知模板草稿'")
    @PostMapping(version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'notification:template:write')")
    public NotificationTemplateVO create(@Validated(Verify.Insert.class) @RequestBody NotificationTemplateSaveFrom params) {
        return service.create(params);
    }

    @ULog("'修改通知模板草稿'")
    @PutMapping(value = "/{id}", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'notification:template:write')")
    public NotificationTemplateVO update(@PathVariable UUID id,
                                         @Validated(Verify.Update.class) @RequestBody NotificationTemplateSaveFrom params) {
        if (!id.equals(params.getId())) {
            throw new DataException("路径模板 ID 与请求体不一致");
        }
        return service.update(params);
    }

    @ULog("'发布通知模板'")
    @PostMapping(value = "/{id}/publish", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'notification:template:publish')")
    public void publish(@PathVariable UUID id, @Validated @RequestBody NotificationTemplateActionFrom params) {
        service.publish(id, params);
    }

    @ULog("'停用通知模板'")
    @PostMapping(value = "/{id}/disable", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'notification:template:write')")
    public void disable(@PathVariable UUID id, @Validated @RequestBody NotificationTemplateActionFrom params) {
        service.disable(id, params);
    }

    @ULog("'归档通知模板'")
    @PostMapping(value = "/{id}/archive", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'notification:template:write')")
    public void archive(@PathVariable UUID id, @Validated @RequestBody NotificationTemplateActionFrom params) {
        service.archive(id, params);
    }

    @ULog("'查询通知模板版本历史'")
    @GetMapping(value = "/{id}/versions", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'notification:template:read')")
    public List<NotificationTemplateVO> versions(@PathVariable UUID id) {
        return service.versions(id);
    }

    @ULog("'回滚通知模板'")
    @PostMapping(value = "/{id}/rollback", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'notification:template:publish')")
    public NotificationTemplateVO rollback(@PathVariable UUID id) {
        return service.rollback(id);
    }

    @ULog("'预览通知模板'")
    @PostMapping(value = "/preview", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'notification:template:read')")
    public NotificationTemplatePreviewVO preview(@Validated @RequestBody NotificationTemplatePreviewFrom params) {
        return service.preview(params);
    }
}
