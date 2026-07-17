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

package com.devops00.spectra.workflow.controller;

import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.log.base.annotation.ULog;
import com.devops00.spectra.workflow.javabean.from.FormDefinitionSaveFrom;
import com.devops00.spectra.workflow.javabean.from.FormPageFrom;
import com.devops00.spectra.workflow.javabean.from.FormVersionSaveFrom;
import com.devops00.spectra.workflow.javabean.vo.FormDefinitionVO;
import com.devops00.spectra.workflow.javabean.vo.FormVersionVO;
import com.devops00.spectra.workflow.service.FormDefinitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/// 工作流-表单定义接口
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/7/17
@Slf4j
@RestController
@RequestMapping("/workflow/form-definitions")
@RequiredArgsConstructor
public class FormDefinitionController {

    private final FormDefinitionService formDefinitionService;

    /// 分页查询表单列表
    @ULog("'查询表单列表'")
    @GetMapping(value = "", version = "1.0.0+")
    @PreAuthorize("isAuthenticated()")
    public Object page(PageFrom page, FormPageFrom params) {
        return formDefinitionService.page(page, params);
    }

    /// 查询表单详情（含当前版本内容）
    @ULog("'查询表单详情'")
    @GetMapping(value = "/{id}", version = "1.0.0+")
    @PreAuthorize("isAuthenticated()")
    public FormDefinitionVO getDetail(@PathVariable UUID id) {
        return formDefinitionService.getDetail(id);
    }

    /// 创建表单（同时创建版本1）
    @ULog("'创建表单定义'")
    @PostMapping(value = "", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'WF_FORM:INSERT')")
    public void create(@RequestBody FormDefinitionSaveFrom from) {
        formDefinitionService.create(from);
    }

    /// 更新表单元数据
    @ULog("'更新表单定义'")
    @PutMapping(value = "/{id}", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'WF_FORM:UPDATE')")
    public void update(@PathVariable UUID id, @RequestBody FormDefinitionSaveFrom from) {
        formDefinitionService.update(id, from);
    }

    /// 删除表单（级联删除版本）
    @ULog("'删除表单定义'")
    @DeleteMapping(value = "/{id}", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'WF_FORM:DELETE')")
    public void remove(@PathVariable UUID id) {
        formDefinitionService.remove(id);
    }

    /// 保存新版本（版本号自增）
    @ULog("'保存表单新版本'")
    @PostMapping(value = "/{id}/versions", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'WF_FORM:INSERT')")
    public void saveVersion(@PathVariable UUID id, @RequestBody FormVersionSaveFrom from) {
        formDefinitionService.saveVersion(id, from);
    }

    /// 查询版本历史
    @ULog("'查询表单版本历史'")
    @GetMapping(value = "/{id}/versions", version = "1.0.0+")
    @PreAuthorize("isAuthenticated()")
    public List<FormVersionVO> getVersions(@PathVariable UUID id) {
        return formDefinitionService.getVersions(id);
    }

    /// 查询指定版本详情
    @ULog("'查询表单版本详情'")
    @GetMapping(value = "/{id}/versions/{version}", version = "1.0.0+")
    @PreAuthorize("isAuthenticated()")
    public FormVersionVO getVersion(@PathVariable UUID id, @PathVariable Integer version) {
        return formDefinitionService.getVersion(id, version);
    }

}
