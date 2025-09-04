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

package io.github.yangxj96.spectra.core.user.controller;

import cn.dev33.satoken.annotation.SaCheckEL;
import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.github.yangxj96.spectra.common.annotation.ULog;
import io.github.yangxj96.spectra.common.base.Verify;
import io.github.yangxj96.spectra.common.base.javabean.from.PageFrom;
import io.github.yangxj96.spectra.core.system.javabean.entity.Menu;
import io.github.yangxj96.spectra.core.system.javabean.vo.MenuVO;
import io.github.yangxj96.spectra.core.user.javabean.entity.Authority;
import io.github.yangxj96.spectra.core.user.javabean.from.RoleAuthorityFrom;
import io.github.yangxj96.spectra.core.user.javabean.from.RoleFrom;
import io.github.yangxj96.spectra.core.user.javabean.from.RoleMenuFrom;
import io.github.yangxj96.spectra.core.user.javabean.from.RolePageFrom;
import io.github.yangxj96.spectra.core.user.javabean.vo.AuthorityTreeVO;
import io.github.yangxj96.spectra.core.user.javabean.vo.AuthorityVO;
import io.github.yangxj96.spectra.core.user.javabean.vo.RoleVO;
import io.github.yangxj96.spectra.core.user.service.PermissionService;
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
@SaCheckLogin
@RestController
@RequestMapping("/permission")
public class PermissionController {

    @Resource
    private PermissionService bindService;

    @ULog("创建角色")
    @PostMapping("/role")
    @SaCheckEL("@ss.hasPermission('ROLE:INSERT')")
    public void createdRole(@Validated(Verify.Insert.class) @RequestBody RoleFrom params) {
        bindService.createdRole(params);
    }

    @ULog("删除角色")
    @DeleteMapping("/role/{id}")
    @SaCheckEL("@ss.hasPermission('ROLE:DELETE')")
    public void deleteRole(@PathVariable String id) {
        // TODO 暂未实现
    }

    @ULog("修改角色")
    @PutMapping("/role")
    @SaCheckEL("@ss.hasPermission('ROLE:UPDATE')")
    public void modifyRole(@Validated(Verify.Update.class) @RequestBody RoleFrom params) {
        bindService.modifyRole(params);
    }

    @ULog("创建权限")
    @PostMapping("/authority")
    @SaCheckEL("@ss.hasPermission('AUTHORITY:INSERT')")
    public void createdAuthority(@Validated(Verify.Insert.class) @RequestBody RoleFrom params) {
        // TODO 暂未实现
    }

    @ULog("删除权限")
    @DeleteMapping("/authority/{id}")
    @SaCheckEL("@ss.hasPermission('AUTHORITY:DELETE')")
    public void deleteAuthority(@PathVariable String id) {
        // TODO 暂未实现
    }

    @ULog("修改权限信息")
    @PutMapping("/authority")
    @SaCheckEL("@ss.hasPermission('AUTHORITY:UPDATE')")
    public void modifyAuthority(@Validated(Verify.Update.class) @RequestBody RoleFrom params) {
        // TODO 暂未实现
    }

    @ULog("分页查询角色列表")
    @GetMapping("/role/page")
    public IPage<RoleVO> pageRole(PageFrom page, RolePageFrom params) {
        return bindService.pageRole(page, params);
    }

    @ULog("查询角色列表")
    @GetMapping("/role/list")
    public List<RoleVO> listRole() {
        return bindService.listRole();
    }

    @ULog("获取权限树列表")
    @GetMapping("/authority/tree")
    public List<AuthorityTreeVO> authorityTree() {
        return bindService.authorityTree();
    }

    @ULog("获取角色关联的权限列表")
    @GetMapping("/role/{roleId}/authority")
    public List<AuthorityVO> getRoleRelAuthorityByRoleId(@PathVariable String roleId) {
        try {
            long id = Long.parseLong(roleId);
            return bindService.getRoleRelevanceAuthorityByRoleId(id);
        } catch (Exception e) {
            throw new RuntimeException("参数转换失败");
        }
    }

    @ULog("获取角色关联的菜单列表")
    @GetMapping("/role/{roleId}/menu")
    public List<MenuVO> getRoleRelMenuByRoleId(@PathVariable String roleId) {
        try {
            long id = Long.parseLong(roleId);
            return bindService.getRoleRelevanceMenuByRoleId(id);
        } catch (Exception e) {
            throw new RuntimeException("参数转换失败");
        }
    }


    @ULog("保存角色关联的权限列表")
    @PostMapping("/role/{roleId}/authority")
    public void saveRoleRelAuthorityByRoleId(@PathVariable String roleId, @Validated @RequestBody RoleAuthorityFrom from) {
        try {
            long id = Long.parseLong(roleId);
            bindService.saveRoleRelevanceAuthorityByRoleId(id, from);
        } catch (Exception e) {
            throw new RuntimeException("参数转换失败");
        }
    }

    @ULog("保存角色关联的菜单列表")
    @PostMapping("/role/{roleId}/menu")
    public void saveRoleRelMenuByRoleId(@PathVariable String roleId, @Validated @RequestBody RoleMenuFrom from) {
        try {
            long id = Long.parseLong(roleId);
            bindService.saveRoleRelevanceMenuByRoleId(id, from);
        } catch (Exception e) {
            throw new RuntimeException("参数转换失败");
        }
    }

}
