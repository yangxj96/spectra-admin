/*
 *  Copyright 2025 yangxj96.com
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
 *
 */

package com.yangxj96.spectra.common.fileupload;

import com.yangxj96.spectra.common.fileupload.strategy.FileTypeValidationStrategy;
import com.yangxj96.spectra.common.fileupload.strategy.impl.ExtensionValidationStrategy;
import com.yangxj96.spectra.common.fileupload.strategy.impl.MagicNumberValidationStrategy;
import com.yangxj96.spectra.common.fileupload.strategy.impl.MimeValidationStrategy;
import com.yangxj96.spectra.common.fileupload.strategy.impl.TikaValidationStrategy;
import com.yangxj96.spectra.common.properties.FileUploadProperties;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 文件上传需要的配置
 * </p>
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/6/19
 */
@Configuration
public class FileUploadConfiguration {

    @Resource
    private FileUploadProperties properties;

    /**
     * 文件类型验证策略管理器
     *
     * @return {@link FileTypeValidator}
     */
    @Bean
    public FileTypeValidator fileTypeValidator() {
        List<FileTypeValidationStrategy> strategies = new ArrayList<>();

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


    /**
     * 获取可上传的文件的mimes列表
     *
     * @param allowedTypes 允许上传的列表
     * @return mime列表
     */
    private List<String> mimes(List<FileType> allowedTypes) {
        List<String> m = new ArrayList<>();
        for (FileType allowedType : allowedTypes) {
            m.add(allowedType.getMime());
        }
        return m;
    }

}
