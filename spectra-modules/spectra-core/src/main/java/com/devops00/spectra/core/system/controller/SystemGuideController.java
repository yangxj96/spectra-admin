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

package com.devops00.spectra.core.system.controller;

import com.devops00.spectra.common.annotation.Encrypt;
import com.devops00.spectra.core.security.initialization.javabean.from.SystemGuideCompleteFrom;
import com.devops00.spectra.core.security.initialization.javabean.vo.SystemGuideStatusVO;
import com.devops00.spectra.core.security.initialization.service.SystemGuideService;
import com.devops00.spectra.common.audit.Audit;
import com.devops00.spectra.common.audit.AuditCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** DEV_OPS 首次进入系统时的设置引导接口。 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/guide")
public class SystemGuideController {

    private final SystemGuideService systemGuideService;

    /**
     * 查询当前登录用户是否需要完成系统设置引导。
     *
     * @return 引导状态
     */
    @Audit("'查询系统设置引导状态'")
    @Encrypt(response = false)
    @GetMapping(value = "/status", version = "1.0.0")
    @PreAuthorize("isAuthenticated()")
    public SystemGuideStatusVO status() {
        log.debug("查询系统设置引导状态");
        return systemGuideService.status();
    }

    /**
     * 保存系统设置引导配置并完成引导。
     *
     * @param from 引导配置
     */
    @Audit(value = "'完成系统设置引导'", category = AuditCategory.SECURITY)
    @Encrypt(response = false)
    @PostMapping(value = "/complete", version = "1.0.0")
    @PreAuthorize("hasRole('ROLE_DEV_OPS')")
    public void complete(@Validated @RequestBody SystemGuideCompleteFrom from) {
        log.debug("完成系统设置引导，cryptoEnabled={}, notificationEnabled={}", from.getCryptoEnabled(),
                from.getNotificationEnabled());
        systemGuideService.complete(from);
    }
}
