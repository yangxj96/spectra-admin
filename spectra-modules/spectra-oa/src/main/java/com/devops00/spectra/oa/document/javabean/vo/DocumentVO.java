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

/// 文档展示对象。
/// @author yangxj96
/// @version 1.0
/// @since 2026/8/7
@Data
public class DocumentVO {
    private UUID id;
    private UUID folderId;
    private UUID departmentId;
    private String title;
    private String summary;
    private String status;
    private String visibility;
    private UUID ownerId;
    private Instant publishedAt;
    private Instant createdAt;
    private Instant updatedAt;
    private DocumentVersionVO currentVersion;
}
