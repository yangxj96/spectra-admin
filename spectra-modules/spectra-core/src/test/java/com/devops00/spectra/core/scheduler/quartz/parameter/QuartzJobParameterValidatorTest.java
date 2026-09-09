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

package com.devops00.spectra.core.scheduler.quartz.parameter;

import com.devops00.spectra.common.port.scheduler.quartz.QuartzParameterSchema;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** 版本化 JSON 参数的对象、字段类型和敏感字段契约。 */
class QuartzJobParameterValidatorTest {

    private final QuartzJobParameterValidator validator = new QuartzJobParameterValidator(new ObjectMapper());
    private final QuartzParameterSchema schema = new QuartzParameterSchema("v1", Map.of(
            "message", new QuartzParameterSchema.FieldDefinition(QuartzParameterSchema.ValueType.STRING, true, false),
            "count", new QuartzParameterSchema.FieldDefinition(QuartzParameterSchema.ValueType.INTEGER, false, false)),
            false);

    @Test
    void validObjectMustReturnImmutableVersionedSnapshot() {
        var parameters = validator.validate(schema, "{\"version\":\"v1\",\"message\":\"hello\",\"count\":2}");

        assertThat(parameters.version()).isEqualTo("v1");
        assertThat(parameters.values()).containsEntry("message", "hello");
        assertThat(parameters.json()).contains("\"version\":\"v1\"");
        assertThatThrownBy(() -> parameters.values().put("other", "value"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void missingVersionUnknownFieldAndSensitiveFieldMustBeRejected() {
        assertThatThrownBy(() -> validator.validate(schema, "{\"message\":\"hello\"}"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("版本");
        assertThatThrownBy(() -> validator.validate(schema, "{\"version\":\"v1\",\"other\":true}"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("未声明");
        assertThatThrownBy(() -> validator.validate(schema, "{\"version\":\"v1\",\"api_token\":\"secret\"}"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("敏感");
    }

    @Test
    void nonObjectAndWrongTypeMustBeRejected() {
        assertThatThrownBy(() -> validator.validate(schema, "[]"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("对象");
        assertThatThrownBy(() -> validator.validate(schema, "{\"version\":\"v1\",\"message\":3}"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("类型");
    }
}
