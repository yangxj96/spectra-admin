/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.core.upload.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;

import java.lang.reflect.Constructor;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UploadApplicationServiceWiringTest {

    @Test
    void taskExecutorConstructorParameterMustUseFileUploadQualifier() {
        Constructor<?> constructor = Arrays.stream(UploadApplicationService.class.getDeclaredConstructors())
                .filter(candidate -> Arrays.stream(candidate.getParameterTypes()).anyMatch(TaskExecutor.class::equals))
                .findFirst()
                .orElseThrow();
        int taskExecutorIndex = Arrays.asList(constructor.getParameterTypes()).indexOf(TaskExecutor.class);
        Qualifier qualifier = constructor.getParameters()[taskExecutorIndex].getAnnotation(Qualifier.class);

        assertNotNull(qualifier);
        assertEquals("fileUploadTaskExecutor", qualifier.value());
    }

    @Test
    void verificationMustUseAnIndependentWorkerDependency() {
        Constructor<?> constructor = Arrays.stream(UploadApplicationService.class.getDeclaredConstructors())
                .findFirst()
                .orElseThrow();

        assertTrue(Arrays.stream(constructor.getParameterTypes())
                .anyMatch(parameterType -> parameterType.getSimpleName().equals("UploadVerificationWorker")));
        assertTrue(Arrays.stream(constructor.getParameterTypes())
                .noneMatch(parameterType -> parameterType.getName().equals("org.springframework.beans.factory.ObjectProvider")));
    }
}
