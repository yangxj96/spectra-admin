/*
 *  Copyright 2025 yangxj96.com
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
 *
 */

package com.yangxj96.spectra.service.auth.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yangxj96.spectra.common.annotation.ULog;
import com.yangxj96.spectra.common.base.Verify;
import com.yangxj96.spectra.common.base.javabean.from.PageFrom;
import com.yangxj96.spectra.service.auth.javabean.from.RoleFrom;
import com.yangxj96.spectra.service.auth.javabean.from.RolePageFrom;
import com.yangxj96.spectra.service.auth.javabean.vo.RoleVO;
import com.yangxj96.spectra.service.auth.service.PermissionService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限操作相关
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
@RestController
@RequestMapping("/permission")
public class PermissionController {

    @Resource
    private PermissionService bindService;

    /**
     * 分页查询角色信息
     *
     * @param page   分页信息,页码和每页数量
     * @param params 角色分页入参
     * @return 分页结果
     */
    @ULog("分页查询角色列表")
    @GetMapping("/pageRole")
    public IPage<RoleVO> pageRole(PageFrom page, RolePageFrom params) {
        return bindService.pageRole(page, params);
    }

    /**
     * 查询角色列表
     *
     * @return 角色列表
     */
    @ULog("查询角色列表")
    @GetMapping("/listRole")
    public List<RoleVO> listRole() {
        return bindService.listRole();
    }

    /**
     * 创建角色
     *
     * @param params 角色实体
     */
    @ULog("创建角色")
    @PostMapping("/createdRole")
    public void createdRole(@Validated(Verify.Insert.class) @RequestBody RoleFrom params) {
        bindService.createdRole(params);
    }

    /**
     * 修改角色信息
     *
     * @param params 角色实体
     */
    @ULog("修改角色信息")
    @PutMapping("/modifyRole")
    public void modifyRole(@Validated(Verify.Update.class) @RequestBody RoleFrom params) {
        bindService.modifyRole(params);
    }

}
