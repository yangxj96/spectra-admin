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

package com.devops00.spectra.core.upload.storage.local;

import com.devops00.spectra.core.upload.properties.LocalProperties;
import com.devops00.spectra.core.upload.storage.StorageMultipart;
import com.devops00.spectra.core.upload.storage.StoredPart;
import com.devops00.spectra.framework.properties.SystemProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * 验证 LOCAL 完成拼接后回收暂存分片目录。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/16
 */
class LocalFileStorageProviderCompleteTest {

    @Test
    void completeMultipartRemovesStagingDirectory(@TempDir Path baseDir) throws Exception {
        var local = new LocalProperties();
        local.setStorageRoot("uploads");
        local.setStagingRoot("temp");
        var system = new SystemProperties();
        system.setBaseDir(baseDir.toString());

        UUID uploadId = UUID.randomUUID();
        Path staging = baseDir.resolve("temp").resolve(uploadId.toString());
        Files.createDirectories(staging);
        Files.writeString(staging.resolve("part-1.bin"), "abc");
        Files.writeString(staging.resolve("part-2.bin"), "def");

        var provider = new LocalFileStorageProvider(local, system);
        String key = "assets/" + uploadId + "/content.bin";
        provider.completeMultipart(new StorageMultipart("local", key, uploadId.toString()),
                List.of(new StoredPart(1, 3, "a".repeat(64), "a".repeat(64)),
                        new StoredPart(2, 3, "a".repeat(64), "a".repeat(64))));

        assertEquals("abcdef", Files.readString(baseDir.resolve("uploads").resolve(key)));
        assertFalse(Files.exists(staging));
    }
}
