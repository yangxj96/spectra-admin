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

package com.yangxj96.spectra.core.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yangxj96.spectra.core.system.javabean.entity.Menu;
import com.yangxj96.spectra.core.system.javabean.from.MenuSaveFrom;
import com.yangxj96.spectra.core.system.javabean.vo.MenuTreeVO;

import java.util.List;

/**
 * 菜单service层
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
public interface MenuService extends IService<Menu> {

    /**
     * 生成树形菜单
     *
     * @return 生成的树形菜单
     */
    List<MenuTreeVO> tree();

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
