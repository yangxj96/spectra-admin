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

import java.util.UUID;

/// 文档目录展示对象。
/// @author yangxj96
/// @version 1.0
/// @since 2026/8/7
@Data
public class DocumentFolderVO {

    /// 主键 ID。
    private UUID id;

    /// 父级 ID。
    private UUID pid;

    /// 名称。
    private String name;

    /// 部门 ID。
    private UUID departmentId;

    /// 可见范围。
    private String visibility;

    /// 排序号。
    private Integer sort;
}
