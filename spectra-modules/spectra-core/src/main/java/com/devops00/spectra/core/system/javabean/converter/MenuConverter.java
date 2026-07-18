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

package com.devops00.spectra.core.system.javabean.converter;

import com.devops00.spectra.core.system.javabean.entity.Menu;
import com.devops00.spectra.core.system.javabean.from.MenuSaveFrom;
import com.devops00.spectra.core.system.javabean.vo.MenuTreeVO;
import com.devops00.spectra.core.system.javabean.vo.MenuVO;
import com.devops00.spectra.framework.configure.mapstruct.GlobalMapperConfig;
import com.devops00.spectra.framework.configure.mapstruct.TimeMapper;
import org.mapstruct.Mapper;

import java.util.List;

/// 菜单相关mapstruct
///
/// @author yangxj96
/// @version 1.0
/// @since 2025/6/14 00:00
@Mapper(uses = TimeMapper.class, config = GlobalMapperConfig.class)
public interface MenuConverter {

    /// 实体转 树形实体VO
    ///
    /// @param source 实体
    /// @return 树形实体VO
    MenuTreeVO toTreeVO(Menu source);

    /// 实体转 树形实体VO
    ///
    /// @param source 实体
    /// @return 树形实体VO
    List<MenuTreeVO> toTreeVOList(List<Menu> source);

    /// 实体转VO
    ///
    /// @param source 实体
    /// @return VO
    MenuVO toVO(Menu source);

    /// 实体转VO(列表)
    ///
    /// @param source 实体
    /// @return VO
    List<MenuVO> toVOList(List<Menu> source);

    /// 保存入参转实体
    ///
    /// @param source 保存入参
    /// @return 实体
    Menu toEntity(MenuSaveFrom source);
}
