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

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.core.system.javabean.from.ConfiguredFrom;
import com.devops00.spectra.core.system.javabean.from.ConfiguredPageFrom;
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
     * 修改系统配置
     * <p>
     * 只能修改值和说明
     *
     * @param params 修改参数入参实体
     */
    @Audit("'修改系统配置'")
    @PutMapping(version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'security:config:update')")
    public void modify(@Validated @RequestBody ConfiguredFrom params) {
        bindService.modify(params);
    }

    /**
     * 查询或获取目标数据（{@code page}）。
     */
    @Audit("'分页查询系统配置'")
    @GetMapping(value = "/page", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'security:config:read')")
    public IPage<ConfiguredVO> page(PageFrom page, ConfiguredPageFrom params) {
        return bindService.page(page, params);
    }
}
