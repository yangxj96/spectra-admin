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

package com.devops00.spectra.core.notification.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.Verify;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.common.exception.DataException;
import com.devops00.spectra.core.notification.javabean.from.NotificationTemplateActionFrom;
import com.devops00.spectra.core.notification.javabean.from.NotificationTemplatePageFrom;
import com.devops00.spectra.core.notification.javabean.from.NotificationTemplatePreviewFrom;
import com.devops00.spectra.core.notification.javabean.from.NotificationTemplateSaveFrom;
import com.devops00.spectra.core.notification.javabean.vo.NotificationTemplatePreviewVO;
import com.devops00.spectra.core.notification.javabean.vo.NotificationTemplateGroupVO;
import com.devops00.spectra.core.notification.javabean.vo.NotificationTemplateVO;
import com.devops00.spectra.core.notification.service.NotificationTemplateService;
import com.devops00.spectra.common.audit.Audit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    /**
     * 查询或获取目标数据（{@code page}）。
     */
    @Audit("'查询通知模板列表'")
    @GetMapping(version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'notification:template:read')")
    public IPage<NotificationTemplateVO> page(PageFrom page, NotificationTemplatePageFrom params) {
        return service.page(page, params);
    }

    /**
     * 查询或获取目标数据（{@code groupPage}）。
     */
    @Audit("'查询通知模板组列表'")
    @GetMapping(value = "/groups", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'notification:template:read')")
    public IPage<NotificationTemplateGroupVO> groupPage(PageFrom page, NotificationTemplatePageFrom params) {
        return service.groupPage(page, params);
    }

    /**
     * 查询或获取目标数据（{@code detail}）。
     */
    @Audit("'查询通知模板详情'")
    @GetMapping(value = "/{id}", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'notification:template:read')")
    public NotificationTemplateVO detail(@PathVariable UUID id) {
        return service.detail(id);
    }

    /**
     * 创建或构建目标数据（{@code create}）。
     */
    @Audit("'创建通知模板草稿'")
    @PostMapping(version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'notification:template:write')")
    public NotificationTemplateVO create(@Validated(Verify.Insert.class) @RequestBody NotificationTemplateSaveFrom params) {
        return service.create(params);
    }

    /**
     * 更新或推进目标状态（{@code update}）。
     */
    @Audit("'修改通知模板草稿'")
    @PutMapping(value = "/{id}", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'notification:template:write')")
    public NotificationTemplateVO update(@PathVariable UUID id,
                                         @Validated(Verify.Update.class) @RequestBody NotificationTemplateSaveFrom params) {
        if (!id.equals(params.getId())) {
            throw new DataException("路径模板 ID 与请求体不一致");
        }
        return service.update(params);
    }

    /**
     * 更新或推进目标状态（{@code publish}）。
     */
    @Audit("'发布通知模板'")
    @PostMapping(value = "/{id}/publish", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'notification:template:publish')")
    public void publish(@PathVariable UUID id, @Validated @RequestBody NotificationTemplateActionFrom params) {
        service.publish(id, params);
    }

    /**
     * 更新或推进目标状态（{@code disable}）。
     */
    @Audit("'停用通知模板'")
    @PostMapping(value = "/{id}/disable", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'notification:template:write')")
    public void disable(@PathVariable UUID id, @Validated @RequestBody NotificationTemplateActionFrom params) {
        service.disable(id, params);
    }

    /**
     * 更新或推进目标状态（{@code enable}）。
     */
    @Audit("'启用通知模板'")
    @PostMapping(value = "/{id}/enable", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'notification:template:write')")
    public void enable(@PathVariable UUID id, @Validated @RequestBody NotificationTemplateActionFrom params) {
        service.enable(id, params);
    }

    /**
     * 更新或推进目标状态（{@code archive}）。
     */
    @Audit("'归档通知模板'")
    @PostMapping(value = "/{id}/archive", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'notification:template:write')")
    public void archive(@PathVariable UUID id, @Validated @RequestBody NotificationTemplateActionFrom params) {
        service.archive(id, params);
    }

    /**
     * 处理内部业务逻辑（{@code versions}）。
     */
    @Audit("'查询通知模板版本历史'")
    @GetMapping(value = "/{id}/versions", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'notification:template:read')")
    public List<NotificationTemplateVO> versions(@PathVariable UUID id) {
        return service.versions(id);
    }

    /**
     * 处理内部业务逻辑（{@code preview}）。
     */
    @Audit("'预览通知模板'")
    @PostMapping(value = "/preview", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'notification:template:read')")
    public NotificationTemplatePreviewVO preview(@Validated @RequestBody NotificationTemplatePreviewFrom params) {
        return service.preview(params);
    }
}
