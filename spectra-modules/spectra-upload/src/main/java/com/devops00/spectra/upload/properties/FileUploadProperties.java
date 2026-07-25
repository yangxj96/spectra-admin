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

package com.devops00.spectra.upload.properties;

import com.devops00.spectra.upload.javabean.constant.UploadType;
import com.devops00.spectra.upload.strategy.FileTypeValidationStrategy;
import com.devops00.spectra.upload.strategy.impl.MagicNumberValidationStrategy;
import com.devops00.spectra.upload.strategy.impl.MimeValidationStrategy;
import com.devops00.spectra.upload.strategy.impl.TikaValidationStrategy;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/// 文件上传参数
///
/// @author yangxj96
/// @version 1.0
/// @since 2025/6/19 00:00
@Data
@ConfigurationProperties(prefix = "spectra.file.upload")
public class FileUploadProperties {

    /// 文件上传默认实现
    private UploadType defaultStorage = UploadType.LOCAL;

    /// 文件类型验证策略（扩展名校验已内置于 FileTypeValidator，无需配置）
    private List<Class<? extends FileTypeValidationStrategy>> strategies = new ArrayList<>(Arrays.asList(
            TikaValidationStrategy.class,
            MimeValidationStrategy.class,
            MagicNumberValidationStrategy.class
    ));

    /// 分片大小,默认5M 5242880L
    private Long chunkSize = 5242880L;

    /// 是否启用白名单模式（仅允许 allowedUpload=true 的类型）
    private boolean whitelistEnabled = true;

    /// 是否启用黑名单模式（拒绝 dangerous=true 的类型）
    private boolean blacklistEnabled = true;

}
