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

package com.yangxj96.spectra.kernel.service;

import cn.hutool.core.lang.tree.Tree;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yangxj96.spectra.kernel.entity.dto.Menu;
import com.yangxj96.spectra.kernel.entity.from.MenuSaveFrom;

import java.util.List;

/**
 * 菜单业务层
 *
 * @author 杨新杰
 * @since 2025/6/3 23:18
 */
public interface MenuService extends IService<Menu> {

    /**
     * 生成树形菜单
     *
     * @return 生成的树形菜单
     */
    List<Tree<String>> tree();

    /**
     * 创建菜单
     *
     * @param params 菜单信息
     */
    void created(MenuSaveFrom params);

    /**
     * 修改菜单信息
     *
     * @param params 修改参数
     */
    void modify(MenuSaveFrom params);
}
