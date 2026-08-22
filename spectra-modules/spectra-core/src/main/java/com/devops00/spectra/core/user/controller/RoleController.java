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

package com.devops00.spectra.core.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.common.constant.LogPrefix;
import com.devops00.spectra.common.exception.DataException;
import com.devops00.spectra.core.system.javabean.vo.MenuVO;
import com.devops00.spectra.core.user.javabean.from.RoleEditorSaveFrom;
import com.devops00.spectra.core.user.javabean.from.RolePageFrom;
import com.devops00.spectra.core.user.javabean.vo.RoleVO;
import com.devops00.spectra.core.user.service.RelRoleMenuService;
import com.devops00.spectra.core.user.service.RoleEditorService;
import com.devops00.spectra.core.user.service.RoleService;
import com.devops00.spectra.log.base.annotation.ULog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * 角色操作
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/11/11 00:00
 */
@Slf4j
@RestController
@RequestMapping("/role")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService bindService;

    private final RoleEditorService roleEditorService;

    private final RelRoleMenuService relRoleMenuService;

    @ULog("'提交角色编辑'")
    @PostMapping(value = "/editor", version = "1.0.0")
    @PreAuthorize("((#p0.id == null and hasPermission(null, 'role:create')) "
            + "or (#p0.id != null and hasPermission(null, 'role:update'))) "
            + "and hasPermission(null, 'role:grant') and hasPermission(null, 'role:assign')")
    public RoleVO saveEditor(@Validated @RequestBody RoleEditorSaveFrom params) {
        return roleEditorService.save(params);
    }

    @ULog("'启用角色'")
    @PutMapping(value = "/{id}/enable", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'role:disable')")
    public void enable(@PathVariable UUID id) {
        bindService.enable(id);
    }

    @ULog("'禁用角色'")
    @PutMapping(value = "/{id}/disable", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'role:disable')")
    public void disable(@PathVariable UUID id) {
        bindService.disable(id);
    }

    @ULog("'删除角色'")
    @DeleteMapping(value = "/{id}", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'role:delete')")
    public void deleteById(@PathVariable UUID id) {
        bindService.deleteById(id);
    }

    /* 查询部分 */

    @ULog("'分页查询角色列表'")
    @GetMapping(value = "/page", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'role:read')")
    public IPage<RoleVO> page(PageFrom page, RolePageFrom params) {
        return bindService.page(page, params);
    }

    @ULog("'查询角色列表'")
    @GetMapping(value = "/list", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'role:read')")
    public List<RoleVO> list() {
        return bindService.all();
    }

    @ULog("'查询角色详情'")
    @GetMapping(value = "/{id}", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'role:read')")
    public RoleVO detail(@PathVariable UUID id) {
        return bindService.detail(id);
    }

    /* 关联处理部分 */

    @ULog("'获取角色关联的菜单列表'")
    @GetMapping(value = "/{roleId}/menu", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'role:read')")
    public List<MenuVO> getRoleRelMenuByRoleId(@PathVariable UUID roleId) {
        try {
            return relRoleMenuService.get(roleId);
        } catch (Exception e) {
            log.error("{}获取角色关联的菜单列表出现错误,{}", LogPrefix.CORE.p(), e.getMessage(), e);
            throw new DataException("参数转换失败");
        }
    }

}
