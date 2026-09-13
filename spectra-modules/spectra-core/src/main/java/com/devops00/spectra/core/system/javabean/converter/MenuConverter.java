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
import com.devops00.spectra.framework.serialization.mapper.GlobalMapperConfig;
import com.devops00.spectra.framework.serialization.mapper.TimeMapper;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * 菜单相关mapstruct
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/6/14 00:00
 */
@Mapper(uses = TimeMapper.class, config = GlobalMapperConfig.class)
public interface MenuConverter {

    /**
     * 转换树结构。
     *
     * @param source 当前数据或配置的来源。
     * @return 菜单树结构数据。
     */
    MenuTreeVO toTreeVO(Menu source);

    /**
     * 转换树结构。
     *
     * @param source 当前数据或配置的来源。
     * @return 符合条件的数据集合。
     */
    List<MenuTreeVO> toTreeVOList(List<Menu> source);

    /**
     * 转换菜单。
     *
     * @param source 当前数据或配置的来源。
     * @return 菜单数据。
     */
    MenuVO toVO(Menu source);

    /**
     * 转换菜单。
     *
     * @param source 当前数据或配置的来源。
     * @return 符合条件的数据集合。
     */
    List<MenuVO> toVOList(List<Menu> source);

    /**
     * 保存入参转实体
     *
     * @param source 保存入参
     * @return 实体
     */
    Menu toEntity(MenuSaveFrom source);
}
