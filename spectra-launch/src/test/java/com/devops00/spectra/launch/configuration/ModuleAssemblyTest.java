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

package com.devops00.spectra.launch.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.mock.env.MockEnvironment;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 验证模块装配开关的严格布尔解析。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/16
 */
class ModuleAssemblyTest {

    @Test
    void invalidBooleanTextFailsFast() {
        MockEnvironment environment = new MockEnvironment().withProperty("spectra.modules.oa.enabled", "tru");
        assertThrows(IllegalStateException.class, () -> ModuleAssembly.from(environment));
    }

    @Test
    void explicitBooleansAreAccepted() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("spectra.modules.oa.enabled", "false")
                .withProperty("spectra.modules.workflow.enabled", "TRUE");
        assertEquals(Set.of(ModuleAssembly.CORE, ModuleAssembly.WORKFLOW),
                ModuleAssembly.from(environment).enabledModules());
    }

    @Test
    void missingPropertiesDefaultToEnabled() {
        assertEquals(Set.of(ModuleAssembly.CORE, ModuleAssembly.WORKFLOW, ModuleAssembly.OA),
                ModuleAssembly.from(new MockEnvironment()).enabledModules());
    }

    @Test
    void oaWithoutWorkflowFailsValidation() {
        assertThrows(IllegalStateException.class,
                () -> ModuleAssembly.of(ModuleAssembly.CORE, ModuleAssembly.OA).validate());
    }

    @Test
    void validModuleCombinationsPassValidation() {
        ModuleAssembly.of(ModuleAssembly.CORE).validate();
        ModuleAssembly.of(ModuleAssembly.CORE, ModuleAssembly.WORKFLOW).validate();
        ModuleAssembly.of(ModuleAssembly.CORE, ModuleAssembly.WORKFLOW, ModuleAssembly.OA).validate();
    }

    @Test
    void configurationBeanFailsStartupForOaWithoutWorkflow() {
        new ApplicationContextRunner()
                .withUserConfiguration(ModuleAssemblyConfiguration.class)
                .withPropertyValues("spectra.modules.oa.enabled=true", "spectra.modules.workflow.enabled=false")
                .run(context -> assertTrue(context.getStartupFailure() != null,
                        "OA 启用而 Workflow 关闭时装配上下文应启动失败"));
    }

    @Test
    void configurationBeanStartsForCoreOnly() {
        new ApplicationContextRunner()
                .withUserConfiguration(ModuleAssemblyConfiguration.class)
                .withPropertyValues("spectra.modules.oa.enabled=false", "spectra.modules.workflow.enabled=false")
                .run(context -> assertTrue(context.getStartupFailure() == null,
                        "仅 Core 组合应可装配"));
    }
}
