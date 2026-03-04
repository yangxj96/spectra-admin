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

package com.devops00.spectra.core.configure.fileupload.properties;

import com.devops00.spectra.core.configure.fileupload.enums.FileType;
import com.devops00.spectra.core.configure.fileupload.strategy.FileTypeValidationStrategy;
import com.devops00.spectra.core.configure.fileupload.strategy.impl.ExtensionValidationStrategy;
import com.devops00.spectra.core.configure.fileupload.strategy.impl.MagicNumberValidationStrategy;
import com.devops00.spectra.core.configure.fileupload.strategy.impl.MimeValidationStrategy;
import com.devops00.spectra.core.configure.fileupload.strategy.impl.TikaValidationStrategy;
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

    /// 上传的文件夹位置
    private String uploadDir = "uploads";

    /// 上传文件的时候临时文件路径
    private String uploadTempDir = "temp";

    /// 允许的类型
    private List<FileType> allowedTypes = new ArrayList<>(Arrays.asList(
            FileType.JPEG,
            FileType.PNG,
            FileType.GIF,
            FileType.PDF,
            FileType.ZIP
    ));

    /// 文件类型验证策略
    private List<Class<? extends FileTypeValidationStrategy>> strategies = new ArrayList<>(Arrays.asList(
            TikaValidationStrategy.class,
            MimeValidationStrategy.class,
            ExtensionValidationStrategy.class,
            MagicNumberValidationStrategy.class
    ));

    /// 分片大小,默认5M
    private Long chunkSize = 5242880L;

    /// 清理间隔天数
    private Integer cleanupAfterDays = 1;
}
