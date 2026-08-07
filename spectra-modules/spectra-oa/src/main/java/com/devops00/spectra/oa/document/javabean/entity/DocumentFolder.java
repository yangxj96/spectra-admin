/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */
package com.devops00.spectra.oa.document.javabean.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.annotation.DataScope;
import com.devops00.spectra.common.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

/// OA 文档目录实体。
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/8/7
@Getter
@Setter
@ToString
@TableName(value = "oa_document_folder", schema = "spectra_oa")
@DataScope
public class DocumentFolder extends BaseEntity {
    @TableField("pid")
    private UUID pid;
    @TableField("name")
    private String name;
    @TableField("department_id")
    private UUID departmentId;
    @TableField("visibility")
    private String visibility;
    @TableField("sort")
    private Integer sort;
}
