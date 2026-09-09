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

package com.devops00.spectra.core.scheduler.quartz.catalog;

import com.devops00.spectra.common.port.scheduler.quartz.QuartzJobDefinition;
import com.devops00.spectra.common.port.scheduler.quartz.QuartzParameterSchema;
import com.devops00.spectra.common.port.scheduler.quartz.QuartzTriggerTemplate;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Quartz 白名单目录的重复和受信任类型契约。 */
class QuartzJobCatalogTest {

    @Test
    void duplicateTypeKeyMustBeRejected() {
        var first = definition("duplicate", false, Optional.empty());
        var second = definition("duplicate", false, Optional.empty());

        assertThatThrownBy(() -> new QuartzJobCatalog(List.of(first, second)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("重复");
    }

    @Test
    void builtInDefinitionMustHaveFixedJobKey() {
        var definition = definition("built-in", true, Optional.empty());

        assertThatThrownBy(() -> new QuartzJobCatalog(List.of(definition)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("固定 JobKey");
    }

    @Test
    void catalogMustBeImmutableAndFindByType() {
        var definition = definition("sample", false, Optional.empty());
        var catalog = new QuartzJobCatalog(List.of(definition));

        assertThat(catalog.find("sample")).contains(definition);
        assertThat(catalog.definitions()).containsExactly(definition);
        assertThatThrownBy(() -> catalog.definitions().clear()).isInstanceOf(UnsupportedOperationException.class);
    }

    private QuartzJobDefinition definition(String key, boolean builtIn, Optional<String> builtInJobKey) {
        return new QuartzJobDefinition() {
            @Override
            public String typeKey() {
                return key;
            }

            @Override
            public String displayName() {
                return key;
            }

            @Override
            public Class<? extends Job> jobClass() {
                return SampleJob.class;
            }

            @Override
            public boolean builtIn() {
                return builtIn;
            }

            @Override
            public Optional<String> builtInJobKey() {
                return builtInJobKey;
            }

            @Override
            public QuartzParameterSchema parameterSchema() {
                return QuartzParameterSchema.empty();
            }

            @Override
            public Optional<QuartzTriggerTemplate> defaultTrigger() {
                return Optional.empty();
            }
        };
    }

    /** 测试用 Quartz Job。 */
    private static final class SampleJob implements Job {

        @Override
        public void execute(JobExecutionContext context) {
        }
    }
}
