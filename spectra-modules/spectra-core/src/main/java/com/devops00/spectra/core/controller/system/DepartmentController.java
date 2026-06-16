/*
 *  Copyright 2018-2025 yangxj96
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

package com.devops00.spectra.core.controller.system;

import com.devops00.spectra.common.base.Verify;
import com.devops00.spectra.core.javabean.system.from.DepartmentFrom;
import com.devops00.spectra.core.javabean.system.vo.DepartmentTreeVo;
import com.devops00.spectra.core.service.system.DepartmentService;
import com.devops00.spectra.log.base.annotation.ULog;
import org.jspecify.annotations.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/// 组织机构控制器
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/7/14
@RestController
@RequestMapping("/department")
public class DepartmentController {

    private final DepartmentService bindService;

    public DepartmentController(DepartmentService bindService) {
        this.bindService = bindService;
    }

    /**
     * 新增组织机构
     *
     * @param from 请求入参
     */
    @ULog("'新增组织机构'")
    @PostMapping
    @PreAuthorize("hasPermission(null ,'DEPT:INSERT')")
    public void created(@RequestBody @Validated(Verify.Insert.class) DepartmentFrom from) {
        bindService.created(from);
    }

    /**
     * 删除组织机构
     *
     * @param id 组织机构ID
     */
    @ULog("'新增组织机构'")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission(null ,'DEPT:INSERT')")
    public void deleteById(@PathVariable String id) {
        bindService.deleteById(id);
    }

    /**
     * 编辑组织机构
     *
     * @param from 请求入参
     */
    @ULog("'编辑组织机构'")
    @PutMapping
    @PreAuthorize("hasPermission(null ,'DEPT:INSERT')")
    public void modify(@RequestBody @Validated(Verify.Update.class) DepartmentFrom from) {
        bindService.modify(from);
    }

    /**
     * 组织机构树形结构
     *
     * @return 组织机构树形结构数组
     */
    @ULog("'获取组织机构树形列表'")
    @GetMapping("/tree")
    public @Nullable List<DepartmentTreeVo> tree() throws IllegalAccessException {
        return bindService.tree();
    }

}
