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

package com.devops00.spectra.core.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.devops00.spectra.core.system.javabean.entity.Department;
import com.devops00.spectra.core.system.javabean.from.DepartmentFrom;
import com.devops00.spectra.core.system.javabean.vo.DepartmentTreeVo;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/// 组织机构业务层
///
/// @author Jack Young
/// @version 1.0
/// @since 2025-6-15
public interface DepartmentService extends IService<Department> {

    /// 新增组织机构
    ///
    /// @param from 请求入参
    void created(DepartmentFrom from);

    /// 根据ID删除组织机构
    ///
    /// @param id ID
    void deleteById(String id);

    /// 编辑组织机构
    ///
    /// @param from 请求入参
    void modify(DepartmentFrom from);

    /// 计算组织机构路径
    ///
    /// @param id 组织机构ID
    /// @return 组织机构路径
    String generatePath(UUID id);

    /// 组织机构树形结构
    ///
    /// @return 组织机构树形结构数组
    @Nullable List<DepartmentTreeVo> tree() throws IllegalAccessException;

    /// 获取自己包含下级的节点的ID
    ///
    /// @return id列表
    Collection<UUID> getSelfAndDescendantIds(UUID departmentId);

    /// 获取所有下级部门 ID（不包含自己）
    ///
    /// @return id列表
    Collection<UUID> getDescendantIds(UUID departmentId);

}
