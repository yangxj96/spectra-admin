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
import com.devops00.spectra.core.system.javabean.vo.SystemBootstrapVO;
import com.devops00.spectra.core.system.service.SystemBootstrapService;
import com.devops00.spectra.common.audit.Audit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Web 端启动阶段的公开配置接口。 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/system")
public class SystemBootstrapController {

    private final SystemBootstrapService systemBootstrapService;

    /**
     * 获取 Web 端启动阶段所需的公开配置。
     *
     * @return 启动配置
     */
    @Audit("'获取系统启动配置'")
    @Encrypt(response = false)
    @GetMapping(value = "/bootstrap", version = "1.0.0")
    @PreAuthorize("permitAll()")
    public SystemBootstrapVO get() {
        return systemBootstrapService.get();
    }
}
