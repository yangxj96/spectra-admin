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

package com.devops00.spectra.core.javabean.system.converter;

import com.devops00.spectra.core.javabean.system.entity.DictItem;
import com.devops00.spectra.core.javabean.system.from.DictItemFrom;
import com.devops00.spectra.core.javabean.system.vo.DictItemVO;
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
public interface DictItemConverter {

    ///
    /// 字典数据入参转实体
    ///
    /// @param source 字典数据入参
    /// @return 转换后的实体
    ///
    DictItem toEntity(DictItemFrom source);

    /// 字典数据转字典数据VO
    ///
    /// @param source 字典数据
    /// @return 字典数据VO
    DictItemVO toVO(DictItem source);

    /// 字典数据转字典数据VO(列表)
    ///
    /// @param source 字典数据
    /// @return 字典数据VO(列表)
    List<DictItemVO> toVOList(List<DictItem> source);
}
