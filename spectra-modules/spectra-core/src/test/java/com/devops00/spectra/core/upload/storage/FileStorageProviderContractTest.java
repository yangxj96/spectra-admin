/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.core.upload.storage;

import com.devops00.spectra.core.upload.javabean.constant.StorageProviderType;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

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
