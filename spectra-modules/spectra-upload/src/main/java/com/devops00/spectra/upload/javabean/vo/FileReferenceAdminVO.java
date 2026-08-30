/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.upload.javabean.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/** 文件引用管理响应。 */
@Data
public class FileReferenceAdminVO {

    private UUID referenceId;

    private UUID fileAssetId;

    private String assetOriginalName;

    private String assetContentSha256;

    private Long assetSize;

    private String assetContentType;

    private String referenceType;

    private UUID businessReferenceId;

    private String purpose;

    private String displayName;

    private UUID createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
