package com.yangxj96.spectra.core.system.service.impl;

import com.yangxj96.spectra.common.base.BaseServiceImpl;
import com.yangxj96.spectra.core.system.javabean.entity.DictData;
import com.yangxj96.spectra.core.system.mapper.DictDataMapper;
import com.yangxj96.spectra.core.system.service.DictDataService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 字典(字典数据)业务层-实现
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-18
 */
@Service
public class DictDataServiceImpl extends BaseServiceImpl<DictDataMapper, DictData> implements DictDataService {

    @Override
    public List<DictData> listByGid(Long gid) {
        var wrapper = this.getWrapper()
                .eq(DictData::getGid, gid);
        return this.list(wrapper);
    }
}
