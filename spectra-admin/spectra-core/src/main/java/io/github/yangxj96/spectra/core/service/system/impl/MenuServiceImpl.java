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

package io.github.yangxj96.spectra.core.service.system.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.yangxj96.spectra.common.constant.Common;
import io.github.yangxj96.spectra.common.exception.DataNotExistException;
import io.github.yangxj96.spectra.common.utils.CollUtils;
import io.github.yangxj96.spectra.common.utils.TreeBuilder;
import io.github.yangxj96.spectra.core.javabean.system.converter.MenuConverter;
import io.github.yangxj96.spectra.core.javabean.system.entity.Menu;
import io.github.yangxj96.spectra.core.javabean.system.from.MenuSaveFrom;
import io.github.yangxj96.spectra.core.javabean.system.vo.MenuTreeVO;
import io.github.yangxj96.spectra.core.javabean.user.entity.RelRoleMenu;
import io.github.yangxj96.spectra.core.mapper.system.MenuMapper;
import io.github.yangxj96.spectra.core.mapper.user.RelRoleMenuMapper;
import io.github.yangxj96.spectra.core.service.system.MenuService;
import jakarta.annotation.Resource;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/// 菜单service层-实现
///
/// @author Jack Young
/// @version 1.0
/// @since 2025-6-14
@Service
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements MenuService {

    @Resource
    private MenuConverter menuConverter;

    @Resource
    private RelRoleMenuMapper roleMenuMapper;

    @Override
    @Transactional
    public void created(MenuSaveFrom params) {
        var menu = new Menu();
        BeanUtils.copyProperties(params, menu);
        this.save(menu);
    }

    @Override
    @Transactional
    public void modify(MenuSaveFrom params) {
        if (null == this.getById(params.getId())) {
            throw new DataNotExistException("[" + params.getId() + "]不存在");
        }
        var menu = new Menu();
        BeanUtils.copyProperties(params, menu);
        this.updateById(menu);
    }

    @Override
    public @Nullable List<MenuTreeVO> tree() {
        // 先转树形VO
        var vos = menuConverter.toTreeVOS(this.list());
        return new TreeBuilder<>(vos).buildTree(Common.PID);
    }

    @Override
    public List<Menu> getByRelRoleId(long id) {
        var relRoleMenus = roleMenuMapper.getByRoleId(id);
        if (CollUtils.isEmpty(relRoleMenus)) {
            return Collections.emptyList();
        }
        return this.listByIds(relRoleMenus.stream().map(RelRoleMenu::getMenuId).toList());
    }
}
