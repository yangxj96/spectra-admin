/*
 *  Copyright 2018-2026 yangxj96
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
 */

package com.devops00.spectra.core.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.devops00.spectra.common.constant.Common;
import com.devops00.spectra.common.exception.BuiltinDataException;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.common.utils.TreeBuilder;
import com.devops00.spectra.core.system.javabean.converter.DictGroupConverter;
import com.devops00.spectra.core.system.javabean.converter.DictItemConverter;
import com.devops00.spectra.core.system.javabean.entity.DictGroup;
import com.devops00.spectra.core.system.javabean.entity.DictItem;
import com.devops00.spectra.core.system.javabean.from.DictGroupFrom;
import com.devops00.spectra.core.system.javabean.from.DictItemFrom;
import com.devops00.spectra.core.system.javabean.vo.DictGroupTreeVO;
import com.devops00.spectra.core.system.javabean.vo.DictItemVO;
import com.devops00.spectra.core.system.service.DictGroupService;
import com.devops00.spectra.core.system.service.DictItemService;
import com.devops00.spectra.core.system.service.DictService;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/// 字典操作业务层实现
///
/// @author yangxj96
/// @version 1.0
/// @since 2025/6/18 00:00
@Slf4j
@Service
public class DictServiceImpl implements DictService {

    private final DictGroupConverter dictGroupConverter;

    private final DictItemConverter dictItemConverter;

    private final DictGroupService groupService;

    private final DictItemService dataService;

    public DictServiceImpl(DictGroupConverter dictGroupConverter, DictItemConverter dictItemConverter, DictGroupService groupService, DictItemService dataService) {
        this.dictGroupConverter = dictGroupConverter;
        this.dictItemConverter = dictItemConverter;
        this.groupService = groupService;
        this.dataService = dataService;
    }


    @Override
    @Transactional
    public void createGroup(DictGroupFrom params) {
        var entity = dictGroupConverter.toEntity(params);
        groupService.save(entity);
    }

    @Override
    @Transactional
    public void deleteGroup(UUID id) {
        var group = groupService.getById(id);
        if (null == group) {
            throw new DataNotExistException("字典组不存在");
        }
        if (group.getBuiltin()) {
            throw new BuiltinDataException("内置字典,无法删除");
        }
        // 获取他的字典数据
        var dictData = dataService.listByGid(id);
        dataService.removeBatchByIds(dictData.stream().map(DictItem::getId).toList());
        // 删除字典组
        groupService.removeById(id);
    }

    @Override
    @Transactional
    public void modifyGroup(DictGroupFrom params) {
        var group = groupService.getById(params.getId());
        if (group.getBuiltin()) {
            throw new BuiltinDataException("内置字典,无法修改");
        }
        var entity = dictGroupConverter.toEntity(params);
        groupService.updateById(entity);
    }

    @Override
    @Transactional
    public void createData(DictItemFrom params) {
        var entity = dictItemConverter.toEntity(params);
        dataService.save(entity);
    }

    @Override
    @Transactional
    public void deleteData(UUID id) {
        var dictData = dataService.getById(id);
        if (null == dictData) {
            throw new DataNotExistException("字典项不存在");
        }
        var group = groupService.getById(dictData.getGid());
        if (group.getBuiltin()) {
            throw new BuiltinDataException("内置字典,无法删除");
        }
        dataService.removeById(id);
    }

    @Override
    @Transactional
    public void modifyData(DictItemFrom params) {
        var group = groupService.getById(params.getGid());
        if (group.getBuiltin()) {
            throw new BuiltinDataException("内置字典,无法修改");
        }
        var entity = dictItemConverter.toEntity(params);
        dataService.updateById(entity);
    }

    @Override
    public @Nullable List<DictGroupTreeVO> listDictGroupWrapTree() {
        // 不能是内置字段,也不能是隐藏字段
        var wrapper = new LambdaQueryWrapper<DictGroup>()
                .eq(DictGroup::getState, Boolean.TRUE)
                .eq(DictGroup::getHide, Boolean.FALSE);
        var menus = groupService.list(wrapper);
        return new TreeBuilder<>(dictGroupConverter.toTreeVOList(menus))
                .buildTree(Common.PID);
    }

    @Override
    public List<DictItemVO> listDictDataByGroupCode(String code) {
        var group = groupService.getByCode(code);
        if (null == group) {
            throw new DataNotExistException("字典类型不存在");
        }
        var dictData = dataService.listByGid(group.getId());
        // 根据sort字段进行一个排序
        dictData.sort(Comparator.comparing(DictItem::getSort));
        return dictItemConverter.toVOList(dictData);
    }
}
