/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.upload.properties;

import com.devops00.spectra.upload.javabean.constant.StorageProviderType;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileUploadPropertiesTest {

    @Test
    void usesTheProtocolDefaults() {
        var properties = new FileUploadProperties();

        assertEquals(StorageProviderType.LOCAL, properties.getDefaultStorage());
        assertEquals(8L * 1024 * 1024, properties.getChunkSize());
        assertEquals(5L * 1024 * 1024, properties.getMinChunkSize());
        assertEquals(64L * 1024 * 1024, properties.getMaxChunkSize());
        assertEquals(10_000, properties.getMaxParts());
        assertEquals(3, properties.getParallelism());
        assertEquals(Duration.ofHours(24), properties.getTaskTtl());
        assertEquals(Duration.ofHours(2), properties.getIdleTimeout());
        assertEquals(Duration.ofDays(7), properties.getRecordRetention());
        assertEquals(Duration.ofDays(7), properties.getOrphanRetention());
        assertEquals(Duration.ofMinutes(15), properties.getPresignTtl());
    }
}
