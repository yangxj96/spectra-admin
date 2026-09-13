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

import com.devops00.spectra.core.system.javabean.entity.Department;
import com.devops00.spectra.core.system.javabean.vo.DepartmentTreeVo;
import com.devops00.spectra.framework.serialization.mapper.GlobalMapperConfig;
import com.devops00.spectra.framework.serialization.mapper.TimeMapper;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * 组织机构的数据转换使用
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/7/14 00:00
 */
@Mapper(uses = TimeMapper.class, config = GlobalMapperConfig.class)
public interface OrganizationConverter {

    /**
     * 转换树结构。
     *
     * @param source 当前数据或配置的来源。
     * @return 部门树结构数据。
     */
    DepartmentTreeVo toTreeVO(Department source);

    /**
     * 转换树结构。
     *
     * @param source 当前数据或配置的来源。
     * @return 符合条件的数据集合。
     */
    List<DepartmentTreeVo> toTreeVOList(List<Department> source);

}
