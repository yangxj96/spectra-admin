package com.devops00.spectra.oa.supply.javabean.converter;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.devops00.spectra.framework.configure.mapstruct.GlobalMapperConfig;
import com.devops00.spectra.framework.configure.mapstruct.TimeMapper;
import com.devops00.spectra.oa.supply.javabean.entity.SupplyItem;
import com.devops00.spectra.oa.supply.javabean.entity.SupplyOperation;
import com.devops00.spectra.oa.supply.javabean.from.SupplySaveFrom;
import com.devops00.spectra.oa.supply.javabean.vo.SupplyItemVO;
import com.devops00.spectra.oa.supply.javabean.vo.SupplyOperationVO;

/// 办公用品 MapStruct 转换器。
@Mapper(uses = TimeMapper.class, config = GlobalMapperConfig.class)
public interface SupplyConverter {
    SupplyItemVO toVO(SupplyItem source);

    SupplyItem toEntity(SupplySaveFrom source);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(SupplySaveFrom source, @MappingTarget SupplyItem target);

    SupplyOperationVO toOperationVO(SupplyOperation source);
}
