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

package com.yangxj96.spectra.service.core.javabean.mapstruct;

import com.yangxj96.spectra.service.core.javabean.entity.Menu;
import com.yangxj96.spectra.service.core.javabean.vo.MenuTreeVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * 菜单相关mapstruct
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
@Mapper(componentModel = "spring")
public interface MenuMapstruct {

    List<MenuTreeVO> toTreeVOS(List<Menu> menus);

    @Mapping(target = "children", ignore = true)
    MenuTreeVO toTreeVO(Menu menu);
}
