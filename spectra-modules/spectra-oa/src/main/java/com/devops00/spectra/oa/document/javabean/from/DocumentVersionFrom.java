/*
 * Copyright 2018-2026 yangxj96
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.devops00.spectra.oa.document.javabean.from;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

/// 文档版本保存入参。
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/8/7
@Data
public class DocumentVersionFrom {

    /// 文件 ID。
    @NotNull(message = "文件不能为空")
    private UUID fileId;

    /// 文件名称。
    private String fileName;

    /// 文件大小。
    private Long fileSize;

    /// 内容类型。
    private String contentType;

    /// 版本说明。
    private String versionNote;
}
