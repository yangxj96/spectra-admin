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
import com.yangxj96.spectra.common.enums.CommonState;
import com.yangxj96.spectra.common.utils.TreeBuilder;
import com.yangxj96.spectra.service.core.javabean.entity.DictData;
import com.yangxj96.spectra.service.core.javabean.entity.DictGroup;
import com.yangxj96.spectra.service.core.javabean.from.DictDataFrom;
import com.yangxj96.spectra.service.core.javabean.from.DictGroupFrom;
import com.yangxj96.spectra.service.core.javabean.mapstruct.DictMapstruct;
import com.yangxj96.spectra.service.core.javabean.vo.DictDataVo;
import com.yangxj96.spectra.service.core.javabean.vo.DictTypeTreeVO;
import com.yangxj96.spectra.service.core.service.DictDataService;
import com.yangxj96.spectra.service.core.service.DictService;
import com.yangxj96.spectra.service.core.service.DictTypeService;
import com.yangxj96.spectra.starter.common.exception.DataNotExistException;
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
    public void createGroup(DictGroupFrom params) {
        DictGroup entity = mapstruct.groupFromToEntity(params);
        typeService.save(entity);
    }

    @Override
    @Transactional
    public void modifyGroup(DictGroupFrom params) {
        DictGroup entity = mapstruct.groupFromToEntity(params);
        typeService.updateById(entity);
    }

    @Override
    @Transactional
    public void createData(DictDataFrom params) {
        DictData entity = mapstruct.dataFromToEntity(params);
        dataService.save(entity);
    }

    @Override
    @Transactional
    public void modifyData(DictDataFrom params) {
        DictData entity = mapstruct.dataFromToEntity(params);
        dataService.updateById(entity);
    }

    @Override
    public List<DictTypeTreeVO> listDictGroupWrapTree() {
        // 不能是内置字段,也不能是隐藏字段
        LambdaQueryWrapper<DictGroup> wrapper = new LambdaQueryWrapper<>();
        wrapper
                .eq(DictGroup::getState, CommonState.ENABLE)
                .eq(DictGroup::getBuiltin, Boolean.FALSE)
                .eq(DictGroup::getHide, Boolean.FALSE);
        List<DictGroup> menus = typeService.list(wrapper);
        List<DictTypeTreeVO> vos = mapstruct.typeToTreeVOS(menus);
        return new TreeBuilder<>(vos).buildTree(Common.PID);
    }

    @Override
    public List<DictDataVo> listDictDataByGroupCode(String code) {
        DictGroup type = typeService.getByCode(code);
        if (null == type) {
            throw new DataNotExistException("字典类型不存在");
        }
        return dataService.listByDictTypeId(type.getId());
    }
}
