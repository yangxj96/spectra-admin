/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.upload.javabean.from;

import lombok.Data;

import java.util.UUID;

/** 文件引用管理查询条件。 */
@Data
public class FileReferencePageRequest {

    private UUID fileAssetId;

    private String referenceType;

    private UUID referenceId;

    private String purpose;

    private String displayName;
}
