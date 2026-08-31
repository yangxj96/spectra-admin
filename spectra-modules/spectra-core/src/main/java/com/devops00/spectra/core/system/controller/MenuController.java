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

package com.devops00.spectra.core.system.controller;

import com.devops00.spectra.common.base.Verify;
import com.devops00.spectra.core.system.javabean.from.MenuSaveFrom;
import com.devops00.spectra.core.system.javabean.vo.MenuTreeVO;
import com.devops00.spectra.core.system.service.MenuService;
import com.devops00.spectra.common.audit.Audit;
import com.devops00.spectra.security.base.javabean.entity.SecurityUser;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
 * 菜单控制器
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/6/14 00:00
 */
@Slf4j
@RestController
@RequestMapping("/menu")
public class MenuController {

    private final MenuService bindService;

    public MenuController(MenuService bindService) {
        this.bindService = bindService;
    }

    /**
     * 新增菜单信息
     *
     * @param params 菜单信息
     */
    @Audit("'新增菜单'")
    @PostMapping(value = "/created", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'menu:create')")
    public void created(@Validated(Verify.Insert.class) @RequestBody MenuSaveFrom params) {
        bindService.created(params);
    }

    /**
     * 更新或推进目标状态（{@code deleteById}）。
     */
    @Audit("'删除菜单'")
    @DeleteMapping(value = "/{id}", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'menu:disable')")
    public void deleteById(@PathVariable UUID id) {
        bindService.deleteById(id);
    }

    /**
     * 修改菜单信息
     *
     * @param params 菜单信息
     */
    @Audit("'修改菜单'")
    @PutMapping(value = "/modify", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'menu:update')")
    public void modify(@Validated(Verify.Update.class) @RequestBody MenuSaveFrom params) {
        bindService.modify(params);
    }

    /**
     * 获取树形菜单
     *
     * @return 构建的树形菜单
     */
    @Audit(value = "'获取树形菜单'")
    @GetMapping(value = "/tree", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'menu:read')")
    public @Nullable List<MenuTreeVO> tree() {
        return bindService.tree();
    }

    /**
     * 获取当前用户授权菜单树
     *
     * @param user 当前登录用户
     * @return 当前用户授权菜单树
     */
    @Audit(value = "'获取当前用户菜单'")
    @GetMapping(value = "/current", version = "1.0.0")
    @PreAuthorize("isAuthenticated()")
    public List<MenuTreeVO> current(@AuthenticationPrincipal SecurityUser user) {
        return bindService.current(user.getId());
    }
}
