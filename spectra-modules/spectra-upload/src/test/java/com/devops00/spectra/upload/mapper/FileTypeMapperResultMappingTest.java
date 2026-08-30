/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.upload.mapper;

import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Ensures annotated file type lookups preserve the JSONB policy columns. */
class FileTypeMapperResultMappingTest {

    private static final List<String> JSONB_COLUMNS = List.of("allowed_extensions", "allowed_content_types", "magic_rules");

    @Test
    void everyFileTypeLookupMustMapJsonbPolicyColumns() throws NoSuchMethodException {
        for (String methodName : List.of("findEnabledByCode", "findEnabledByContentType", "findByIdIncludingDisabled")) {
            Method method = mapperMethod(methodName);
            Results results = method.getAnnotation(Results.class);

            assertThat(results).as("JSONB mapping for %s", methodName).isNotNull();
            assertThat(Arrays.stream(results.value()).map(Result::column).toList())
                    .as("JSONB columns for %s", methodName)
                    .containsAll(JSONB_COLUMNS);
        }
    }

    private Method mapperMethod(String name) throws NoSuchMethodException {
        return switch (name) {
            case "findEnabledByCode" -> FileTypeMapper.class.getMethod(name, String.class);
            case "findEnabledByContentType" -> FileTypeMapper.class.getMethod(name, String.class);
            case "findByIdIncludingDisabled" -> FileTypeMapper.class.getMethod(name, java.util.UUID.class);
            default -> throw new NoSuchMethodException(name);
        };
    }
}
