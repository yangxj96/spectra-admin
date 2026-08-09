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

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 文档展示对象。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/7
 */
@Data
public class DocumentVO {

    /**
     * 主键 ID。
     */
    private UUID id;

    /**
     * 目录 ID。
     */
    private UUID folderId;

    /**
     * 部门 ID。
     */
    private UUID departmentId;

    /**
     * 标题。
     */
    private String title;

    /**
     * 摘要。
     */
    private String summary;

    /**
     * 状态。
     */
    private String status;

    /**
     * 可见范围。
     */
    private String visibility;

    /**
     * 所有者 ID。
     */
    private UUID ownerId;

    /**
     * 发布时间。
     */
    private LocalDateTime publishedAt;

    /**
     * 创建时间。
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间。
     */
    private LocalDateTime updatedAt;

    /**
     * 当前版本字段。
     */
    private DocumentVersionVO currentVersion;
}
