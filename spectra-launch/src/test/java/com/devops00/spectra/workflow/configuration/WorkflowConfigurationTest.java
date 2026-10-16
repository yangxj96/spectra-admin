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

package com.devops00.spectra.workflow.configuration;

import org.flowable.spring.SpringProcessEngineConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 验证流程引擎 schema 更新只跟随配置，缺省为 false，生产不会无条件尝试 DDL。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/16
 */
class WorkflowConfigurationTest {

    @Test
    void schemaUpdateFollowsProperty() {
        var configuration = new WorkflowConfiguration();

        var disabled = new SpringProcessEngineConfiguration();
        configuration.processEngineConfigurer(property("false")).configure(disabled);
        assertEquals("false", disabled.getDatabaseSchemaUpdate());

        var enabled = new SpringProcessEngineConfiguration();
        configuration.processEngineConfigurer(property("true")).configure(enabled);
        assertEquals("true", enabled.getDatabaseSchemaUpdate());

        var absent = new SpringProcessEngineConfiguration();
        configuration.processEngineConfigurer(new MockEnvironment()).configure(absent);
        assertEquals("false", absent.getDatabaseSchemaUpdate());
    }

    private static MockEnvironment property(String value) {
        return new MockEnvironment().withProperty("flowable.database-schema-update", value);
    }
}
