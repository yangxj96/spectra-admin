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

package com.devops00.spectra.core.upload.properties;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 验证文件上传配置绑定约束：负数/非法分片与非法 TTL 被拒绝。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/16
 */
class FileUploadPropertiesValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void defaultValuesPassValidation() {
        assertTrue(validator.validate(new FileUploadProperties()).isEmpty());
    }

    @Test
    void negativeChunkSizeIsRejected() {
        FileUploadProperties properties = new FileUploadProperties();
        properties.setChunkSize(-1L);
        assertFalse(validator.validate(properties).isEmpty());
    }

    @Test
    void chunkSizeOutsideRangeIsRejected() {
        FileUploadProperties properties = new FileUploadProperties();
        properties.setChunkSize(properties.getMinChunkSize() - 1);
        assertFalse(validator.validate(properties).isEmpty());
    }

    @Test
    void nonPositiveTtlIsRejected() {
        FileUploadProperties properties = new FileUploadProperties();
        properties.setPresignTtl(Duration.ZERO);
        assertFalse(validator.validate(properties).isEmpty());
    }

    @Test
    void nonPositiveParallelismIsRejected() {
        FileUploadProperties properties = new FileUploadProperties();
        properties.setParallelism(0);
        assertFalse(validator.validate(properties).isEmpty());
    }
}
