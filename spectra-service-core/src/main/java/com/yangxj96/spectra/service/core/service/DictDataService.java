package com.yangxj96.spectra.service.core.service;

import com.yangxj96.spectra.common.base.BaseService;
import com.yangxj96.spectra.service.core.javabean.entity.DictData;

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
     * 根据字典组ID查询字典数据列表
     *
     * @param gid 字典组ID
     * @return 字典数据列表
     */
    List<DictData> listByGid(Long gid);
}
