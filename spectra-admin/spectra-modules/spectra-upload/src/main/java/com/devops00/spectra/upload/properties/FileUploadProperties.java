/*
 *  Copyright 2018-2025 yangxj96
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
import com.devops00.spectra.upload.strategy.impl.ExtensionValidationStrategy;
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
/// @author Jack Young
/// @version 1.0
/// @since 2025/6/19
@Data
@ConfigurationProperties(prefix = "spectra.file.upload")
public class FileUploadProperties {

    /// 文件上传默认实现
    private UploadType defaultStorage = UploadType.LOCAL;

    /// 文件类型验证策略
    private List<Class<? extends FileTypeValidationStrategy>> strategies = new ArrayList<>(Arrays.asList(
            TikaValidationStrategy.class,
            MimeValidationStrategy.class,
            ExtensionValidationStrategy.class,
            MagicNumberValidationStrategy.class
    ));

    /// 分片大小,默认5M
    private Long chunkSize = 5242880L;

}
