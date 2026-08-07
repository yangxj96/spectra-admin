/*
 * Copyright 2018-2026 yangxj96
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.devops00.spectra.oa.document.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.devops00.spectra.oa.document.javabean.entity.DocumentVersion;
import org.apache.ibatis.annotations.Mapper;

/// 文档版本 Mapper。
/// @author yangxj96
/// @version 1.0
/// @since 2026/8/7
@Mapper
public interface DocumentVersionMapper extends BaseMapper<DocumentVersion> {
}
