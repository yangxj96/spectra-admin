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

import com.devops00.spectra.core.user.javabean.entity.UserDepartmentMembership;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

/**
 * 用户与部门成员关系 Mapper。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@Mapper
public interface UserDepartmentMembershipMapper {

    /**
     * 查询用户当前有效的关联部门 ID。
     *
     * @param userId 用户 ID
     * @return 当前有效的关联部门 ID 列表
     */
    List<UUID> selectActiveDepartmentIdsByUserId(@Param("userId") UUID userId);

    /**
     * 查询用于授权的数据范围成员部门，包括用户主部门与有效关联部门。
     */
    List<UUID> selectDepartmentIdsForAuthorization(@Param("userId") UUID userId);

    /**
     * 查询匹配指定任一部门成员关系的用户 ID；以 EXISTS 防止多部门产生重复用户。
     */
    List<UUID> selectUserIdsByDepartmentIds(@Param("departmentIds") List<UUID> departmentIds,
                                            @Param("status") String status);

    /**
     * 批量查询用户当前有效的关联部门关系。
     *
     * @param userIds 用户 ID
     * @return 有效关联关系
     */
    List<UserDepartmentMembership> selectActiveByUserIds(@Param("userIds") List<UUID> userIds);

    /** 锁定指定用户在源部门中的活动关联关系，部门重组 Apply 调用方必须处于事务中。 */
    List<UserDepartmentMembership> selectActiveByUsersAndDepartmentsForUpdate(
                                                                              @Param("userIds") List<UUID> userIds,
                                                                              @Param("departmentIds") List<UUID> departmentIds);

    /**
     * 软删除用户当前全部关联部门。
     *
     * @param userId     用户 ID
     * @param operatorId 操作人 ID
     * @return 更新行数
     */
    int softDeleteActiveByUserId(@Param("userId") UUID userId, @Param("operatorId") UUID operatorId);

    /**
     * 批量建立用户关联部门。
     *
     * @param userId        用户 ID
     * @param departmentIds 关联部门 ID 列表
     * @param operatorId    操作人 ID
     * @return 插入行数
     */
    int batchInsertAssociated(
                              @Param("userId") UUID userId,
                              @Param("departmentIds") List<UUID> departmentIds,
                              @Param("operatorId") UUID operatorId);

    /**
     * 软删除指定用户在指定部门中的当前关系。
     *
     * @param userIds       用户 ID
     * @param departmentIds 部门 ID
     * @param operatorId    操作者 ID
     * @return 更新行数
     */
    int softDeleteActiveByUsersAndDepartments(@Param("userIds") List<UUID> userIds,
                                              @Param("departmentIds") List<UUID> departmentIds,
                                              @Param("operatorId") UUID operatorId);

    /**
     * 批量插入重组后的关联部门关系。
     *
     * @param memberships 关联关系
     * @param operatorId  操作者 ID
     * @return 插入行数
     */
    int batchInsertAssociations(@Param("memberships") List<UserDepartmentMembership> memberships,
                                @Param("operatorId") UUID operatorId);
}
