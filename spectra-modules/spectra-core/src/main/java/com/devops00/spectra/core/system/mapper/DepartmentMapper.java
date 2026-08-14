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
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

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
     * 清理组织闭包表，调用方必须在同一事务中随后重建。
     */
    @Delete("DELETE FROM spectra_core.sys_department_closure")
    int clearClosure();

    /**
     * 按当前邻接关系重建组织闭包表。
     */
    @Insert("""
            WITH RECURSIVE department_tree AS (
                SELECT id AS ancestor_id, id AS descendant_id, 0 AS depth, ARRAY[id] AS path
                FROM spectra_core.sys_department
                UNION ALL
                SELECT tree.ancestor_id, child.id, tree.depth + 1, tree.path || child.id
                FROM department_tree tree
                JOIN spectra_core.sys_department child ON child.pid = tree.descendant_id
                WHERE NOT child.id = ANY(tree.path)
            )
            INSERT INTO spectra_core.sys_department_closure (ancestor_id, descendant_id, depth)
            SELECT ancestor_id, descendant_id, depth
            FROM department_tree
            """)
    int rebuildClosure();

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
}
