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

package com.yangxj96.spectra.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * <p>
 * 文件相关配置
 * </p>
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/6/19
 */
@Data
@Component
@ConfigurationProperties(prefix = "spectra.file")
public class FileProperties {

    /**
     * 基础文件位置,所有文件都会在这个目录下面进行存放
     */
    private String baseDir = "/spectra-files";

    /**
     * 上传的文件夹位置
     */
    private String uploadDir = "/uploads";

    /**
     * 日志文件夹位置
     */
    private String logsDir = "/logs";

}
