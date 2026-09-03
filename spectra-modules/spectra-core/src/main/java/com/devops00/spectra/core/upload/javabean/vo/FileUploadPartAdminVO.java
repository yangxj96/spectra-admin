/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.core.upload.javabean.vo;

import com.devops00.spectra.core.upload.javabean.constant.UploadPartStatus;
import lombok.Data;

import java.time.LocalDateTime;

/** 上传任务分片管理响应。 */
@Data
public class FileUploadPartAdminVO {

    private Integer partNumber;

    private Long expectedSize;

    private Long uploadedSize;

    private UploadPartStatus status;

    private Integer uploadAttempt;

    private LocalDateTime uploadedAt;
}
