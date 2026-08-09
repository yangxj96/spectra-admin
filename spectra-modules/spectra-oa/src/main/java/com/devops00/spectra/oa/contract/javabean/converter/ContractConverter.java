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

package com.devops00.spectra.oa.contract.javabean.converter;

import com.devops00.spectra.framework.configure.mapstruct.GlobalMapperConfig;
import com.devops00.spectra.framework.configure.mapstruct.TimeMapper;
import com.devops00.spectra.oa.contract.javabean.entity.Contract;
import com.devops00.spectra.oa.contract.javabean.entity.ContractMilestone;
import com.devops00.spectra.oa.contract.javabean.entity.ContractVersion;
import com.devops00.spectra.oa.contract.javabean.from.ContractSaveFrom;
import com.devops00.spectra.oa.contract.javabean.vo.ContractMilestoneVO;
import com.devops00.spectra.oa.contract.javabean.vo.ContractVersionVO;
import com.devops00.spectra.oa.contract.javabean.vo.ContractVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * 合同对象转换器。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/8
 */
@Mapper(uses = TimeMapper.class, config = GlobalMapperConfig.class)
public interface ContractConverter {

    /**
     * 合同实体转视图。
     */
    ContractVO toVO(Contract source);

    /**
     * 合同版本实体转视图。
     */
    @Mapping(source = "currentVersion", target = "current")
    ContractVersionVO toVersionVO(ContractVersion source);

    /**
     * 合同履约节点实体转视图。
     */
    ContractMilestoneVO toMilestoneVO(ContractMilestone source);

    /**
     * 合同保存入参转实体。
     */
    Contract toEntity(ContractSaveFrom source);

    /**
     * 使用合同保存入参更新实体。
     */
    void updateEntity(ContractSaveFrom source, @MappingTarget Contract target);
}
