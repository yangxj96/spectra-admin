/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */
package com.devops00.spectra.core.upload.storage;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PartTargetTest {

    @Test
    void protectsHeadersFromExternalMutation() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Length", "10");

        var target = new PartTarget("PUT", "/upload", headers, Instant.EPOCH, 1);
        headers.put("Content-Length", "20");

        assertEquals("10", target.headers().get("Content-Length"));
        assertThrows(UnsupportedOperationException.class, () -> target.headers().put("X-Test", "value"));
    }
}
