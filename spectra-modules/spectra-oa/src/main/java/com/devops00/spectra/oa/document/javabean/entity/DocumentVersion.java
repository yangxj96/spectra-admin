/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */
package com.devops00.spectra.oa.document.javabean.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

/// OA 文档版本实体。
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/8/7
@Getter
@Setter
@ToString
@TableName(value = "oa_document_version", schema = "spectra_oa")
public class DocumentVersion extends BaseEntity {
    @TableField("document_id")
    private UUID documentId;
    @TableField("version_no")
    private Integer versionNo;
    @TableField("file_id")
    private UUID fileId;
    @TableField("file_name")
    private String fileName;
    @TableField("file_size")
    private Long fileSize;
    @TableField("content_type")
    private String contentType;
    @TableField("version_note")
    private String versionNote;
    @TableField("is_current")
    private Boolean currentVersion;
}
