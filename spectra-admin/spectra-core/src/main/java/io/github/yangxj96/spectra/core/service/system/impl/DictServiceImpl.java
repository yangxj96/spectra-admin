/*
 *  Copyright 2018-2025 yangxj96
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

package io.github.yangxj96.spectra.core.service.system.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.yangxj96.spectra.common.constant.Common;
import io.github.yangxj96.spectra.common.exception.BuiltinDataException;
import io.github.yangxj96.spectra.common.exception.DataNotExistException;
import io.github.yangxj96.spectra.common.utils.TreeBuilder;
import io.github.yangxj96.spectra.core.configure.datascope.DataScope;
import io.github.yangxj96.spectra.core.javabean.system.converter.DictConverter;
import io.github.yangxj96.spectra.core.javabean.system.entity.DictItem;
import io.github.yangxj96.spectra.core.javabean.system.entity.DictGroup;
import io.github.yangxj96.spectra.core.javabean.system.from.DictDataFrom;
import io.github.yangxj96.spectra.core.javabean.system.from.DictGroupFrom;
import io.github.yangxj96.spectra.core.javabean.system.vo.DictDataVo;
import io.github.yangxj96.spectra.core.javabean.system.vo.DictTypeTreeVO;
import io.github.yangxj96.spectra.core.service.system.DictItemService;
import io.github.yangxj96.spectra.core.service.system.DictGroupService;
import io.github.yangxj96.spectra.core.service.system.DictService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
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
    private DictConverter dictConverter;

    @Resource
    private DictGroupService groupService;

    @Resource
    private DictItemService dataService;


    @Override
    @Transactional
    public void createGroup(DictGroupFrom params) {
        var entity = dictConverter.groupFromToEntity(params);
        groupService.save(entity);
    }

    @Override
    @Transactional
    public void deleteGroup(Long id) {
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
        var entity = dictConverter.groupFromToEntity(params);
        groupService.updateById(entity);
    }

    @Override
    @Transactional
    public void createData(DictDataFrom params) {
        var entity = dictConverter.dataFromToEntity(params);
        dataService.save(entity);
    }

    @Override
    @Transactional
    public void deleteData(Long id) {
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
    public void modifyData(DictDataFrom params) {
        var group = groupService.getById(params.getGid());
        if (group.getBuiltin()) {
            throw new BuiltinDataException("内置字典,无法修改");
        }
        var entity = dictConverter.dataFromToEntity(params);
        dataService.updateById(entity);
    }

    @Override
    public @Nullable List<DictTypeTreeVO> listDictGroupWrapTree() {
        // 不能是内置字段,也不能是隐藏字段
        var wrapper = new LambdaQueryWrapper<DictGroup>()
                .eq(DictGroup::getState, 0)
                .eq(DictGroup::getHide, Boolean.FALSE);
        var menus = groupService.list(wrapper);
        var vos = dictConverter.typeToTreeVOS(menus);
        return new TreeBuilder<>(vos).buildTree(Common.PID);
    }

    @Override
    @DataScope
    public List<DictDataVo> listDictDataByGroupCode(String code) {
        var group = groupService.getByCode(code);
        if (null == group) {
            throw new DataNotExistException("字典类型不存在");
        }
        var dictData = dataService.listByGid(group.getId());
        // 根据sort字段进行一个排序
        dictData.sort(Comparator.comparing(DictItem::getSort));
        return dictConverter.dataToVos(dictData);
    }
}
