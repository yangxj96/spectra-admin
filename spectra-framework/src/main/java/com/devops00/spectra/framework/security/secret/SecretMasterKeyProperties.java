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

package com.devops00.spectra.framework.security.secret;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 密钥管理唯一部署根密钥配置。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/9/10
 */
@Data
@ConfigurationProperties(prefix = "spectra.security.secret")
public class SecretMasterKeyProperties {

    /** 根密钥的 Base64 编码，运行时不得记录。 */
    private String masterKey = "";

    /**
     * 创建空配置，供 Spring 绑定和测试使用。
     */
    public SecretMasterKeyProperties() {
    }

    /**
     * 创建指定根密钥配置。
     *
     * @param masterKey 根密钥 Base64 编码
     */
    public SecretMasterKeyProperties(String masterKey) {
        this.masterKey = masterKey;
    }
}
