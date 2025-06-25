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

package com.yangxj96.spectra.core.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.yangxj96.spectra.common.base.Verify;
import com.yangxj96.spectra.common.annotation.ULog;
import com.yangxj96.spectra.core.javabean.from.MenuSaveFrom;
import com.yangxj96.spectra.core.javabean.vo.MenuTreeVO;
import com.yangxj96.spectra.core.service.MenuService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单控制器
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
@SaCheckLogin
@RestController
@RequestMapping("/menu")
public class MenuController {

    @Resource
    private MenuService bindService;

    /**
     * 获取树形菜单
     *
     * @return 构建的树形菜单
     */
    @ULog(value = "获取树形菜单")
    @GetMapping("/tree")
    public List<MenuTreeVO> tree() {
        return bindService.tree();
    }

    /**
     * 新增菜单信息
     *
     * @param params 菜单信息
     */
    @ULog("新增菜单")
    @PostMapping("/created")
    public void created(@Validated(Verify.Insert.class) @RequestBody MenuSaveFrom params) {
        bindService.created(params);
    }

    /**
     * 修改菜单信息
     *
     * @param params 菜单信息
     */
    @ULog("修改菜单")
    @PutMapping("/modify")
    public void modify(@Validated(Verify.Update.class) @RequestBody MenuSaveFrom params) {
        bindService.modify(params);
    }
}
