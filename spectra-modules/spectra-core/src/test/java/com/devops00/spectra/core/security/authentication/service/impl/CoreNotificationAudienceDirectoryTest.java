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

package com.devops00.spectra.core.security.authentication.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.devops00.spectra.common.notification.NotificationAudience;
import com.devops00.spectra.core.security.authorization.entity.RoleAssignment;
import com.devops00.spectra.core.security.authorization.entity.SecurityRole;
import com.devops00.spectra.core.security.authorization.mapper.RoleAssignmentMapper;
import com.devops00.spectra.core.security.authorization.mapper.SecurityRoleMapper;
import com.devops00.spectra.core.system.service.DepartmentService;
import com.devops00.spectra.core.user.javabean.constant.UserStatus;
import com.devops00.spectra.core.user.javabean.entity.User;
import com.devops00.spectra.core.user.service.UserService;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.ObjectTypeHandler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 受控发送部门、角色和明确用户范围展开测试。
 */
class CoreNotificationAudienceDirectoryTest {

    private static final UUID USER_FROM_DEPARTMENT = UUID.randomUUID();

    private static final UUID USER_FROM_ROLE = UUID.randomUUID();

    private static final UUID DEPARTMENT_ID = UUID.randomUUID();

    private static final UUID CHILD_DEPARTMENT_ID = UUID.randomUUID();

    private static final UUID ROLE_ID = UUID.randomUUID();

    @BeforeAll
    static void registerMybatisLambdaMetadata() {
        var configuration = new MybatisConfiguration();
        configuration.getTypeHandlerRegistry().register(UUID.class, JdbcType.OTHER, ObjectTypeHandler.class);
        var assistant = new MapperBuilderAssistant(configuration, "notification-audience-test");
        TableInfoHelper.initTableInfo(assistant, User.class);
        TableInfoHelper.initTableInfo(assistant, SecurityRole.class);
        TableInfoHelper.initTableInfo(assistant, RoleAssignment.class);
    }

    @Test
    void shouldExpandDepartmentRoleAndExplicitUserWithoutDuplicates() {
        var users = mock(UserService.class);
        var departments = mock(DepartmentService.class);
        var assignments = mock(RoleAssignmentMapper.class);
        var roles = mock(SecurityRoleMapper.class);
        var departmentUser = new User();
        departmentUser.setId(USER_FROM_DEPARTMENT);
        departmentUser.setStatus(UserStatus.ACTIVE);
        var role = new SecurityRole();
        role.setId(ROLE_ID);
        role.setState("ACTIVE");
        var assignment = new RoleAssignment();
        assignment.setUserId(USER_FROM_ROLE);
        assignment.setRoleId(ROLE_ID);
        assignment.setState("ACTIVE");
        assignment.setValidFrom(Instant.now().minusSeconds(1));
        when(departments.getSelfAndDescendantIds(DEPARTMENT_ID))
                .thenReturn(List.of(DEPARTMENT_ID, CHILD_DEPARTMENT_ID));
        when(users.list(any(LambdaQueryWrapper.class))).thenReturn(List.of(departmentUser));
        when(roles.selectList(any())).thenReturn(List.of(role));
        when(assignments.selectList(any())).thenReturn(List.of(assignment));

        var directory = new CoreNotificationAudienceDirectory(users, departments, assignments, roles);
        var result = directory.resolve(new NotificationAudience(List.of(USER_FROM_DEPARTMENT),
                List.of(DEPARTMENT_ID), List.of(ROLE_ID)));

        assertEquals(List.of(USER_FROM_DEPARTMENT, USER_FROM_ROLE), result);
    }
}
