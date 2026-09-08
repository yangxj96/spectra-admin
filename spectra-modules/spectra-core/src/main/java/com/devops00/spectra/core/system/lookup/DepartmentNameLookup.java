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

package com.devops00.spectra.core.system.lookup;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.devops00.spectra.core.system.javabean.entity.Department;
import com.devops00.spectra.core.system.mapper.DepartmentMapper;
import com.devops00.spectra.framework.assembler.NameLookup;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 部门 ID 到部门路径的批量名称查询适配器。
 * <p>
 * 该适配器只负责展示名称查询，直接读取部门 Mapper，不依赖完整部门 Service，
 * 从而避免名称装配引入 Service 自调用。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/08
 */
@Component
@CacheConfig(cacheNames = "core:dept", keyGenerator = "standardCacheKeyGenerator")
public class DepartmentNameLookup implements NameLookup<UUID> {

    private final DepartmentMapper departmentMapper;

    /**
     * 创建部门名称查询适配器。
     *
     * @param departmentMapper 读取部门 ID 与展示路径的 Mapper
     */
    public DepartmentNameLookup(DepartmentMapper departmentMapper) {
        this.departmentMapper = departmentMapper;
    }

    /**
     * 批量读取部门展示路径。
     *
     * @param ids 需要查询展示路径的部门 ID 集合；null 或空集合表示无需查询
     * @return 部门 ID 到部门路径的映射；没有输入或没有匹配记录时返回空 Map，不返回 null
     */
    @Override
    @Cacheable
    public Map<UUID, String> getNameMap(Set<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return departmentMapper.selectList(Wrappers.<Department>lambdaQuery()
                .select(Department::getId, Department::getPath)
                .in(Department::getId, ids))
                .stream()
                .collect(Collectors.toMap(Department::getId, Department::getPath));
    }
}
