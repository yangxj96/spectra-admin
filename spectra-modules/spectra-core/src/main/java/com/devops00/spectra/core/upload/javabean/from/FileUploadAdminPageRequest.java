/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.core.upload.javabean.from;

import com.devops00.spectra.core.upload.javabean.constant.UploadSessionStatus;
import lombok.Data;

import java.util.UUID;

/** 上传任务管理查询条件。 */
@Data
public class FileUploadAdminPageRequest {

    private String originalName;

    private UUID ownerUserId;

    private UploadSessionStatus status;
}
