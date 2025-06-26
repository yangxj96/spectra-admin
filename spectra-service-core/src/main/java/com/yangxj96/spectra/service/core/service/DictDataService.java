package com.yangxj96.spectra.service.core.service;

import com.yangxj96.spectra.common.base.BaseService;
import com.yangxj96.spectra.service.core.javabean.entity.DictData;
import com.yangxj96.spectra.service.core.javabean.vo.DictDataVo;

import java.util.List;

/**
 * 字典(字典数据)业务层
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-18
 */
public interface DictDataService extends BaseService<DictData> {

    /**
     * 根据字典类型ID获取字典数据列表
     *
     * @param id 字典类型ID
     * @return 字典数据列表
     */
    List<DictDataVo> listByDictTypeId(Long id);
}
