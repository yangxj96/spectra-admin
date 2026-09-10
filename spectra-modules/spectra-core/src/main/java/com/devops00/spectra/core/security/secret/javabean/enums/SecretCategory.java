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

package com.devops00.spectra.core.security.secret.javabean.enums;

/** 密钥管理页面展示的固定分类。 */
public enum SecretCategory {

    /** 应用内部加密密钥。 */
    APPLICATION_CRYPTO,
    /** 外部业务服务凭据。 */
    BUSINESS_CREDENTIAL,
    /** 安全签名密钥。 */
    SECURITY_SIGNING
}
