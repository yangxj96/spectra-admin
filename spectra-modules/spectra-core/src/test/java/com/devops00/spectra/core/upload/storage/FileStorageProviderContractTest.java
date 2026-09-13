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

import com.devops00.spectra.core.upload.javabean.constant.StorageProviderType;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 验证 {@code FileStorageProviderContractTest} 的主要行为、边界条件和回归约束。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
class FileStorageProviderContractTest {

    @Test
    void keepsStorageApiIndependentFromAwsTypes() {
        assertTrue(Arrays.stream(FileStorageProvider.class.getMethods())
                .map(Method::toGenericString)
                .noneMatch(signature -> signature.contains("software.amazon.awssdk")));
    }

    @Test
    void exposesBothOfficialProviders() {
        assertTrue(Arrays.asList(StorageProviderType.values()).containsAll(Arrays.asList(StorageProviderType.LOCAL, StorageProviderType.S3)));
    }
}
