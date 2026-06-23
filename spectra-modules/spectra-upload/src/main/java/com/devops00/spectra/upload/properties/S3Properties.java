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

/// S3协议配置
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/3/31 01:46
@Data
@ConfigurationProperties(prefix = "spectra.file.upload.s3")
public class S3Properties {

    private String endpoint;

    private String accessKey;

    private String secretKey;

    private String region;

    private String bucket;

    private Integer previewMinutes = 10;

}
