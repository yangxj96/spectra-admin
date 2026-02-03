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
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.yangxj96.spectra.common.base.BaseEntity;
import io.github.yangxj96.spectra.common.constant.Common;
import io.github.yangxj96.spectra.common.exception.DataNotExistException;
import io.github.yangxj96.spectra.common.exception.NotImplementedException;
import io.github.yangxj96.spectra.common.utils.CollUtils;
import io.github.yangxj96.spectra.common.utils.TreeBuilder;
import io.github.yangxj96.spectra.common.assembler.NameFillExecutor;
import io.github.yangxj96.spectra.common.assembler.NameLookup;
import io.github.yangxj96.spectra.core.javabean.system.converter.OrganizationConverter;
import io.github.yangxj96.spectra.core.javabean.system.entity.Department;
import io.github.yangxj96.spectra.core.javabean.system.from.DepartmentFrom;
import io.github.yangxj96.spectra.core.javabean.system.vo.DepartmentTreeVo;
import io.github.yangxj96.spectra.core.mapper.system.DepartmentMapper;
import io.github.yangxj96.spectra.core.service.system.DepartmentService;
import org.jspecify.annotations.NonNull;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/// 组织机构业务层-实现
///
/// @author Jack Young
/// @version 1.0
/// @since 2025-6-15
@Service
@CacheConfig(cacheNames = "core:dept",keyGenerator = "standardCacheKeyGenerator")
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentService, NameLookup<String> {

    private final OrganizationConverter organizationConverter;

    private final NameFillExecutor nameFillExecutor;

    public DepartmentServiceImpl(OrganizationConverter organizationConverter, NameFillExecutor nameFillExecutor) {
        this.organizationConverter = organizationConverter;
        this.nameFillExecutor = nameFillExecutor;
    }

    @Override
    @Cacheable
    public Map<String, String> getNameMap(Set<String> ids) {
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
    public String generatePath(String id) {
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
    public Collection<String> getSelfAndDescendantIds(String departmentId) {
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
        Map<String, List<String>> childrenMap = buildChildrenMap(allDepartments);

        // DFS 递归获取结果
        Set<String> result = new HashSet<>();
        dfs(departmentId, childrenMap, result);

        return result;
    }

    @Override
    @Cacheable
    public Collection<String> getDescendantIds(String departmentId) {
        Collection<String> all = getSelfAndDescendantIds(departmentId);
        all.remove(departmentId);
        return all;
    }

    /// 构建 parentId -> childrenId 列表
    private @NonNull Map<String, List<String>> buildChildrenMap(@NonNull List<Department> list) {
        Map<String, List<String>> map = new HashMap<>();

        for (Department dept : list) {
            String parentId = dept.getPid();
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
    private void dfs(String currentId, Map<String, List<String>> childrenMap, @NonNull Set<String> result) {

        // 已访问过，直接返回（防止环）
        if (!result.add(currentId)) {
            return;
        }

        List<String> children = childrenMap.get(currentId);
        if (children == null || children.isEmpty()) {
            return;
        }

        for (String childId : children) {
            dfs(childId, childrenMap, result);
        }
    }

}
