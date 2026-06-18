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

import com.devops00.spectra.core.system.javabean.entity.DictGroup;
import com.devops00.spectra.core.system.javabean.from.DictGroupFrom;
import com.devops00.spectra.core.system.javabean.vo.DictGroupTreeVO;
import com.devops00.spectra.framework.configure.mapstruct.GlobalMapperConfig;
import com.devops00.spectra.framework.configure.mapstruct.TimeMapper;
import org.mapstruct.Mapper;

import java.util.List;

/// 字典Mapstruct
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/6/18
@Mapper(uses = TimeMapper.class, config = GlobalMapperConfig.class)
public interface DictGroupConverter {

    /// 字典类型入参转实体
    ///
    /// @param source 字典类型入参
    /// @return 转换后的实体
    DictGroup toEntity(DictGroupFrom source);

    /// 字典类型转字典树类型
    ///
    /// @param source 字典类型
    /// @return 字典类型
    DictGroupTreeVO toTreeVO(DictGroup source);

    /// 字典类型转字典树类型列表
    ///
    /// @param source 字典类型
    /// @return 字典类型
    List<DictGroupTreeVO> toTreeVOList(List<DictGroup> source);


}
