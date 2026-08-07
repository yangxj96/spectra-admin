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

package com.devops00.spectra.oa.asset.javabean.converter;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.devops00.spectra.framework.configure.mapstruct.GlobalMapperConfig;
import com.devops00.spectra.framework.configure.mapstruct.TimeMapper;
import com.devops00.spectra.oa.asset.javabean.entity.Asset;
import com.devops00.spectra.oa.asset.javabean.entity.AssetCategory;
import com.devops00.spectra.oa.asset.javabean.entity.AssetOperation;
import com.devops00.spectra.oa.asset.javabean.from.AssetCategorySaveFrom;
import com.devops00.spectra.oa.asset.javabean.from.AssetSaveFrom;
import com.devops00.spectra.oa.asset.javabean.vo.AssetCategoryVO;
import com.devops00.spectra.oa.asset.javabean.vo.AssetOperationVO;
import com.devops00.spectra.oa.asset.javabean.vo.AssetVO;

/// 资产 MapStruct 转换器。
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/8/8
@Mapper(uses = TimeMapper.class, config = GlobalMapperConfig.class)
public interface AssetConverter {
    AssetVO toVO(Asset source);

    Asset toEntity(AssetSaveFrom source);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(AssetSaveFrom source, @MappingTarget Asset target);

    AssetCategoryVO toCategoryVO(AssetCategory source);

    AssetCategory toCategoryEntity(AssetCategorySaveFrom source);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCategoryEntity(AssetCategorySaveFrom source, @MappingTarget AssetCategory target);

    AssetOperationVO toOperationVO(AssetOperation source);
}
