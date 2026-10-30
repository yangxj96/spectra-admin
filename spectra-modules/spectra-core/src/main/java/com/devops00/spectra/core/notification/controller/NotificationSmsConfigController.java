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

import com.devops00.spectra.common.audit.Audit;
import com.devops00.spectra.core.notification.javabean.from.NotificationSmsConfigSaveFrom;
import com.devops00.spectra.core.notification.javabean.vo.NotificationSmsConfigVO;
import com.devops00.spectra.core.notification.service.NotificationSmsConfigService;
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

import java.util.List;
import java.util.UUID;

/**
 * 短信配置管理接口。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/30
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/notification/admin/sms-configs")
public class NotificationSmsConfigController {

    private final NotificationSmsConfigService service;

    /**
     * 查询短信配置列表。
     *
     * @return 配置列表
     */
    @Audit("'查询短信配置列表'")
    @GetMapping(value = "/list", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'notification:provider:read')")
    public List<NotificationSmsConfigVO> list() {
        return service.list();
    }

    /**
     * 查询短信配置详情。
     *
     * @param id 配置 ID
     * @return 配置详情
     */
    @Audit("'查询短信配置详情'")
    @GetMapping(value = "/{id}", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'notification:provider:read')")
    public NotificationSmsConfigVO detail(@PathVariable UUID id) {
        return service.detail(id);
    }

    /**
     * 创建短信配置。
     *
     * @param from 保存参数
     * @return 新配置 ID
     */
    @Audit("'创建短信配置'")
    @PostMapping(version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'notification:provider:configure')")
    public UUID created(@Validated @RequestBody NotificationSmsConfigSaveFrom from) {
        return service.created(from);
    }

    /**
     * 修改短信配置。
     *
     * @param id   配置 ID
     * @param from 保存参数
     */
    @Audit("'修改短信配置'")
    @PutMapping(value = "/{id}", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'notification:provider:configure')")
    public void modify(@PathVariable UUID id, @Validated @RequestBody NotificationSmsConfigSaveFrom from) {
        service.modify(id, from);
    }

    /**
     * 删除短信配置。
     *
     * @param id 配置 ID
     */
    @Audit("'删除短信配置'")
    @DeleteMapping(value = "/{id}", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'notification:provider:configure')")
    public void deleteById(@PathVariable UUID id) {
        service.deleteById(id);
    }

    /**
     * 启用短信配置。
     *
     * @param id 配置 ID
     */
    @Audit("'启用短信配置'")
    @PostMapping(value = "/{id}/activate", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'notification:provider:configure')")
    public void activate(@PathVariable UUID id) {
        service.activate(id);
    }
}
