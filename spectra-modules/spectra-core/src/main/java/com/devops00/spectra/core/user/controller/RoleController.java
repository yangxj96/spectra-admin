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
import com.devops00.spectra.common.audit.Audit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @Audit("'提交角色编辑'")
    @PostMapping(value = "/editor", version = "1.0.0")
    @PreAuthorize("((#p0.id == null and hasPermission(null, 'role:create')) "
            + "or (#p0.id != null and hasPermission(null, 'role:update'))) "
            + "and hasPermission(null, 'role:grant') and hasPermission(null, 'role:assign')")
    /**
     * 更新或推进目标状态（{@code saveEditor}）。
     */
    public RoleVO saveEditor(@Validated @RequestBody RoleEditorSaveFrom params) {
        return roleEditorService.save(params);
    }

    /**
     * 处理内部业务逻辑（{@code enable}）。
     */
    @Audit("'启用角色'")
    @PutMapping(value = "/{id}/enable", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'role:disable')")
    public void enable(@PathVariable UUID id) {
        bindService.enable(id);
    }

    /**
     * 更新或推进目标状态（{@code disable}）。
     */
    @Audit("'禁用角色'")
    @PutMapping(value = "/{id}/disable", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'role:disable')")
    public void disable(@PathVariable UUID id) {
        bindService.disable(id);
    }

    /**
     * 更新或推进目标状态（{@code deleteById}）。
     */
    @Audit("'删除角色'")
    @DeleteMapping(value = "/{id}", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'role:delete')")
    public void deleteById(@PathVariable UUID id) {
        bindService.deleteById(id);
    }

    /* 查询部分 */

    @Audit("'分页查询角色列表'")
    @GetMapping(value = "/page", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'role:read')")
    public IPage<RoleVO> page(PageFrom page, RolePageFrom params) {
        return bindService.page(page, params);
    }

    /**
     * 查询或获取目标数据（{@code list}）。
     */
    @Audit("'查询角色列表'")
    @GetMapping(value = "/list", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'role:read')")
    public List<RoleVO> list() {
        return bindService.all();
    }

    /**
     * 查询或获取目标数据（{@code detail}）。
     */
    @Audit("'查询角色详情'")
    @GetMapping(value = "/{id}", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'role:read')")
    public RoleVO detail(@PathVariable UUID id) {
        return bindService.detail(id);
    }

    /* 关联处理部分 */

    @Audit("'获取角色关联的菜单列表'")
    @GetMapping(value = "/{roleId}/menu", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'role:read')")
    public List<MenuVO> getRoleRelMenuByRoleId(@PathVariable UUID roleId) {
        try {
            return relRoleMenuService.get(roleId);
        } catch (Exception e) {
            log.error("{}获取角色关联的菜单列表出现错误,{}", LogPrefix.CORE.p(), e.getMessage(), e);
            throw new DataException("参数转换失败", e);
        }
    }

}
