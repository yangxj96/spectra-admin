/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.core.upload.storage;

public record StoredPart(int partNumber, long size, String sha256, String etag) {
}
