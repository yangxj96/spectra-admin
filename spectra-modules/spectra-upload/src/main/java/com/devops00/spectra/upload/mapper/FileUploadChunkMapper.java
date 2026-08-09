/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.devops00.spectra.upload.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.devops00.spectra.upload.javabean.entity.FileUploadChunk;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文件分片信息Mapper
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/12/8 00:06
 */
@Mapper
public interface FileUploadChunkMapper extends BaseMapper<FileUploadChunk> {

}
