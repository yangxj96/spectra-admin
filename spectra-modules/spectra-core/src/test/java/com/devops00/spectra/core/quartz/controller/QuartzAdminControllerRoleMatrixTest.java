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

package com.devops00.spectra.core.quartz.controller;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Quartz 管理 API 的角色、版本和路径契约测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
class QuartzAdminControllerRoleMatrixTest {

    @Test
    void readEndpointsMustAllowAuditAndEveryEndpointMustUseApiVersion() throws Exception {
        for (var method : QuartzAdminController.class.getDeclaredMethods()) {
            var get = method.getAnnotation(GetMapping.class);
            var post = method.getAnnotation(PostMapping.class);
            if (get == null && post == null) {
                continue;
            }
            var version = get == null ? post.version() : get.version();
            assertThat(version).contains("1.0.0");
            var authorize = method.getAnnotation(PreAuthorize.class);
            assertThat(authorize).isNotNull();
            if (method.getName().equals("jobs")
                    || method.getName().equals("jobTypes")
                    || method.getName().equals("triggerDetail")
                    || method.getName().equals("executionHistory")) {
                assertThat(authorize.value()).contains("ROLE_AUDIT");
            }
        }
    }

    @Test
    void immediateTriggerMustBeDevOpsOnlyAndOldEndpointMustNotExist() {
        Method trigger = find("triggerNow");
        assertThat(trigger.getAnnotation(PreAuthorize.class).value()).isEqualTo("hasRole('ROLE_DEV_OPS')");
        assertThat(QuartzAdminController.class.getAnnotation(org.springframework.web.bind.annotation.RequestMapping.class)
                .value()).containsExactly("/scheduler/quartz");
    }

    /**
     * 查询Quartz角色。
     */
    private Method find(String name) {
        return java.util.Arrays.stream(QuartzAdminController.class.getDeclaredMethods())
                .filter(method -> method.getName().equals(name))
                .findFirst()
                .orElseThrow();
    }
}
