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

package com.devops00.spectra.core.system.javabean.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 加解密配置响应
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/7/14
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CryptoConfigVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 是否启用加解密
     */
    private Boolean enabled;

    /**
     * 服务端公钥（Base64）
     */
    private String serverPublicKey;
}
