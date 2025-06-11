/*
 * Copyright (c) 2018-2025 Jack Young (杨新杰)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.yangxj96.spectra.kernel.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.hutool.core.lang.tree.Tree;
import com.yangxj96.spectra.core.annotation.ULog;
import com.yangxj96.spectra.core.base.Verify;
import com.yangxj96.spectra.kernel.entity.from.MenuSaveFrom;
import com.yangxj96.spectra.kernel.service.MenuService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单控制器
 *
 * @author 杨新杰
 * @since 2025/6/3 23:18
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
    public List<Tree<String>> tree() {
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
