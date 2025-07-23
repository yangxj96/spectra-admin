package com.yangxj96.spectra.core.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yangxj96.spectra.common.base.BaseServiceImpl;
import com.yangxj96.spectra.core.system.javabean.entity.DictGroup;
import com.yangxj96.spectra.core.system.mapper.DictTypeMapper;
import com.yangxj96.spectra.core.system.service.DictGroupService;
import org.springframework.stereotype.Service;

/**
 * 字典(字典类型)业务层实现
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-18
 */
@Service
public class DictGroupServiceImpl extends BaseServiceImpl<DictTypeMapper, DictGroup> implements DictGroupService {

    @Override
    public DictGroup getByCode(String code) {
        return this.getOne(
                new LambdaQueryWrapper<DictGroup>()
                        .eq(DictGroup::getCode, code)
                        .last("LIMIT 1")
        );
    }
}
