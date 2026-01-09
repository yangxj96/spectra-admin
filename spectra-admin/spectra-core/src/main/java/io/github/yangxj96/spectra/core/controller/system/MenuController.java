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

package io.github.yangxj96.spectra.core.controller.system;

import io.github.yangxj96.spectra.common.base.Verify;
import io.github.yangxj96.spectra.common.exception.NotImplementedException;
import io.github.yangxj96.spectra.core.configure.ulog.annotation.ULog;
import io.github.yangxj96.spectra.core.javabean.system.from.MenuSaveFrom;
import io.github.yangxj96.spectra.core.javabean.system.vo.MenuTreeVO;
import io.github.yangxj96.spectra.core.service.system.MenuService;
import org.jspecify.annotations.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/// 菜单控制器
///
/// @author Jack Young
/// @version 1.0
/// @since 2025-6-14
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
    @ULog("新增菜单")
    @PostMapping("/created")
    @PreAuthorize("hasPermission(null ,'MENU:INSERT')")
    public void created(@Validated(Verify.Insert.class) @RequestBody MenuSaveFrom params) {
        bindService.created(params);
    }

    @ULog("删除菜单")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission(null ,'MENU:DELETE')")
    public void deleteById(@PathVariable String id) {
        throw new NotImplementedException("暂未实现,ID:" + id);
    }

    /**
     * 修改菜单信息
     *
     * @param params 菜单信息
     */
    @ULog("修改菜单")
    @PutMapping("/modify")
    @PreAuthorize("hasPermission(null ,'MENU:UPDATE')")
    public void modify(@Validated(Verify.Update.class) @RequestBody MenuSaveFrom params) {
        bindService.modify(params);
    }

    /**
     * 获取树形菜单
     *
     * @return 构建的树形菜单
     */
    @ULog(value = "获取树形菜单")
    @GetMapping("/tree")
    public @Nullable List<MenuTreeVO> tree() {
        return bindService.tree();
    }
}
