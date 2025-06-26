package com.yangxj96.spectra.service.core.service;

import com.yangxj96.spectra.common.base.BaseService;
import com.yangxj96.spectra.service.core.javabean.entity.DictType;

/**
 * 字典(字典类型)业务层
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-18
 */
public interface DictTypeService extends BaseService<DictType> {

    /**
     * 根据字典类型编码获取字典类型
     *
     * @param code 字典类型编码
     * @return 字典类型
     */
    DictType getByCode(String code);
}
