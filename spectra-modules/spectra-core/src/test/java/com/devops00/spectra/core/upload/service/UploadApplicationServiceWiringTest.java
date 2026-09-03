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
}
