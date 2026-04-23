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

package com.devops00.spectra.core.controller.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.Verify;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.common.constant.LogPrefix;
import com.devops00.spectra.core.configure.ulog.annotation.ULog;
import com.devops00.spectra.core.javabean.system.vo.MenuVO;
import com.devops00.spectra.core.javabean.user.from.RoleAuthorityFrom;
import com.devops00.spectra.core.javabean.user.from.RoleFrom;
import com.devops00.spectra.core.javabean.user.from.RoleMenuFrom;
import com.devops00.spectra.core.javabean.user.from.RolePageFrom;
import com.devops00.spectra.core.javabean.user.vo.AuthorityVO;
import com.devops00.spectra.core.javabean.user.vo.RoleVO;
import com.devops00.spectra.core.service.user.RelRoleAuthorityService;
import com.devops00.spectra.core.service.user.RelRoleMenuService;
import com.devops00.spectra.core.service.user.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/// 角色操作
///
/// @author Jack Young
/// @version 1.0
/// @since 2025-11-11
@Slf4j
@RestController
@RequestMapping("/role")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService bindService;

    private final RelRoleMenuService relRoleMenuService;

    private final RelRoleAuthorityService relRoleAuthorityService;

    @ULog("创建角色")
    @PostMapping
    @PreAuthorize("hasPermission(null ,'ROLE:INSERT')")
    public void created(@Validated(Verify.Insert.class) @RequestBody RoleFrom params) {
        bindService.created(params);
    }

    @ULog("删除角色")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission(null ,'ROLE:DELETE')")
    public void delete(@PathVariable UUID id) {
        try {
            bindService.delete(id);
        } catch (NumberFormatException e) {
            log.error("ID转换异常", e);
        }
    }

    @ULog("修改角色")
    @PutMapping
    @PreAuthorize("hasPermission(null ,'ROLE:UPDATE')")
    public void modify(@Validated(Verify.Update.class) @RequestBody RoleFrom params) {
        bindService.modify(params);
    }

    /* 查询部分 */

    @ULog("分页查询角色列表")
    @GetMapping("/page")
    public IPage<RoleVO> page(PageFrom page, RolePageFrom params) {
        return bindService.page(page, params);
    }

    @ULog("查询角色列表")
    @GetMapping("/list")
    public List<RoleVO> list() {
        return bindService.all();
    }

    /* 关联处理部分 */

    @ULog("获取角色关联的权限列表")
    @GetMapping("/{roleId}/authority")
    public List<AuthorityVO> getRoleRelAuthorityByRoleId(@PathVariable UUID roleId) {
        try {
            return relRoleAuthorityService.get(roleId);
        } catch (Exception e) {
            log.error("{}获取角色关联的权限列表出现错误,{}", LogPrefix.CORE.p(), e.getMessage(), e);
            throw new IllegalArgumentException("参数转换失败");
        }
    }

    @ULog("获取角色关联的菜单列表")
    @GetMapping("/{roleId}/menu")
    public List<MenuVO> getRoleRelMenuByRoleId(@PathVariable UUID roleId) {
        try {
            return relRoleMenuService.get(roleId);
        } catch (Exception e) {
            log.error("{}获取角色关联的菜单列表出现错误,{}", LogPrefix.CORE.p(), e.getMessage(), e);
            throw new IllegalArgumentException("参数转换失败");
        }
    }

    @ULog("保存角色关联的权限列表")
    @PutMapping("/{roleId}/authorities")
    @PreAuthorize("hasPermission(null ,'ROLE:UPDATE')")
    public void saveRoleRelAuthorityByRoleId(@PathVariable String roleId, @Validated @RequestBody RoleAuthorityFrom from) {
        try {
            relRoleAuthorityService.grant(UUID.fromString(roleId), from);
        } catch (Exception e) {
            log.error("{}保存角色关联的权限列表出现错误,{}", LogPrefix.CORE.p(), e.getMessage(), e);
            throw new IllegalArgumentException("参数转换失败");
        }
    }

    @ULog("保存角色关联的菜单列表")
    @PutMapping("/{roleId}/menus")
    @PreAuthorize("hasPermission(null ,'ROLE:UPDATE')")
    public void saveRoleRelMenuByRoleId(@PathVariable UUID roleId, @Validated @RequestBody RoleMenuFrom from) {
        try {
            relRoleMenuService.grant(roleId, from);
        } catch (Exception e) {
            log.error("{}保存角色关联的菜单列表出现错误,{}", LogPrefix.CORE.p(), e.getMessage(), e);
            throw new IllegalArgumentException("参数转换失败");
        }
    }


}
