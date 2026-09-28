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

package com.devops00.spectra.core.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devops00.spectra.core.user.javabean.entity.User;
import com.devops00.spectra.core.user.javabean.vo.DepartmentMemberCandidateVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

/**
 * 用户mapper层
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/6/14 00:00
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 查询主部门或关联部门直接匹配指定部门的用户，不展开后代部门。
     *
     * @param page         分页参数
     * @param departmentId 活动部门 ID
     * @param keyword      用户名或姓名关键字
     * @return 部门直属成员页
     */
    IPage<DepartmentMemberCandidateVO> selectDepartmentMemberCandidates(
            Page<DepartmentMemberCandidateVO> page,
            @Param("departmentId") UUID departmentId,
            @Param("keyword") String keyword);

    /**
     * 批量将用户主部门从源部门替换为目标部门。
     *
     * @param sourceDepartmentIds 源部门 ID
     * @param targetDepartmentId  新部门 ID
     * @param operatorId          操作者 ID
     * @return 更新行数
     */
    int reassignPrimaryDepartmentsByIds(@Param("sourceDepartmentIds") List<UUID> sourceDepartmentIds,
                                        @Param("targetDepartmentId") UUID targetDepartmentId,
                                        @Param("operatorId") UUID operatorId);

    /**
     * 将选定用户仍指向指定源部门的主部门关系替换为目标部门。
     *
     * @param userIds             用户 ID
     * @param sourceDepartmentId 源部门 ID
     * @param targetDepartmentId 新部门 ID
     * @param operatorId         操作者 ID
     * @return 更新行数
     */
    int reassignPrimaryDepartmentForUsers(@Param("userIds") List<UUID> userIds,
                                          @Param("sourceDepartmentId") UUID sourceDepartmentId,
                                          @Param("targetDepartmentId") UUID targetDepartmentId,
                                          @Param("operatorId") UUID operatorId);

    /** 锁定指定的活动用户，部门重组 Apply 调用方必须处于事务中。 */
    List<User> selectActiveByIdsForUpdate(@Param("userIds") List<UUID> userIds);

}
