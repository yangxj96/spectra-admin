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

package com.devops00.spectra.core.upload.controller;

import com.devops00.spectra.core.upload.javabean.from.PartTargetRequest;
import com.devops00.spectra.core.upload.javabean.vo.PartTargetVO;
import com.devops00.spectra.core.upload.service.UploadApplicationService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 验证 {@code FileUploadControllerTargetUrlTest} 的主要行为、边界条件和回归约束。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
class FileUploadControllerTargetUrlTest {

    @Test
    void localTargetMustIncludeCurrentServletContextPath() throws Exception {
        Method targetMethod = targetMethod();

        UUID uploadId = UUID.randomUUID();
        PartTargetRequest request = new PartTargetRequest();
        PartTargetVO target = new PartTargetVO();
        target.setMethod("PUT");
        target.setUrl("/file/uploads/" + uploadId + "/parts/1/content");

        UploadApplicationService service = mock(UploadApplicationService.class);
        when(service.target(uploadId, 1, request)).thenReturn(target);
        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        when(servletRequest.getContextPath()).thenReturn("/api");

        FileUploadController controller = new FileUploadController(service);
        Object result = targetMethod.invoke(controller, uploadId, 1, request, servletRequest);

        assertThat(((PartTargetVO) result).getUrl()).isEqualTo("/api/file/uploads/" + uploadId + "/parts/1/content");
    }

    @Test
    void externalTargetMustNotBePrefixedWithServletContextPath() throws Exception {
        UUID uploadId = UUID.randomUUID();
        PartTargetRequest request = new PartTargetRequest();
        PartTargetVO target = new PartTargetVO();
        target.setMethod("PUT");
        target.setUrl("https://object-storage.example/upload?signature=redacted");

        UploadApplicationService service = mock(UploadApplicationService.class);
        when(service.target(uploadId, 1, request)).thenReturn(target);
        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        when(servletRequest.getContextPath()).thenReturn("/api");

        FileUploadController controller = new FileUploadController(service);
        Object result = targetMethod().invoke(controller, uploadId, 1, request, servletRequest);

        assertThat(((PartTargetVO) result).getUrl()).isEqualTo("https://object-storage.example/upload?signature=redacted");
    }

    /**
     * 处理目标方法相关数据。
     */
    private Method targetMethod() {
        Method targetMethod = java.util.Arrays.stream(FileUploadController.class.getDeclaredMethods())
                .filter(method -> method.getName().equals("target"))
                .filter(method -> java.util.Arrays.asList(method.getParameterTypes()).contains(HttpServletRequest.class))
                .findFirst()
                .orElse(null);
        assertThat(targetMethod).as("target endpoint must receive the current servlet request").isNotNull();
        return targetMethod;
    }
}
