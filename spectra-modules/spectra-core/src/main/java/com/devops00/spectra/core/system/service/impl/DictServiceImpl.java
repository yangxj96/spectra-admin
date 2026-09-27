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
import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.common.foundation.tree.TreeBuilder;
import com.devops00.spectra.core.system.javabean.converter.DictGroupConverter;
import com.devops00.spectra.core.system.javabean.converter.DictItemConverter;
import com.devops00.spectra.core.system.javabean.entity.DictGroup;
import com.devops00.spectra.core.system.javabean.entity.DictItem;
import com.devops00.spectra.core.system.javabean.enums.DictItemState;
import com.devops00.spectra.core.system.javabean.from.DictGroupFrom;
import com.devops00.spectra.core.system.javabean.from.DictItemDefaultFrom;
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
import java.util.Objects;
import java.util.UUID;

/**
 * 字典操作业务层实现
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/6/18 00:00
 */
@Slf4j
@Service
public class DictServiceImpl implements DictService {

    private final DictGroupConverter dictGroupConverter;

    private final DictItemConverter dictItemConverter;

    private final DictGroupService groupService;

    private final DictItemService dataService;

    public DictServiceImpl(DictGroupConverter dictGroupConverter, DictItemConverter dictItemConverter, DictGroupService groupService,
                           DictItemService dataService) {
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
        if (!dataService.listByGid(id).isEmpty()) {
            throw new DataSaveException("字典组包含字典项，不能删除");
        }
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
        DictItemState.fromValue(params.getState());
        requireEditableGroup(params.getGid());
        var entity = dictItemConverter.toEntity(params);
        dataService.save(entity);
    }

    @Override
    @Transactional
    public void modifyData(DictItemFrom params) {
        var current = requireDictItem(params.getId());
        if (!Objects.equals(current.getGid(), params.getGid()) || !Objects.equals(current.getValue(), params.getValue())) {
            throw new DataSaveException("字典项所属字典组和值创建后不能修改，请禁用原项并新增字典项");
        }
        requireEditableGroup(current.getGid());
        var targetState = DictItemState.fromValue(params.getState());
        var entity = dictItemConverter.toEntity(params);
        if (targetState == DictItemState.DISABLED) {
            entity.setDefaultFlag(false);
        }
        dataService.updateById(entity);
    }

    @Override
    @Transactional
    public void enableData(UUID id) {
        changeDataState(id, DictItemState.ENABLED);
    }

    @Override
    @Transactional
    public void disableData(UUID id) {
        changeDataState(id, DictItemState.DISABLED);
    }

    @Override
    @Transactional
    public void setDataDefault(UUID id, DictItemDefaultFrom params) {
        var item = requireDictItem(id);
        requireEditableGroup(item.getGid());
        if (Boolean.TRUE.equals(params.getDefaultFlag())) {
            if (DictItemState.fromValue(item.getState()) != DictItemState.ENABLED) {
                throw new DataSaveException("禁用的字典项不能设为默认");
            }
            for (var sibling : dataService.listByGid(item.getGid())) {
                if (!sibling.getId().equals(item.getId()) && Boolean.TRUE.equals(sibling.getDefaultFlag())) {
                    sibling.setDefaultFlag(false);
                    dataService.updateById(sibling);
                }
            }
            if (!Boolean.TRUE.equals(item.getDefaultFlag())) {
                item.setDefaultFlag(true);
                dataService.updateById(item);
            }
        } else {
            if (Boolean.TRUE.equals(item.getDefaultFlag())) {
                item.setDefaultFlag(false);
                dataService.updateById(item);
            }
        }
    }

    @Override
    public @Nullable List<DictGroupTreeVO> listDictGroupWrapTree() {
        // 不能是内置字段,也不能是隐藏字段
        var wrapper = new LambdaQueryWrapper<DictGroup>().eq(DictGroup::getState, Boolean.TRUE).eq(DictGroup::getHide, Boolean.FALSE);
        var menus = groupService.list(wrapper);
        return new TreeBuilder<>(dictGroupConverter.toTreeVOList(menus)).buildTree(Common.PID);
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

    /**
     * 修改字典项的启用状态。
     *
     * @param id    对应字典项ID
     * @param state 目标状态
     */
    private void changeDataState(UUID id, DictItemState state) {
        var item = requireDictItem(id);
        requireEditableGroup(item.getGid());
        item.setState(state.value());
        if (state == DictItemState.DISABLED) {
            item.setDefaultFlag(false);
        }
        dataService.updateById(item);
    }

    /**
     * 查询字典项，不存在时抛出业务异常。
     *
     * @param id 字典项ID
     * @return 字典项
     */
    private DictItem requireDictItem(UUID id) {
        var item = dataService.getById(id);
        if (item == null) {
            throw new DataNotExistException("字典项不存在");
        }
        return item;
    }

    /**
     * 查询可维护的字典组。
     *
     * @param id 字典组ID
     * @return 字典组
     */
    private DictGroup requireEditableGroup(UUID id) {
        var group = groupService.getById(id);
        if (group == null) {
            throw new DataNotExistException("字典组不存在");
        }
        if (Boolean.TRUE.equals(group.getBuiltin())) {
            throw new BuiltinDataException("内置字典,无法修改");
        }
        return group;
    }
}
