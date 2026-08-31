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

package com.devops00.spectra.upload.api;

import com.devops00.spectra.common.port.file.FileAssetPort;
import com.devops00.spectra.common.port.file.FileAssetSnapshot;
import com.devops00.spectra.common.port.file.FileDownload;
import com.devops00.spectra.common.port.file.FileReferenceCommand;
import com.devops00.spectra.common.port.file.FileReferenceKey;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 跨模块文件端口契约测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/08/30
 */
class FileUploadPortContractTest {

    @Test
    void fileAssetPortOnlyExposesDomainSnapshotsAndStreams() {
        var methods = Arrays.stream(FileAssetPort.class.getDeclaredMethods()).toList();

        assertThat(methods).extracting(Method::getName)
                .containsExactlyInAnyOrder("requireReady", "requireReadyForReference", "open");
        assertThat(methods).allMatch(method -> Arrays.stream(method.getParameterTypes())
                .noneMatch(type -> type.getName().startsWith("com.baomidou")
                        || type.getName().startsWith("software.amazon.awssdk")
                        || type.getName().contains("Mapper")));
        assertThat(FileAssetSnapshot.class.getDeclaredFields()).extracting(field -> field.getType().getName())
                .doesNotContain("com.devops00.spectra.upload.javabean.entity.FileAsset");

        var open = methods.stream().filter(method -> method.getName().equals("open")).findFirst().orElseThrow();
        assertThat(open.getReturnType()).isEqualTo(FileDownload.class);
        assertThat(FileDownload.class).hasDeclaredMethods("close");
        assertThat(InputStream.class).isAssignableFrom(FileDownload.class.getRecordComponents()[0].getType());
    }

    @Test
    void referenceCommandUsesBusinessReferenceWithoutTenantField() {
        var fields = Arrays.stream(FileReferenceCommand.class.getRecordComponents()).map(component -> component.getName()).toList();

        assertThat(fields).containsExactly("fileAssetId", "referenceType", "referenceId", "purpose", "displayName");
        assertThat(fields).doesNotContain("tenantId", "fileId");
        assertThat(FileReferenceKey.class.getRecordComponents()).extracting(component -> component.getName())
                .containsExactly("referenceType", "referenceId", "purpose");
    }
}
