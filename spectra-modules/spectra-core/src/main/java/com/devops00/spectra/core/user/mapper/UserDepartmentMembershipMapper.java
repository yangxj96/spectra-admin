/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.core.user.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.UUID;

/** 用户与部门成员关系 Mapper。 */
@Mapper
public interface UserDepartmentMembershipMapper {

    /**
     * 为用户建立主部门关系。
     *
     * @param userId       用户 ID
     * @param departmentId 部门 ID
     * @return 受影响行数
     */
    @Insert("""
            INSERT INTO spectra_core.sys_user_department_membership
                (user_id, department_id, membership_type, created_by, updated_by)
            VALUES (#{userId}, #{departmentId}, 'PRIMARY', #{userId}, #{userId})
            """)
    /**
     * 处理内部业务逻辑（{@code insertPrimary}）。
     */
    int insertPrimary(@Param("userId") UUID userId, @Param("departmentId") UUID departmentId);
}
