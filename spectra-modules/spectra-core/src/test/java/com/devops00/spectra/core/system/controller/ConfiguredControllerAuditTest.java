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

import com.devops00.spectra.common.audit.Audit;
import com.devops00.spectra.core.system.javabean.from.ConfiguredBatchFrom;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 防止系统配置秘密值进入审计快照。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
class ConfiguredControllerAuditTest {

    @Test
    void shouldNotCaptureConfiguredValuesInBatchAuditSnapshots() throws NoSuchMethodException {
        var method = ConfiguredController.class.getDeclaredMethod("modifyBatch", ConfiguredBatchFrom.class);
        var audit = method.getAnnotation(Audit.class);

        assertFalse(audit.captureArguments());
    }

    @Test
    void shouldExposeSettingsFormEndpoint() {
        var method = findMethod("settings");

        assertArrayEquals(new String[]{"/settings"}, method.getAnnotation(GetMapping.class).value());
    }

    @Test
    void shouldExposeBatchUpdateWithoutCapturingConfigurationValues() {
        var method = findMethod("modifyBatch");
        var mapping = method.getAnnotation(PutMapping.class);
        var audit = method.getAnnotation(Audit.class);

        assertArrayEquals(new String[]{"/batch"}, mapping.value());
        assertFalse(audit.captureArguments());
    }

    @Test
    void shouldRemoveReplacedSingleItemAndPagedEndpoints() {
        assertFalse(Arrays.stream(ConfiguredController.class.getDeclaredMethods())
                .anyMatch(method -> method.getName().equals("modify") || method.getName().equals("page")));
    }

    private static Method findMethod(String name) {
        var method = Arrays.stream(ConfiguredController.class.getDeclaredMethods())
                .filter(candidate -> candidate.getName().equals(name))
                .findFirst();
        assertNotNull(method.orElse(null), "系统配置控制器应提供 " + name + " 接口");
        return method.orElseThrow();
    }
}
