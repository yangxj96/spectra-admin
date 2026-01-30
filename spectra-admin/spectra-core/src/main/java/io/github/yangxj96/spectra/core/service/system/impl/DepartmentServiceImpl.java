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

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.yangxj96.spectra.common.constant.Common;
import io.github.yangxj96.spectra.common.exception.DataNotExistException;
import io.github.yangxj96.spectra.common.exception.NotImplementedException;
import io.github.yangxj96.spectra.common.utils.CollUtils;
import io.github.yangxj96.spectra.common.utils.TreeBuilder;
import io.github.yangxj96.spectra.core.javabean.system.converter.OrganizationConverter;
import io.github.yangxj96.spectra.core.javabean.system.entity.Department;
import io.github.yangxj96.spectra.core.javabean.system.from.OrganizationFrom;
import io.github.yangxj96.spectra.core.javabean.system.vo.OrganizationTreeVo;
import io.github.yangxj96.spectra.core.mapper.system.DepartmentMapper;
import io.github.yangxj96.spectra.core.service.system.DepartmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/// 组织机构业务层-实现
///
/// @author Jack Young
/// @version 1.0
/// @since 2025-6-15
@Service
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentService {

    private final OrganizationConverter organizationConverter;

    public DepartmentServiceImpl(OrganizationConverter organizationConverter) {
        this.organizationConverter = organizationConverter;
    }


    @Override
    @Transactional
    public void created(OrganizationFrom from) {
        var entity = organizationConverter.toEntity(from);
        entity.setCode(IdWorker.get32UUID().toUpperCase());
        this.save(entity);
    }

    @Override
    @Transactional
    public void deleteById(String id) {
        // this.removeById(Long.parseLong(id))
        throw new NotImplementedException("暂未实现");
    }

    @Override
    @Transactional
    public void modify(OrganizationFrom from) {
        var organization = this.getById(from.getId());
        if (null == organization) {
            throw new DataNotExistException("没找到组织机构信息");
        }
        var entity = organizationConverter.toEntity(from);
        this.updateById(entity);
    }

    @Override
    public String generatePath(String id) {
        return baseMapper.generatePath(id);
    }

    @Override
    public List<OrganizationTreeVo> tree() {
        var list = this.list();
        if (CollUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        var vos = organizationConverter.toTreeVOList(list);
        return new TreeBuilder<>(vos).buildTree(Common.PID);
    }

    @Override
    public List<Department> getAllChildrenById(String organizationId) {
        var organizations = this.list();
        // 2. 构建父ID -> 子节点列表的映射
        var childrenMap = organizations.stream()
                .filter(org -> org.getPid() != null)
                .collect(Collectors.groupingBy(Department::getPid));
        // 3. 存放结果的集合
        var result = new ArrayList<Department>();
        // 4. 从指定ID开始递归收集所有子节点
        collectAllChildren(organizationId, childrenMap, result);
        return result;
    }


    /**
     * 递归收集指定节点的所有子节点
     *
     * @param parentId    要查找子节点的父节点ID
     * @param childrenMap 父ID -> 子节点列表的映射
     * @param result      收集结果的列表
     */
    private void collectAllChildren(String parentId, Map<String, List<Department>> childrenMap, List<Department> result) {
        // 获取该父节点的所有直接子节点
        var directChildren = childrenMap.get(parentId);
        // 如果没有子节点，直接返回（递归终止条件）
        if (directChildren == null || directChildren.isEmpty()) {
            return;
        }
        // 将所有直接子节点添加到结果中
        result.addAll(directChildren);
        // 递归处理每个直接子节点，查找它们的子节点
        for (Department child : directChildren) {
            collectAllChildren(child.getId(), childrenMap, result);
        }
    }
}
