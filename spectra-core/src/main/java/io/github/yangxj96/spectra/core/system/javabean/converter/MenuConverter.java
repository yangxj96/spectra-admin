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

package io.github.yangxj96.spectra.core.system.javabean.converter;

import io.github.yangxj96.spectra.core.system.javabean.entity.Menu;
import io.github.yangxj96.spectra.core.system.javabean.vo.MenuTreeVO;
import io.github.yangxj96.spectra.core.system.javabean.vo.MenuVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * 菜单相关mapstruct
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MenuConverter {

    /**
     * 实体转 树形实体VO
     *
     * @param entity 实体
     * @return 树形实体VO
     */
    MenuTreeVO toTreeVO(Menu entity);

    /**
     * 实体列表 转 树形实体VO列表
     *
     * @param coll 实体列表
     * @return 树形实体VO列表
     */
    List<MenuTreeVO> toTreeVOS(List<Menu> coll);

    /**
     * 实体转VO
     *
     * @param entity 实体
     * @return VO
     */
    MenuVO toVO(Menu entity);

    /**
     * 实体列表转VO列表
     *
     * @param coll 实体泪飙
     * @return VO列表
     */
    List<MenuVO> toVOS(List<Menu> coll);

}
