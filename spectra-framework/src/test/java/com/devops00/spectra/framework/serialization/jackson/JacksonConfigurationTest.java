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

package com.devops00.spectra.framework.serialization.jackson;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * API JSON 时间格式和并发序列化契约测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
class JacksonConfigurationTest {

    @Test
    void shouldPreserveLegacyDateFormatAndRoundTrip() throws Exception {
        ObjectMapper mapper = apiMapper();
        Date value = Date.from(Instant.parse("2026-09-08T10:11:12Z"));

        assertThat(mapper.writeValueAsString(value)).isEqualTo("\"2026-09-08 10:11:12\"");
        assertThat(mapper.readValue("\"2026-09-08 10:11:12\"", Date.class)).isEqualTo(value);
    }

    @Test
    void shouldSerializeDatesWithoutCrossThreadState() throws Exception {
        ObjectMapper mapper = apiMapper();
        try (var executor = Executors.newFixedThreadPool(8)) {
            List<Callable<String>> jobs = new ArrayList<>();
            for (int index = 0; index < 64; index++) {
                Date value = Date.from(Instant.parse("2026-09-08T10:11:00Z").plusSeconds(index));
                jobs.add(() -> mapper.writeValueAsString(value));
            }
            List<String> results = executor.invokeAll(jobs).stream().map(future -> {
                try {
                    return future.get();
                } catch (Exception exception) {
                    throw new IllegalStateException(exception);
                }
            }).toList();

            assertThat(results).containsExactlyElementsOf(jobs.stream().map(job -> {
                try {
                    return job.call();
                } catch (Exception exception) {
                    throw new IllegalStateException(exception);
                }
            }).toList());
        }
    }

    @Test
    void shouldKeepUnknownPropertyCompatibilityForApiJson() {
        assertThat(apiMapper().isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)).isFalse();
    }

    /**
     * 处理配置相关数据。
     */
    private static ObjectMapper apiMapper() {
        var builder = JsonMapper.builder();
        JacksonConfiguration configuration = new JacksonConfiguration(new JacksonProperties());
        configuration.jsonMapperBuilderCustomizer().customize(builder);
        return builder.build();
    }
}
