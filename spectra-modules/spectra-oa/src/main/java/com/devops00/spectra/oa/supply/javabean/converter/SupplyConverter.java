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

package com.devops00.spectra.oa.supply.javabean.converter;

import com.devops00.spectra.framework.serialization.mapper.GlobalMapperConfig;
import com.devops00.spectra.framework.serialization.mapper.TimeMapper;
import com.devops00.spectra.oa.supply.javabean.entity.SupplyItem;
import com.devops00.spectra.oa.supply.javabean.entity.SupplyOperation;
import com.devops00.spectra.oa.supply.javabean.from.SupplySaveFrom;
import com.devops00.spectra.oa.supply.javabean.vo.SupplyItemVO;
import com.devops00.spectra.oa.supply.javabean.vo.SupplyOperationVO;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * 办公用品 MapStruct 转换器。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/9
 */
@Mapper(uses = TimeMapper.class, config = GlobalMapperConfig.class)
public interface SupplyConverter {
    /**
     * 办公用品实体转视图对象。
     */
    SupplyItemVO toVO(SupplyItem source);

    /**
     * 办公用品保存入参转实体。
     */
    SupplyItem toEntity(SupplySaveFrom source);

    /**
     * 使用保存入参更新办公用品实体。
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(SupplySaveFrom source, @MappingTarget SupplyItem target);

    /**
     * 办公用品操作实体转视图对象。
     */
    SupplyOperationVO toOperationVO(SupplyOperation source);
}
