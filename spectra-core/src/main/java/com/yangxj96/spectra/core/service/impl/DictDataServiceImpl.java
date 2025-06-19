package com.yangxj96.spectra.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yangxj96.spectra.common.base.BaseServiceImpl;
import com.yangxj96.spectra.core.javabean.entity.DictData;
import com.yangxj96.spectra.core.javabean.mapstruct.DictMapstruct;
import com.yangxj96.spectra.core.javabean.vo.DictDataVo;
import com.yangxj96.spectra.core.mapper.DictDataMapper;
import com.yangxj96.spectra.core.service.DictDataService;
import jakarta.annotation.Resource;
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

    @Resource
    private DictMapstruct mapstruct;


    @Override
    public List<DictDataVo> listByDictTypeId(Long id) {
        var wrapper = new LambdaQueryWrapper<DictData>()
                .eq(DictData::getDictTypeId, id);
        List<DictData> list = this.list(wrapper);
        return mapstruct.dataToVos(list);
    }
}
