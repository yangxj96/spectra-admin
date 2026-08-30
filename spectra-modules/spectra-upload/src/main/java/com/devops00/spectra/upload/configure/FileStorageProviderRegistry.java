/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.upload.configure;

import com.devops00.spectra.upload.api.FileErrorCode;
import com.devops00.spectra.upload.api.FileUploadException;
import com.devops00.spectra.upload.javabean.constant.StorageProviderType;
import com.devops00.spectra.upload.storage.FileStorageProvider;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;

@Component
public class FileStorageProviderRegistry {

    private final EnumMap<StorageProviderType, FileStorageProvider> providers = new EnumMap<>(StorageProviderType.class);

    public FileStorageProviderRegistry(List<FileStorageProvider> providers) {
        providers.forEach(provider -> this.providers.put(provider.type(), provider));
    }

    public FileStorageProvider require(StorageProviderType type) {
        var provider = providers.get(type);
        if (provider == null) {
            throw new FileUploadException(FileErrorCode.FILE_STORAGE_UNAVAILABLE, "storage provider is unavailable: " + type);
        }
        return provider;
    }
}
