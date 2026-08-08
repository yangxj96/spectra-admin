/*
 * Copyright 2018-2026 yangxj96
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.devops00.spectra.oa.document.javabean.from;

import lombok.Data;

import java.util.UUID;

/// 文档分页查询条件。
/// @author yangxj96
/// @version 1.0
/// @since 2026/8/7
@Data
public class DocumentPageFrom {

    /// 搜索关键字。
    private String keyword;

    /// 状态。
    private String status;

    /// 目录 ID。
    private UUID folderId;
}
