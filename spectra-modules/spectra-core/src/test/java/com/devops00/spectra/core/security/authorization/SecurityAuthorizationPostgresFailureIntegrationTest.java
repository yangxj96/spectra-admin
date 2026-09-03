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

package com.devops00.spectra.core.security.authorization;

import com.devops00.spectra.core.security.authorization.service.impl.JdbcAuthorizationSnapshotLoader;
import com.devops00.spectra.common.port.security.SecuritySessionRevocationPort;
import com.devops00.spectra.core.security.authorization.mapper.AssignmentGrantBoundaryMapper;
import com.devops00.spectra.core.security.authorization.mapper.AssignmentPermissionBoundaryMapper;
import com.devops00.spectra.core.security.authorization.mapper.AuthorizationScopeMapper;
import com.devops00.spectra.core.security.authorization.mapper.PermissionMapper;
import com.devops00.spectra.core.security.authorization.mapper.RoleAssignmentMapper;
import com.devops00.spectra.core.security.authorization.mapper.RoleGrantablePermissionMapper;
import com.devops00.spectra.core.security.authorization.mapper.RolePermissionMapper;
import com.devops00.spectra.core.security.authorization.mapper.ScopeRuleMapper;
import com.devops00.spectra.core.security.authorization.mapper.SecurityRoleMapper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import jakarta.annotation.Resource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** 真实 PostgreSQL 不可用时，授权快照加载必须失败，不得降级为空权限。 */
@EnabledIfEnvironmentVariable(named = "SPECTRA_SECURITY_POSTGRES_FAILURE_TEST", matches = "true")
@Tag("manual-integration")
@SpringBootTest(classes = SecurityAuthorizationPostgresFailureIntegrationTest.TestApplication.class, properties = {
        "spring.autoconfigure.exclude="
                + "com.devops00.spectra.core.CoreModule,"
                + "com.devops00.spectra.framework.FrameworkModule"
})
class SecurityAuthorizationPostgresFailureIntegrationTest {

    @Resource
    private JdbcAuthorizationSnapshotLoader authorizationSnapshotLoader;

    @Test
    void shouldFailClosedWhenPostgresIsUnavailable() {
        assertThatThrownBy(() -> authorizationSnapshotLoader.load(UUID.randomUUID()))
                .isInstanceOf(DataAccessException.class);
    }

    @Configuration(proxyBeanMethods = false)
    @EnableAutoConfiguration
    @MapperScan("com.devops00.spectra.core.security.authorization.mapper")
    static class TestApplication {

        @Bean
        JdbcAuthorizationSnapshotLoader authorizationSnapshotLoader(
                                                                    RoleAssignmentMapper roleAssignmentMapper,
                                                                    SecurityRoleMapper securityRoleMapper,
                                                                    RolePermissionMapper rolePermissionMapper,
                                                                    RoleGrantablePermissionMapper roleGrantablePermissionMapper,
                                                                    PermissionMapper permissionMapper,
                                                                    AuthorizationScopeMapper authorizationScopeMapper,
                                                                    ScopeRuleMapper scopeRuleMapper,
                                                                    AssignmentPermissionBoundaryMapper permissionBoundaryMapper,
                                                                    AssignmentGrantBoundaryMapper grantBoundaryMapper) {
            return new JdbcAuthorizationSnapshotLoader(
                    roleAssignmentMapper,
                    securityRoleMapper,
                    rolePermissionMapper,
                    roleGrantablePermissionMapper,
                    permissionMapper,
                    authorizationScopeMapper,
                    scopeRuleMapper,
                    permissionBoundaryMapper,
                    grantBoundaryMapper);
        }

        @Bean
        SecuritySessionRevocationPort securitySessionRevocationPort() {
            return ignored -> {
                // 故障测试不创建安全 Session，因此无需撤销外部 Session。
            };
        }
    }
}
