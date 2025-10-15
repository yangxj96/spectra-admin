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

package io.github.yangxj96.spectra.license.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 许可相关配置
 */
@Data
@ConfigurationProperties(prefix = "spectra.system.license")
public class LicenseProperties {

    /**
     * 许可位置,当项目作为客户端的时候,验证许可的许可文件位置
     */
    private String licensePath;

    /**
     * 公钥位置
     */
    private String publicKey;

    /**
     * 私钥位置
     */
    private String privateKey;

    /**
     * 密钥密码
     */
    private String password;

}
