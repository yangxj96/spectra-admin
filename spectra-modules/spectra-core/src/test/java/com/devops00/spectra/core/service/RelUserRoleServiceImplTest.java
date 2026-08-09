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

package com.devops00.spectra.core.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.devops00.spectra.core.user.javabean.entity.RelUserRole;
import com.devops00.spectra.core.user.javabean.entity.Role;
import com.devops00.spectra.core.user.mapper.RelUserRoleMapper;
import com.devops00.spectra.core.user.mapper.RoleMapper;
import com.devops00.spectra.core.user.service.impl.RelUserRoleServiceImpl;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 用户角色关联服务测试
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/7/30
 */
@ExtendWith(MockitoExtension.class)
class RelUserRoleServiceImplTest {

    @Mock
    private RelUserRoleMapper relUserRoleMapper;

    @Mock
    private RoleMapper roleMapper;

    @InjectMocks
    private RelUserRoleServiceImpl service;

    @BeforeEach
    void setUp() {
        var configuration = new MybatisConfiguration();
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(configuration, ""), RelUserRole.class);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(configuration, ""), Role.class);
    }

    @Test
    void getRolesShouldFilterDeletedRelationsAndRoles() {
        var userId = UUID.randomUUID();
        var role = new Role();
        role.setId(UUID.randomUUID());
        when(relUserRoleMapper.selectList(any())).thenReturn(List.of(RelUserRole.builder().userId(userId).roleId(role.getId()).build()));
        when(roleMapper.selectList(any())).thenReturn(List.of(role));

        assertEquals(List.of(role), service.getRoles(userId));
        verify(relUserRoleMapper).selectList(argThat(wrapper -> wrapper.getSqlSegment().contains("deleted")));
        verify(roleMapper).selectList(argThat(wrapper -> wrapper.getSqlSegment().contains("deleted")));
    }
}
