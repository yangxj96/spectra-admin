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

package com.devops00.spectra.core.upload.storage;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 验证 {@code PartTargetTest} 的主要行为、边界条件和回归约束。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
class PartTargetTest {

    @Test
    void protectsHeadersFromExternalMutation() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Length", "10");

        var target = new PartTarget("PUT", "/upload", headers, Instant.EPOCH, 1);
        headers.put("Content-Length", "20");

        assertEquals("10", target.headers().get("Content-Length"));
        assertThrows(UnsupportedOperationException.class, () -> target.headers().put("X-Test", "value"));
    }
}
