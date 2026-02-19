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

package io.github.yangxj96.spectra.core.configure.fileupload;

import io.github.yangxj96.spectra.common.constant.LogPrefix;
import io.github.yangxj96.spectra.core.configure.fileupload.enums.FileType;
import io.github.yangxj96.spectra.core.configure.fileupload.properties.FileUploadProperties;
import io.github.yangxj96.spectra.core.configure.fileupload.strategy.FileTypeValidationStrategy;
import io.github.yangxj96.spectra.core.configure.fileupload.strategy.FileTypeValidator;
import io.github.yangxj96.spectra.core.configure.fileupload.strategy.impl.ExtensionValidationStrategy;
import io.github.yangxj96.spectra.core.configure.fileupload.strategy.impl.MagicNumberValidationStrategy;
import io.github.yangxj96.spectra.core.configure.fileupload.strategy.impl.MimeValidationStrategy;
import io.github.yangxj96.spectra.core.configure.fileupload.strategy.impl.TikaValidationStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/// 文件上传类型验证需要的相关配置
///
/// @author Jack Young
/// @version 1.0
/// @since 2025-06-27
@Slf4j
@Configuration
@EnableConfigurationProperties(FileUploadProperties.class)
public class FileUploadConfiguration {

    private final FileUploadProperties properties;

    public FileUploadConfiguration(FileUploadProperties properties) {
        this.properties = properties;
    }

    /// 文件类型验证策略管理器
    ///
    /// @return 文件策略验证管理器
    @Bean
    public FileTypeValidator fileTypeValidator() {
        log.debug(LogPrefix.STORAGE.f("载入文件类型验证策略管理器"));
        var strategies = new ArrayList<FileTypeValidationStrategy>();
        List<FileType> allowedTypes = properties.getAllowedTypes();
        // 根据配置添加策略处理器
        for (var strategy : properties.getStrategies()) {
            if (strategy.isAssignableFrom(MimeValidationStrategy.class)) {
                strategies.add(new MimeValidationStrategy(mimes(allowedTypes)));
            }
            if (strategy.isAssignableFrom(ExtensionValidationStrategy.class)) {
                strategies.add(new ExtensionValidationStrategy(allowedTypes));
            }
            if (strategy.isAssignableFrom(MagicNumberValidationStrategy.class)) {
                strategies.add(new MagicNumberValidationStrategy(allowedTypes));
            }
            if (strategy.isAssignableFrom(TikaValidationStrategy.class)) {
                strategies.add(new TikaValidationStrategy(mimes(allowedTypes)));
            }
        }
        return new FileTypeValidator(strategies);
    }


    /// 获取可上传的文件的mimes列表
    ///
    /// @param allowedTypes 允许上传的列表
    /// @return mime列表
    private List<String> mimes(List<FileType> allowedTypes) {
        var m = new ArrayList<String>();
        for (FileType allowedType : allowedTypes) {
            m.add(allowedType.getMime());
        }
        return m;
    }

}
