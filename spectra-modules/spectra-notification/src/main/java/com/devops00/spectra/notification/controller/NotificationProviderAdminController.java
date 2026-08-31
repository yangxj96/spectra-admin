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

import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.common.audit.Audit;
import com.devops00.spectra.notification.javabean.domain.NotificationProviderHealth;
import com.devops00.spectra.notification.javabean.from.NotificationProviderSaveFrom;
import com.devops00.spectra.notification.javabean.from.NotificationProviderTestFrom;
import com.devops00.spectra.notification.javabean.vo.NotificationProviderVO;
import com.devops00.spectra.notification.javabean.vo.NotificationProviderTestVO;
import com.devops00.spectra.notification.service.NotificationProviderAdminService;
import com.devops00.spectra.notification.provider.NotificationProviderRuntime;
import com.devops00.spectra.notification.service.NotificationProviderTestService;
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

/**
 * 通知 Provider 配置管理接口；只返回脱敏配置，不返回 Secret。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/23
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/notification/admin/providers")
public class NotificationProviderAdminController {

    /**
     * Provider 配置服务。
     */
    private final NotificationProviderAdminService service;

    /**
     * Provider 运行时协调器。
     */
    private final NotificationProviderRuntime runtime;

    /**
     * Provider 测试发送服务。
     */
    private final NotificationProviderTestService testService;

    /**
     * 查询所有通知渠道 Provider 配置。
     */
    @Audit("'查询通知 Provider 配置'")
    @GetMapping(version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'notification:provider:read')")
    public List<NotificationProviderVO> list() {
        return service.list();
    }

    /**
     * 查询指定渠道 Provider 配置。
     */
    @Audit("'查询通知 Provider 详情'")
    @GetMapping(value = "/{channel}", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'notification:provider:read')")
    public NotificationProviderVO get(@PathVariable NotificationChannel channel) {
        return service.get(channel);
    }

    /**
     * 保存指定渠道 Provider 配置。
     */
    @Audit("'保存通知 Provider 配置'")
    @PutMapping(value = "/{channel}", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'notification:provider:configure')")
    public NotificationProviderVO modify(@PathVariable NotificationChannel channel,
                                         @Validated @RequestBody NotificationProviderSaveFrom params) {
        return service.modify(channel, params);
    }

    /**
     * 执行指定渠道健康检查；不返回任何供应商原始响应。
     */
    @Audit("'检查通知 Provider 健康状态'")
    @PostMapping(value = "/{channel}/health", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'notification:provider:configure')")
    public NotificationProviderHealth health(@PathVariable NotificationChannel channel) {
        return runtime.check(channel);
    }

    /**
     * 向明确指定的测试地址发送一次测试消息；不写入业务 Request/Task/Delivery。
     */
    @Audit("'测试通知 Provider 发送'")
    @PostMapping(value = "/{channel}/test", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'notification:provider:configure')")
    public NotificationProviderTestVO test(@PathVariable NotificationChannel channel,
                                           @Validated @RequestBody NotificationProviderTestFrom params) {
        return testService.send(channel, params);
    }
}
