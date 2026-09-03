/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
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

/** Verifies that Local part targets include the servlet context path. */
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
