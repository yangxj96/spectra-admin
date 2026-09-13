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

package com.devops00.spectra.core.system.controller;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 验证 {@code CacheManagementControllerPermissionTest} 的主要行为、边界条件和回归约束。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
class CacheManagementControllerPermissionTest {

    @Test
    void shouldProtectReadAndBusinessClearEndpoints() throws Exception {
        assertGet("getOverview", "/monitor/overview", "hasPermission(null, 'system:cache:read')");
        assertGet("getRegions", "/monitor/regions", "hasPermission(null, 'system:cache:read')");
        assertGet("getSecurity", "/monitor/security", "hasPermission(null, 'system:cache:read')");
        assertPost("previewBusinessClear", "/admin/business/clear/preview",
                "hasPermission(null, 'system:cache:clear')");
        assertPost("clearBusiness", "/admin/business/clear", "hasPermission(null, 'system:cache:clear')");
    }

    @Test
    void shouldProtectSecurityCandidateQueriesWithTheMatchingOperationPermission() throws Exception {
        assertGetWithParams("getSessionCandidates", "/admin/security/session/candidates",
                "hasPermission(null, 'session:revoke')");
        assertGetWithParams("getVerificationCandidates", "/admin/security/verification/candidates",
                "hasPermission(null, 'security:verification:manage')");
        assertGetWithParams("getLoginFailureCandidates", "/admin/security/login-failure/candidates",
                "hasPermission(null, 'security:login-failure:manage')");
    }

    @Test
    void shouldProtectSecurityOperationsAndKeepNonceRootOnly() throws Exception {
        assertPost("revokeSession", "/admin/security/session/revoke",
                "hasPermission(null, 'session:revoke')");
        assertPost("revokeAllSessions", "/admin/security/session/revoke-all",
                "hasPermission(null, 'session:revoke')");
        assertPost("clearVerification", "/admin/security/verification/clear",
                "hasPermission(null, 'security:verification:manage')");
        assertPost("clearLoginFailure", "/admin/security/login-failure/clear",
                "hasPermission(null, 'security:login-failure:manage')");
        assertPost("invalidateNonce", "/admin/security/nonce/invalidate", "hasRole('ROLE_DEV_OPS')");
        assertPost("invalidateAllNonces", "/admin/security/nonce/invalidate-all", "hasRole('ROLE_DEV_OPS')");
    }

    /**
     * 处理获取相关数据。
     */
    private static void assertGet(String methodName, String path, String permission) throws Exception {
        var method = CacheManagementController.class.getMethod(methodName);
        var mapping = method.getAnnotation(GetMapping.class);
        assertEquals(path, mapping.value()[0]);
        assertEquals("1.0.0", mapping.version());
        assertEquals(permission, method.getAnnotation(PreAuthorize.class).value());
    }

    /**
     * 处理缓存权限相关数据。
     */
    private static void assertPost(String methodName, String path, String permission) throws Exception {
        var method = java.util.Arrays.stream(CacheManagementController.class.getMethods())
                .filter(candidate -> candidate.getName().equals(methodName))
                .findFirst()
                .orElseThrow();
        var mapping = method.getAnnotation(PostMapping.class);
        assertEquals(path, mapping.value()[0]);
        assertEquals("1.0.0", mapping.version());
        assertEquals(permission, method.getAnnotation(PreAuthorize.class).value());
    }

    /**
     * 处理获取相关数据。
     */
    private static void assertGetWithParams(String methodName, String path, String permission) throws Exception {
        var method = java.util.Arrays.stream(CacheManagementController.class.getMethods())
                .filter(candidate -> candidate.getName().equals(methodName))
                .findFirst()
                .orElseThrow();
        var mapping = method.getAnnotation(GetMapping.class);
        assertEquals(path, mapping.value()[0]);
        assertEquals("1.0.0", mapping.version());
        assertEquals(permission, method.getAnnotation(PreAuthorize.class).value());
    }
}
