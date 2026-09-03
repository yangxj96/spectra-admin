/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.core.upload.javabean.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/** 上传任务管理详情响应。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FileUploadAdminDetailVO extends FileUploadAdminVO {

    private List<FileUploadPartAdminVO> parts;
}
