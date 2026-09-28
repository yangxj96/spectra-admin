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

package com.devops00.spectra.core.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.devops00.spectra.core.system.javabean.entity.Department;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

/**
 * 组织机构Mapper
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/6/15 00:00
 */
@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {

    /**
     * 根据ID生成组织机构路径
     * <p>
     * 如:光谱平台/云南分公司/保山分公司/测试小组
     * </p>
     *
     * @param id 组织机构ID
     * @return 组织机构路径
     */
    String generatePath(@Param("id") UUID id);

    /**
     * 查询未软删除的部门 ID。
     *
     * @param departmentIds 待检查部门 ID
     * @return 当前有效的部门 ID
     */
    List<UUID> selectActiveIdsByIds(@Param("departmentIds") List<UUID> departmentIds);

    /**
     * 批量查询未软删除的部门摘要。
     *
     * @param departmentIds 部门 ID
     * @return 未软删除的部门记录
     */
    List<Department> selectActiveByIds(@Param("departmentIds") List<UUID> departmentIds);

    /** 锁定并查询未软删除的源部门。 */
    List<Department> selectActiveByIdsForUpdate(@Param("departmentIds") List<UUID> departmentIds);

    /** 查询源部门的活动直属子部门。 */
    List<Department> selectActiveChildrenByParentIds(@Param("parentIds") List<UUID> parentIds);

    /** 锁定源部门的活动直属子部门。 */
    List<Department> selectActiveChildrenByParentIdsForUpdate(@Param("parentIds") List<UUID> parentIds);

    /** 将源部门直属子部门重挂到新部门。 */
    int moveActiveChildrenToParent(@Param("childIds") List<UUID> childIds,
                                   @Param("sourceParentIds") List<UUID> sourceParentIds,
                                   @Param("targetParentId") UUID targetParentId,
                                   @Param("operatorId") UUID operatorId);

    /** 软删除源部门并推进乐观锁版本。 */
    int softDeleteActiveByIds(@Param("departmentIds") List<UUID> departmentIds,
                              @Param("operatorId") UUID operatorId);

    /** 根据父子关系重算所有活动部门路径。 */
    int rebuildActivePaths(@Param("operatorId") UUID operatorId);

    /** 按 ID 读取部门路径，包含逻辑删除部门，供历史名称解析专用。 */
    List<Department> selectByIdsIncludingDeleted(@Param("departmentIds") List<UUID> departmentIds);
}
