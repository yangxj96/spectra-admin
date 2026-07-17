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

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.devops00.spectra.common.base.BaseEntity;
import com.devops00.spectra.common.base.BaseServiceImpl;
import com.devops00.spectra.common.constant.Common;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.common.exception.NotImplementedException;
import com.devops00.spectra.common.utils.CollUtils;
import com.devops00.spectra.common.utils.TreeBuilder;
import com.devops00.spectra.core.system.javabean.converter.OrganizationConverter;
import com.devops00.spectra.core.system.javabean.entity.Department;
import com.devops00.spectra.core.system.javabean.from.DepartmentFrom;
import com.devops00.spectra.core.system.javabean.vo.DepartmentTreeVo;
import com.devops00.spectra.core.system.mapper.DepartmentMapper;
import com.devops00.spectra.core.system.service.DepartmentService;
import com.devops00.spectra.framework.assembler.NameFillExecutor;
import com.devops00.spectra.framework.assembler.NameLookup;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/// 组织机构业务层-实现
///
/// @author yangxj96
/// @version 1.0
/// @since 2025/6/15 00:00
@Slf4j
@Service
@CacheConfig(cacheNames = "core:dept", keyGenerator = "standardCacheKeyGenerator")
public class DepartmentServiceImpl extends BaseServiceImpl<DepartmentMapper, Department> implements DepartmentService, NameLookup<UUID> {

    private final OrganizationConverter organizationConverter;

    private final NameFillExecutor nameFillExecutor;

    public DepartmentServiceImpl(OrganizationConverter organizationConverter, NameFillExecutor nameFillExecutor) {
        this.organizationConverter = organizationConverter;
        this.nameFillExecutor = nameFillExecutor;
    }

    @Override
    @Cacheable
    public Map<UUID, String> getNameMap(Set<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return lambdaQuery()
                .in(BaseEntity::getId, ids)
                .list()
                .stream()
                .collect(Collectors.toMap(BaseEntity::getId, Department::getPath));
    }

    @Override
    @Transactional
    public void created(DepartmentFrom from) {
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
    public void modify(DepartmentFrom from) {
        var organization = this.getById(from.getId());
        if (null == organization) {
            throw new DataNotExistException("没找到组织机构信息");
        }
        var entity = organizationConverter.toEntity(from);
        this.updateById(entity);
    }

    @Override
    public String generatePath(UUID id) {
        return baseMapper.generatePath(id);
    }

    @Override
    public List<DepartmentTreeVo> tree() throws IllegalAccessException {
        var list = this.list();
        if (CollUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        var vos = organizationConverter.toTreeVOList(list);
        nameFillExecutor.fill(vos);
        return new TreeBuilder<>(vos).buildTree(Common.PID);
    }

    @Override
    @Cacheable
    public Collection<UUID> getSelfAndDescendantIds(UUID departmentId) {
        if (departmentId == null) {
            return Collections.emptySet();
        }

        // 一次性查出所有部门（只查必要字段）
        List<Department> allDepartments = list(
                Wrappers.<Department>lambdaQuery()
                        .select(Department::getId, Department::getPid)
        );

        if (allDepartments.isEmpty()) {
            return Collections.emptySet();
        }

        // 构建 parentId -> childrenIds 映射
        Map<UUID, List<UUID>> childrenMap = buildChildrenMap(allDepartments);

        // DFS 递归获取结果
        Set<UUID> result = new HashSet<>();
        dfs(departmentId, childrenMap, result);

        return result;
    }

    @Override
    @Cacheable
    public Collection<UUID> getDescendantIds(UUID departmentId) {
        Collection<UUID> all = getSelfAndDescendantIds(departmentId);
        all.remove(departmentId);
        return all;
    }

    /// 构建 parentId -> childrenId 列表
    private @NonNull Map<UUID, List<UUID>> buildChildrenMap(@NonNull List<Department> list) {
        Map<UUID, List<UUID>> map = new HashMap<>();

        for (Department dept : list) {
            UUID parentId = dept.getPid();
            map.computeIfAbsent(parentId, _ -> new ArrayList<>())
                    .add(dept.getId());
        }

        return map;
    }

    /// 深度优先遍历（防止死循环）
    ///
    /// @param currentId   当前节点ID
    /// @param childrenMap 子节点map
    /// @param result      响应结果
    private void dfs(UUID currentId, Map<UUID, List<UUID>> childrenMap, @NonNull Set<UUID> result) {

        // 已访问过，直接返回（防止环）
        if (!result.add(currentId)) {
            return;
        }

        List<UUID> children = childrenMap.get(currentId);
        if (children == null || children.isEmpty()) {
            return;
        }

        for (UUID childId : children) {
            dfs(childId, childrenMap, result);
        }
    }

}
