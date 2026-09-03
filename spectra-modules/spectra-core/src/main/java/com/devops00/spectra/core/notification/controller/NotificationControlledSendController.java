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
import com.devops00.spectra.core.notification.javabean.from.NotificationControlledSendApplyFrom;
import com.devops00.spectra.core.notification.javabean.from.NotificationControlledSendFrom;
import com.devops00.spectra.core.notification.javabean.vo.NotificationControlledSendApplyVO;
import com.devops00.spectra.core.notification.javabean.vo.NotificationControlledSendPreviewVO;
import com.devops00.spectra.core.notification.service.NotificationControlledSendService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 通知中心受控发送接口；Preview 与 Apply 都必须经过权限和服务层数据范围校验。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/notification/admin/send")
public class NotificationControlledSendController {

    private final NotificationControlledSendService service;

    /**
     * 生成十分钟有效的一次性 Preview。
     */
    @Audit("'预览受控通知发送'")
    @PostMapping(value = "/preview", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'notification:send:preview')")
    public NotificationControlledSendPreviewVO preview(@Valid @RequestBody NotificationControlledSendFrom params) {
        return service.preview(params);
    }

    /**
     * 原子消费 Preview 并通过 NotificationGateway 入队。
     */
    @Audit("'应用受控通知发送'")
    @PostMapping(value = "/apply", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'notification:send:apply')")
    public NotificationControlledSendApplyVO apply(@Valid @RequestBody NotificationControlledSendApplyFrom params) {
        return service.apply(params);
    }
}
