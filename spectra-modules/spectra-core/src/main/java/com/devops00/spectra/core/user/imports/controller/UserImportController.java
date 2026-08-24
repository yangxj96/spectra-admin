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

package com.devops00.spectra.core.user.imports.controller;

import com.devops00.spectra.core.user.imports.javabean.from.UserImportApplyFrom;
import com.devops00.spectra.core.user.imports.javabean.from.UserImportPreviewFrom;
import com.devops00.spectra.core.user.imports.javabean.vo.UserImportRowVO;
import com.devops00.spectra.core.user.imports.javabean.vo.UserImportTaskVO;
import com.devops00.spectra.core.user.imports.service.UserImportService;
import com.devops00.spectra.log.base.annotation.ULog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * 用户批量导入 Preview/Apply 入口。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/21
 */
@Slf4j
@RestController
@RequestMapping("/user/imports")
@RequiredArgsConstructor
public class UserImportController {

    private final UserImportService userImportService;

    /**
     * 处理内部业务逻辑（{@code preview}）。
     */
    @ULog("'预览用户批量导入'")
    @PostMapping(value = "/preview", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'user:create') and hasPermission(null, 'role:assign')")
    public UserImportTaskVO preview(@Validated @RequestBody UserImportPreviewFrom params) {
        return userImportService.preview(params);
    }

    /**
     * 查询或获取目标数据（{@code detail}）。
     */
    @ULog("'查询用户批量导入任务'")
    @GetMapping(value = "/{id}", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'user:read')")
    public UserImportTaskVO detail(@PathVariable UUID id) {
        return userImportService.detail(id);
    }

    /**
     * 处理内部业务逻辑（{@code errors}）。
     */
    @ULog("'查询用户批量导入错误'")
    @GetMapping(value = "/{id}/errors", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'user:read')")
    public List<UserImportRowVO> errors(@PathVariable UUID id) {
        return userImportService.errors(id);
    }

    /**
     * 更新或推进目标状态（{@code apply}）。
     */
    @ULog("'应用用户批量导入'")
    @PostMapping(value = "/{id}/apply", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'user:create') and hasPermission(null, 'role:assign')")
    public UserImportTaskVO apply(@PathVariable UUID id, @Validated @RequestBody UserImportApplyFrom params) {
        return userImportService.apply(id, params);
    }
}
