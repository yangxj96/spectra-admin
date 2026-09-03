/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.core.upload.storage;

import java.time.Instant;
import java.util.Map;

public record PartTarget(String method, String url, Map<String, String> headers, Instant expiresAt, int attempt) {

    public PartTarget {
        headers = Map.copyOf(headers);
    }
}
