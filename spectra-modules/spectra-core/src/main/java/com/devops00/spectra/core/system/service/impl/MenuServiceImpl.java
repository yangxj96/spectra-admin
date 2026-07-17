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

package com.devops00.spectra.core.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.devops00.spectra.common.constant.Common;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.common.utils.CollUtils;
import com.devops00.spectra.common.utils.TreeBuilder;
import com.devops00.spectra.core.system.javabean.converter.MenuConverter;
import com.devops00.spectra.core.system.javabean.entity.Menu;
import com.devops00.spectra.core.system.javabean.from.MenuSaveFrom;
import com.devops00.spectra.core.system.javabean.vo.MenuTreeVO;
import com.devops00.spectra.core.system.mapper.MenuMapper;
import com.devops00.spectra.core.system.service.MenuService;
import com.devops00.spectra.core.user.javabean.entity.RelRoleMenu;
import com.devops00.spectra.core.user.mapper.RelRoleMenuMapper;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/// 菜单service层-实现
///
/// @author yangxj96
/// @version 1.0
/// @since 2025/6/14 00:00
@Slf4j
@Service
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements MenuService {

    private final MenuConverter menuConverter;

    private final RelRoleMenuMapper roleMenuMapper;

    public MenuServiceImpl(MenuConverter menuConverter, RelRoleMenuMapper roleMenuMapper) {
        this.menuConverter = menuConverter;
        this.roleMenuMapper = roleMenuMapper;
    }


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
        var db = this.list();
        if (CollUtils.isEmpty(db)) {
            return Collections.emptyList();
        }
        var vos = menuConverter.toTreeVOList(db);
        return new TreeBuilder<>(vos).buildTree(Common.PID);
    }

    @Override
    public List<Menu> getByRelRoleId(UUID id) {
        var relRoleMenus = roleMenuMapper.getByRoleId(id);
        if (CollUtils.isEmpty(relRoleMenus)) {
            return Collections.emptyList();
        }
        return this.listByIds(relRoleMenus.stream().map(RelRoleMenu::getMenuId).toList());
    }

    @Override
    public void deleteById(String id) {
        Menu menu = this.getById(id);
        if (menu == null) {
            throw new DataNotExistException("[" + id + "]不存在");
        }
        this.removeById(menu);
    }
}
