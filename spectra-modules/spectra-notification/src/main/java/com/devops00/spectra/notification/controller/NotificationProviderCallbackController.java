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
import com.devops00.spectra.notification.javabean.vo.NotificationProviderCallbackVO;
import com.devops00.spectra.notification.service.NotificationProviderCallbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 外部 Provider 回执入口；接口匿名开放，但每个渠道必须使用 Provider Secret 验签。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/23
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/notification/provider/callback")
public class NotificationProviderCallbackController {

    /**
     * 回执处理服务。
     */
    private final NotificationProviderCallbackService service;

    /**
     * 接收一个渠道的外部 Provider 回执。
     */
    @PostMapping(value = "/{channel}", version = "1.0.0")
    public NotificationProviderCallbackVO callback(@PathVariable NotificationChannel channel,
                                                   @RequestHeader(name = "X-Notification-Signature", required = false) String signature,
                                                   @RequestBody String body) {
        return service.handle(channel, signature, body);
    }
}
