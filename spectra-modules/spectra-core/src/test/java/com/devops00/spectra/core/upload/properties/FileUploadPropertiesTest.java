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

import com.devops00.spectra.core.upload.javabean.constant.StorageProviderType;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 验证 {@code FileUploadPropertiesTest} 的主要行为、边界条件和回归约束。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
class FileUploadPropertiesTest {

    @Test
    void usesTheProtocolDefaults() {
        var properties = new FileUploadProperties();

        assertEquals(StorageProviderType.LOCAL, properties.getDefaultStorage());
        assertEquals(8L * 1024 * 1024, properties.getChunkSize());
        assertEquals(5L * 1024 * 1024, properties.getMinChunkSize());
        assertEquals(64L * 1024 * 1024, properties.getMaxChunkSize());
        assertEquals(10_000, properties.getMaxParts());
        assertEquals(3, properties.getParallelism());
        assertEquals(Duration.ofHours(24), properties.getTaskTtl());
        assertEquals(Duration.ofHours(2), properties.getIdleTimeout());
        assertEquals(Duration.ofDays(7), properties.getRecordRetention());
        assertEquals(Duration.ofDays(7), properties.getOrphanRetention());
        assertEquals(Duration.ofMinutes(15), properties.getPresignTtl());
    }
}
