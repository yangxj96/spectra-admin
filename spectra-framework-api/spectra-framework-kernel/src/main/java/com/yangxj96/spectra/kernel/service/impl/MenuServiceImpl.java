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

package com.yangxj96.spectra.kernel.service.impl;

import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yangxj96.spectra.core.exception.DataNotExistException;
import com.yangxj96.spectra.kernel.entity.dto.Menu;
import com.yangxj96.spectra.kernel.entity.from.MenuSaveFrom;
import com.yangxj96.spectra.kernel.mapper.MenuMapper;
import com.yangxj96.spectra.kernel.service.MenuService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 菜单业务层实现
 *
 * @author 杨新杰
 * @since 2025/6/3 23:18
 */
@Service
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements MenuService {

    @Override
    public List<Tree<String>> tree() {
        List<Menu> menus = this.list();
        TreeNodeConfig config = new TreeNodeConfig();
        config.setWeightKey("sort");
        config.setParentIdKey("pid");
        return TreeUtil.build(menus, "0", config, (node, tree) -> {
            tree.setId(node.getId().toString());
            tree.setParentId(node.getPid().toString());
            tree.setName(node.getName());
            tree.putExtra("icon", node.getIcon());
            tree.putExtra("path", node.getPath());
            tree.putExtra("component", node.getComponent());
            tree.putExtra("layout", node.getLayout());
            tree.putExtra("sort", node.getSort());
        });
    }

    @Override
    @Transactional
    public void created(MenuSaveFrom params) {
        Menu menu = new Menu();
        BeanUtils.copyProperties(params, menu);
        this.save(menu);
    }

    @Override
    @Transactional
    public void modify(MenuSaveFrom params) {
        if (null == this.getById(params.getId())) {
            throw new DataNotExistException("[" + params.getId() + "]不存在");
        }
        Menu menu = new Menu();
        BeanUtils.copyProperties(params, menu);
        this.updateById(menu);
    }
}
