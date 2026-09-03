/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.core.upload.javabean.vo;

import lombok.Data;
import tools.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.util.UUID;

/** 文件类型策略管理响应。 */
@Data
public class FileTypePolicyVO {

    private UUID id;

    private String code;

    private String displayName;

    private JsonNode allowedExtensions;

    private JsonNode allowedContentTypes;

    private JsonNode magicRules;

    private Long maxSize;

    private Boolean previewEnabled;

    private Boolean downloadEnabled;

    private Boolean uploadEnabled;

    private Boolean dangerous;

    private Boolean enabled;

    private Long version;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
