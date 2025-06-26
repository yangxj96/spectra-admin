package com.yangxj96.spectra.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yangxj96.spectra.common.base.BaseServiceImpl;
import com.yangxj96.spectra.core.javabean.entity.DictType;
import com.yangxj96.spectra.core.mapper.DictTypeMapper;
import com.yangxj96.spectra.core.service.DictTypeService;
import org.springframework.stereotype.Service;

/**
 * 字典(字典类型)业务层实现
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-18
 */
@Service
public class DictTypeServiceImpl extends BaseServiceImpl<DictTypeMapper, DictType> implements DictTypeService {

    @Override
    public DictType getByCode(String code) {
        return this.getOne(
                new LambdaQueryWrapper<DictType>()
                        .eq(DictType::getCode, code)
                        .last("LIMIT 1")
        );
    }
}
