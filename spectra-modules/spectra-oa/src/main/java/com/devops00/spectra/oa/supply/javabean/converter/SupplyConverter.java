package com.devops00.spectra.oa.supply.javabean.converter;

import com.devops00.spectra.framework.configure.mapstruct.GlobalMapperConfig;
import com.devops00.spectra.framework.configure.mapstruct.TimeMapper;
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
