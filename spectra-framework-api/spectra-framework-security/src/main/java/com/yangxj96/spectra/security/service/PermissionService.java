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

package com.yangxj96.spectra.security.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yangxj96.spectra.core.entity.from.PageFrom;
import com.yangxj96.spectra.security.entity.dto.Role;
import com.yangxj96.spectra.security.entity.from.RoleFrom;
import com.yangxj96.spectra.security.entity.from.RolePageFrom;

/**
 * 权限操作业务层
 */
public interface PermissionService {

    /**
     * 新增角色
     *
     * @param params 角色入参实体
     */
    void createdRole(RoleFrom params);

    /**
     * 修改角色
     *
     * @param params 角色入参实体
     */
    void modifyRole(RoleFrom params);

    /**
     * 分页查询角色信息
     *
     * @param page   分页信息
     * @param params 查询参数
     * @return 分页结果
     */
    IPage<Role> pageRole(PageFrom page, RolePageFrom params);
}
