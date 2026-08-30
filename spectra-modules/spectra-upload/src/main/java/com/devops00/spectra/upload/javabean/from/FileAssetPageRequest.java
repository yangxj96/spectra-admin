/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.upload.javabean.from;

import com.devops00.spectra.upload.javabean.constant.FileAssetStatus;
import com.devops00.spectra.upload.javabean.constant.StorageProviderType;
import lombok.Data;

/** File asset administration filters. */
@Data
public class FileAssetPageRequest {

    private String originalName;

    private String contentSha256;

    private String contentType;

    private StorageProviderType storageProvider;

    private FileAssetStatus status;
}
