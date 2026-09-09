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

package com.devops00.spectra.core.quartz;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Quartz 白名单 Job 定义契约。 */
class QuartzJobDefinitionContractTest {

    @Test
    void jobCatalogMustExposeOnlyCodeOwnedDefinitions() throws IOException {
        var corePaths = List.of(
                "quartz/catalog/QuartzJobCatalog.java",
                "quartz/catalog/QuartzJobRegistrar.java");
        for (var path : corePaths) {
            assertThat(QuartzTestSource.readCoreSource(path)).as("Quartz 白名单定义缺失: %s", path)
                    .containsAnyOf("class ", "interface ", "record ");
        }
        var commonPaths = List.of(
                "port/quartz/QuartzJobDefinition.java",
                "port/quartz/QuartzTriggerTemplate.java",
                "port/quartz/QuartzParameterSchema.java");
        for (var path : commonPaths) {
            assertThat(QuartzTestSource.readCommonSource(path)).as("Quartz 公共白名单定义缺失: %s", path)
                    .containsAnyOf("class ", "interface ", "record ");
        }
    }

    @Test
    void jobDefinitionMustBindQuartzJobTypeAndVersionedParameters() throws IOException {
        var definition = QuartzTestSource.readCommonSource(
                "port/quartz/QuartzJobDefinition.java");
        var schema = QuartzTestSource.readCommonSource(
                "port/quartz/QuartzParameterSchema.java");

        assertThat(definition).contains("QuartzJobDefinition");
        assertThat(definition).contains("Class<? extends Job>");
        assertThat(definition).contains("parameter");
        assertThat(schema).contains("version");
        assertThat(schema).contains("sensitive");
    }
}
