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

package com.yangxj96.spectra.security.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yangxj96.spectra.core.annotation.ULog;
import com.yangxj96.spectra.core.base.Verify;
import com.yangxj96.spectra.core.entity.from.PageFrom;
import com.yangxj96.spectra.security.entity.dto.Role;
import com.yangxj96.spectra.security.entity.from.RoleFrom;
import com.yangxj96.spectra.security.entity.from.RolePageFrom;
import com.yangxj96.spectra.security.service.PermissionService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 权限操作相关
 *
 * @author 杨新杰
 * @since 2025/6/9 23:45
 */
@RestController
@RequestMapping("/permission")
public class PermissionController {

    @Resource
    private PermissionService bindService;

    /**
     * 分页查询角色信息
     *
     * @return 分页结果
     */
    @ULog("分页查询角色列表")
    @GetMapping("/pageRole")
    public IPage<Role> pageRole(PageFrom page, RolePageFrom params) {
        return bindService.pageRole(page, params);
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
