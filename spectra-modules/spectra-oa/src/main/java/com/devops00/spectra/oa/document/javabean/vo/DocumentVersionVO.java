/*
 * Copyright 2018-2026 yangxj96
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.devops00.spectra.oa.document.javabean.vo;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

/// 文档版本展示对象。
/// @author yangxj96
/// @version 1.0
/// @since 2026/8/7
@Data
public class DocumentVersionVO {
    private UUID id;
    private Integer versionNo;
    private UUID fileId;
    private String fileName;
    private Long fileSize;
    private String contentType;
    private String versionNote;
    private Boolean current;
    private Instant createdAt;
}
