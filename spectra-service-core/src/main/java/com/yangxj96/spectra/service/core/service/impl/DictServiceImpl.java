/*
 *  Copyright 2025 yangxj96.com
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
 *
 */

package com.yangxj96.spectra.service.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yangxj96.spectra.common.constant.Common;
import com.yangxj96.spectra.service.core.javabean.entity.DictData;
import com.yangxj96.spectra.service.core.javabean.entity.DictType;
import com.yangxj96.spectra.service.core.javabean.from.DictDataFrom;
import com.yangxj96.spectra.service.core.javabean.from.DictTypeFrom;
import com.yangxj96.spectra.service.core.javabean.mapstruct.DictMapstruct;
import com.yangxj96.spectra.service.core.javabean.vo.DictDataVo;
import com.yangxj96.spectra.service.core.javabean.vo.DictTypeTreeVO;
import com.yangxj96.spectra.service.core.service.DictDataService;
import com.yangxj96.spectra.service.core.service.DictTypeService;
import com.yangxj96.spectra.starter.common.exception.DataNotExistException;
import com.yangxj96.spectra.common.utils.TreeBuilder;
import com.yangxj96.spectra.service.core.service.DictService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 字典操作业务层实现
 * </p>
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/6/18
 */
@Slf4j
@Service
public class DictServiceImpl implements DictService {

    @Resource
    private DictMapstruct mapstruct;

    @Resource
    private DictTypeService typeService;

    @Resource
    private DictDataService dataService;


    @Override
    @Transactional
    public void createType(DictTypeFrom params) {
        // 先转换为实体
        DictType entity = mapstruct.typeFromToEntity(params);
        typeService.save(entity);
    }

    @Override
    @Transactional
    public void createData(DictDataFrom params) {
        // 先转换为实体
        DictData entity = mapstruct.dataFromToEntity(params);
        dataService.save(entity);
    }

    @Override
    public List<DictTypeTreeVO> getTypesWrapTree() {
        // 不能是内置字段,也不能是隐藏字段
        LambdaQueryWrapper<DictType> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DictType::getBuiltin, Boolean.FALSE)
                .eq(DictType::getHide, Boolean.FALSE);
        List<DictType> menus = typeService.list(wrapper);
        List<DictTypeTreeVO> vos = mapstruct.typeToTreeVOS(menus);
        return new TreeBuilder<>(vos).buildTree(Common.PID);
    }

    @Override
    public List<DictDataVo> getDataByTypeCode(String code) {
        DictType type = typeService.getByCode(code);
        if (null == type) {
            throw new DataNotExistException("字典类型不存在");
        }
        return dataService.listByDictTypeId(type.getId());
    }
}
