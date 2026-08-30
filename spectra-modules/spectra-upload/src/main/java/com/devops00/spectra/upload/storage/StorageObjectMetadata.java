/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.upload.storage;

public record StorageObjectMetadata(long size, String contentType, String sha256, String etag) {
}
