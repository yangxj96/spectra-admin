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

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 本地上传的配置
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/3/31 13:57
 */
@Data
@ConfigurationProperties(prefix = "spectra.file.upload.local")
public class LocalProperties {

    /**
     * 上传的文件夹位置
     */
    private String storageRoot = "uploads";

    /**
     * 上传文件的时候临时文件路径
     */
    private String stagingRoot = "temp";
}
