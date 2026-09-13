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

import com.devops00.spectra.core.system.javabean.from.ConfiguredBatchFrom;
import com.devops00.spectra.core.system.javabean.vo.ConfiguredVO;
import com.devops00.spectra.core.system.service.ConfiguredService;
import com.devops00.spectra.common.audit.Audit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 系统配置控制器
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/11/11 00:00
 */
@Slf4j
@RestController
@RequestMapping("/configured")
public class ConfiguredController {

    private final ConfiguredService bindService;

    public ConfiguredController(ConfiguredService bindService) {
        this.bindService = bindService;
    }

    /**
     * 查询系统配置表单。
     *
     * @return 按业务分类组织的系统配置项。
     */
    @Audit("'查询系统配置表单'")
    @GetMapping(value = "/settings", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'security:config:read')")
    public List<ConfiguredVO> settings() {
        return bindService.settings();
    }

    /**
     * 原子保存单个业务分类的系统配置表单。
     *
     * @param params 分类及配置项。
     */
    @Audit(value = "'批量修改系统配置'", captureArguments = false)
    @PutMapping(value = "/batch", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'security:config:update')")
    public void modifyBatch(@Validated @RequestBody ConfiguredBatchFrom params) {
        bindService.modifyBatch(params);
    }

}
