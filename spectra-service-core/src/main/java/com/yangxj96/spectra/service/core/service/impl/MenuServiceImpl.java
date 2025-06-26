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

package com.yangxj96.spectra.service.core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yangxj96.spectra.common.constant.Common;
import com.yangxj96.spectra.service.core.javabean.entity.Menu;
import com.yangxj96.spectra.service.core.javabean.from.MenuSaveFrom;
import com.yangxj96.spectra.service.core.javabean.mapstruct.MenuMapstruct;
import com.yangxj96.spectra.service.core.javabean.vo.MenuTreeVO;
import com.yangxj96.spectra.service.core.mapper.MenuMapper;
import com.yangxj96.spectra.service.core.service.MenuService;
import com.yangxj96.spectra.starter.common.exception.DataNotExistException;
import com.yangxj96.spectra.common.utils.TreeBuilder;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 菜单service层-实现
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
@Service
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements MenuService {

    @Resource
    private MenuMapstruct mapstruct;

    @Override
    public List<MenuTreeVO> tree() {
        List<Menu> menus = this.list();
        // 先转树形VO
        List<MenuTreeVO> vos = mapstruct.toTreeVOS(menus);
        return new TreeBuilder<>(vos).buildTree(Common.PID);
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
