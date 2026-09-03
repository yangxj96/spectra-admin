/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.core.upload.storage;

import com.devops00.spectra.core.upload.javabean.constant.StorageProviderType;

import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface FileStorageProvider {

    StorageProviderType type();

    StorageMultipart createMultipart(UUID uploadId, String container, String key, int totalParts);

    PartTarget createPartTarget(StorageMultipart multipart, int partNumber, long partSize, String partSha256,
                                Instant expiresAt, int attempt);

    StoredPart putLocalPart(StorageMultipart multipart, int partNumber, InputStream content, long expectedSize,
                            String expectedSha256);

    StoredPart confirmExternalPart(StorageMultipart multipart, int partNumber, long expectedSize, String expectedSha256,
                                   String providerEtag);

    void completeMultipart(StorageMultipart multipart, List<StoredPart> parts);

    void abortMultipart(StorageMultipart multipart);

    StorageObject open(String container, String key, Long rangeStart, Long rangeEnd);

    void delete(String container, String key);

    boolean exists(String container, String key);

    StorageHealth health();
}
