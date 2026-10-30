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
import com.devops00.spectra.common.audit.Audit;
import com.devops00.spectra.core.notification.javabean.from.NotificationTemplateEnabledFrom;
import com.devops00.spectra.core.notification.javabean.from.NotificationTemplateQueryFrom;
import com.devops00.spectra.core.notification.javabean.from.NotificationTemplateSaveFrom;
import com.devops00.spectra.core.notification.javabean.vo.NotificationTemplateVO;
import com.devops00.spectra.core.notification.service.NotificationTemplateService;
import com.devops00.spectra.framework.persistence.pagination.PageFrom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * 通知模板管理接口。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/30
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/notification/admin/templates")
public class NotificationTemplateAdminController {

    private final NotificationTemplateService service;

    /**
     * 分页查询模板。
     *
     * @param page   分页参数
     * @param params 查询条件
     * @return 模板分页
     */
    @Audit("'查询通知模板列表'")
    @GetMapping(value = "/page", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'notification:template:read')")
    public IPage<NotificationTemplateVO> page(PageFrom page, NotificationTemplateQueryFrom params) {
        return service.page(page, params);
    }

    /**
     * 查询模板详情。
     *
     * @param id 模板 ID
     * @return 模板详情
     */
    @Audit("'查询通知模板详情'")
    @GetMapping(value = "/{id}", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'notification:template:read')")
    public NotificationTemplateVO detail(@PathVariable UUID id) {
        return service.detail(id);
    }

    /**
     * 创建模板。
     *
     * @param from 保存参数
     * @return 新模板 ID
     */
    @Audit("'创建通知模板'")
    @PostMapping(version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'notification:template:write')")
    public UUID created(@Validated @RequestBody NotificationTemplateSaveFrom from) {
        return service.created(from);
    }

    /**
     * 修改模板。
     *
     * @param id   模板 ID
     * @param from 保存参数
     */
    @Audit("'修改通知模板'")
    @PutMapping(value = "/{id}", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'notification:template:write')")
    public void modify(@PathVariable UUID id, @Validated @RequestBody NotificationTemplateSaveFrom from) {
        service.modify(id, from);
    }

    /**
     * 删除模板。
     *
     * @param id 模板 ID
     */
    @Audit("'删除通知模板'")
    @DeleteMapping(value = "/{id}", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'notification:template:write')")
    public void deleteById(@PathVariable UUID id) {
        service.deleteById(id);
    }

    /**
     * 启用或停用模板。
     *
     * @param id   模板 ID
     * @param from 启停参数
     */
    @Audit("'修改通知模板启用状态'")
    @PutMapping(value = "/{id}/enabled", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'notification:template:write')")
    public void changeEnabled(@PathVariable UUID id, @Validated @RequestBody NotificationTemplateEnabledFrom from) {
        service.changeEnabled(id, from.getEnabled());
    }
}
