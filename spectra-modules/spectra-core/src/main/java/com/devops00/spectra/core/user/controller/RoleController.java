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
import com.devops00.spectra.common.base.Verify;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.common.constant.LogPrefix;
import com.devops00.spectra.common.exception.DataException;
import com.devops00.spectra.core.system.javabean.vo.MenuVO;
import com.devops00.spectra.core.user.javabean.from.RoleAuthorityFrom;
import com.devops00.spectra.core.user.javabean.from.RoleFrom;
import com.devops00.spectra.core.user.javabean.from.RoleMenuFrom;
import com.devops00.spectra.core.user.javabean.from.RolePageFrom;
import com.devops00.spectra.core.user.javabean.vo.AuthorityVO;
import com.devops00.spectra.core.user.javabean.vo.RoleVO;
import com.devops00.spectra.core.user.service.RelRoleAuthorityService;
import com.devops00.spectra.core.user.service.RelRoleMenuService;
import com.devops00.spectra.core.user.service.RoleService;
import com.devops00.spectra.log.base.annotation.ULog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/// 角色操作
///
/// @author yangxj96
/// @version 1.0
/// @since 2025/11/11 00:00
@Slf4j
@RestController
@RequestMapping("/role")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService bindService;

    private final RelRoleMenuService relRoleMenuService;

    private final RelRoleAuthorityService relRoleAuthorityService;

    @ULog("'创建角色'")
    @PostMapping(version = "1.0.0+")
    @PreAuthorize("hasPermission(null ,'ROLE:INSERT')")
    public void created(@Validated(Verify.Insert.class) @RequestBody RoleFrom params) {
        bindService.created(params);
    }

    @ULog("'删除角色'")
    @DeleteMapping(value = "/{id}", version = "1.0.0+")
    @PreAuthorize("hasPermission(null ,'ROLE:DELETE')")
    public void deleteById(@PathVariable UUID id) {
        try {
            bindService.deleteById(id);
        } catch (NumberFormatException e) {
            log.error("ID转换异常", e);
        }
    }

    @ULog("'修改角色'")
    @PutMapping(version = "1.0.0+")
    @PreAuthorize("hasPermission(null ,'ROLE:UPDATE')")
    public void modify(@Validated(Verify.Update.class) @RequestBody RoleFrom params) {
        bindService.modify(params);
    }

    /* 查询部分 */

    @ULog("'分页查询角色列表'")
    @GetMapping(value = "/page", version = "1.0.0+")
    @PreAuthorize("hasPermission(null ,'ROLE:QUERY')")
    public IPage<RoleVO> page(PageFrom page, RolePageFrom params) {
        return bindService.page(page, params);
    }

    @ULog("'查询角色列表'")
    @GetMapping(value = "/list", version = "1.0.0+")
    @PreAuthorize("hasPermission(null ,'ROLE:QUERY')")
    public List<RoleVO> list() {
        return bindService.all();
    }

    /* 关联处理部分 */

    @ULog("'获取角色关联的权限列表'")
    @GetMapping(value = "/{roleId}/authority", version = "1.0.0+")
    @PreAuthorize("hasPermission(null ,'ROLE:QUERY')")
    public List<AuthorityVO> getRoleRelAuthorityByRoleId(@PathVariable UUID roleId) {
        try {
            return relRoleAuthorityService.get(roleId);
        } catch (Exception e) {
            log.error("{}获取角色关联的权限列表出现错误,{}", LogPrefix.CORE.p(), e.getMessage(), e);
            throw new DataException("参数转换失败");
        }
    }

    @ULog("'获取角色关联的菜单列表'")
    @GetMapping(value = "/{roleId}/menu", version = "1.0.0+")
    @PreAuthorize("hasPermission(null ,'ROLE:QUERY')")
    public List<MenuVO> getRoleRelMenuByRoleId(@PathVariable UUID roleId) {
        try {
            return relRoleMenuService.get(roleId);
        } catch (Exception e) {
            log.error("{}获取角色关联的菜单列表出现错误,{}", LogPrefix.CORE.p(), e.getMessage(), e);
            throw new DataException("参数转换失败");
        }
    }

    @ULog("'保存角色关联的权限列表'")
    @PutMapping(value = "/{roleId}/authorities", version = "1.0.0+")
    @PreAuthorize("hasPermission(null ,'ROLE:UPDATE')")
    public void saveRoleRelAuthorityByRoleId(@PathVariable UUID roleId, @Validated @RequestBody RoleAuthorityFrom from) {
        try {
            relRoleAuthorityService.grant(roleId, from);
        } catch (Exception e) {
            log.error("{}保存角色关联的权限列表出现错误,{}", LogPrefix.CORE.p(), e.getMessage(), e);
            throw new DataException("参数转换失败");
        }
    }

    @ULog("'保存角色关联的菜单列表'")
    @PutMapping(value = "/{roleId}/menus", version = "1.0.0+")
    @PreAuthorize("hasPermission(null ,'ROLE:UPDATE')")
    public void saveRoleRelMenuByRoleId(@PathVariable UUID roleId, @Validated @RequestBody RoleMenuFrom from) {
        try {
            relRoleMenuService.grant(roleId, from);
        } catch (Exception e) {
            log.error("{}保存角色关联的菜单列表出现错误,{}", LogPrefix.CORE.p(), e.getMessage(), e);
            throw new DataException("参数转换失败");
        }
    }


}
