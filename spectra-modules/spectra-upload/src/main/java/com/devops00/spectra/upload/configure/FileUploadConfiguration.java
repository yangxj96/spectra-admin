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

package com.devops00.spectra.upload.configure;

import com.devops00.spectra.common.constant.LogPrefix;
import com.devops00.spectra.upload.properties.FileUploadProperties;
import com.devops00.spectra.upload.service.FileTypeService;
import com.devops00.spectra.upload.strategy.FileTypeValidationStrategy;
import com.devops00.spectra.upload.strategy.FileTypeValidator;
import com.devops00.spectra.upload.strategy.impl.MagicNumberValidationStrategy;
import com.devops00.spectra.upload.strategy.impl.MimeValidationStrategy;
import com.devops00.spectra.upload.strategy.impl.TikaValidationStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;

/**
 * 文件上传类型验证需要的相关配置
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/06/27 00:00
 */
@Slf4j
@NullMarked
@Configuration
@EnableConfigurationProperties(FileUploadProperties.class)
@RequiredArgsConstructor
public class FileUploadConfiguration {

    private final FileUploadProperties properties;

    private final FileTypeService fileTypeService;

    /**
     * 文件类型验证策略管理器
     *
     * @return 文件策略验证管理器
     */
    @Bean
    public FileTypeValidator fileTypeValidator() {
        log.debug(LogPrefix.STORAGE.f("载入文件类型验证策略管理器"));
        boolean wl = properties.isWhitelistEnabled();
        boolean bl = properties.isBlacklistEnabled();
        var strategies = new ArrayList<FileTypeValidationStrategy>();
        for (var strategy : properties.getStrategies()) {
            if (strategy.isAssignableFrom(MimeValidationStrategy.class)) {
                strategies.add(new MimeValidationStrategy(fileTypeService, wl, bl));
            }
            if (strategy.isAssignableFrom(MagicNumberValidationStrategy.class)) {
                strategies.add(new MagicNumberValidationStrategy(fileTypeService));
            }
            if (strategy.isAssignableFrom(TikaValidationStrategy.class)) {
                strategies.add(new TikaValidationStrategy(fileTypeService, wl, bl));
            }
        }
        return new FileTypeValidator(strategies, fileTypeService, wl, bl);
    }
}
