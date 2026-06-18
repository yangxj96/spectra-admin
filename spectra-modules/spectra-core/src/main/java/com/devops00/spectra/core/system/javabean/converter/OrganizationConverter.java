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

package com.devops00.spectra.core.system.javabean.converter;

import com.devops00.spectra.core.system.javabean.entity.Department;
import com.devops00.spectra.core.system.javabean.from.DepartmentFrom;
import com.devops00.spectra.core.system.javabean.vo.DepartmentTreeVo;
import com.devops00.spectra.framework.configure.mapstruct.GlobalMapperConfig;
import com.devops00.spectra.framework.configure.mapstruct.TimeMapper;
import org.mapstruct.Mapper;

import java.util.List;

/// 组织机构的数据转换使用
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/7/14
@Mapper(uses = TimeMapper.class, config = GlobalMapperConfig.class)
public interface OrganizationConverter {

    /// 实体转树形
    ///
    /// @param source 实体
    /// @return 树形
    DepartmentTreeVo toTreeVO(Department source);

    /// 实体转树形(列表)
    ///
    /// @param source 实体
    /// @return 树形
    List<DepartmentTreeVo> toTreeVOList(List<Department> source);

    /// 入参转实体
    ///
    /// @param source 入参
    /// @return 实体
    Department toEntity(DepartmentFrom source);

}

