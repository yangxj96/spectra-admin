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

package io.github.yangxj96.spectra.core.system.controller;

import cn.dev33.satoken.annotation.SaCheckEL;
import cn.dev33.satoken.annotation.SaCheckLogin;
import io.github.yangxj96.spectra.common.annotation.ULog;
import io.github.yangxj96.spectra.common.base.Verify;
import io.github.yangxj96.spectra.core.system.javabean.from.OrganizationFrom;
import io.github.yangxj96.spectra.core.system.javabean.vo.OrganizationTreeVo;
import io.github.yangxj96.spectra.core.system.service.OrganizationService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 组织机构控制器
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/7/14
 */
@SaCheckLogin
@RestController
@RequestMapping("/organization")
public class OrganizationController {

    @Resource
    private OrganizationService bindService;

    /**
     * 组织机构树形结构
     *
     * @return 组织机构树形结构数组
     */
    @ULog("获取组织机构树形列表")
    @GetMapping("/tree")
    public List<OrganizationTreeVo> tree() {
        return bindService.tree();
    }

    /**
     * 新增组织机构
     *
     * @param from 请求入参
     */
    @ULog("新增组织机构")
    @PostMapping
    @SaCheckEL("@ss.hasPermission('ORGANIZATION:INSERT')")
    public void created(@RequestBody @Validated(Verify.Insert.class) OrganizationFrom from) {
        bindService.created(from);
    }

    /**
     * 删除组织机构
     *
     * @param id 组织机构ID
     */
    @ULog("新增组织机构")
    @DeleteMapping("/{id}")
    @SaCheckEL("@ss.hasPermission('ORGANIZATION:DELETE')")
    public void deleteById(@PathVariable String id) {
        bindService.deleteById(id);
    }

    /**
     * 编辑组织机构
     *
     * @param from 请求入参
     */
    @ULog("编辑组织机构")
    @PutMapping
    @SaCheckEL("@ss.hasPermission('ORGANIZATION:UPDATE')")
    public void modify(@RequestBody @Validated(Verify.Update.class) OrganizationFrom from) {
        bindService.modify(from);
    }

}
