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

package com.devops00.spectra.core.security.secret.javabean.vo;

import java.time.Instant;
import java.util.UUID;

/**
 * 封装密钥版本相关的响应数据。
 *
 * @param id              数据记录的唯一标识
 * @param versionNo       密钥版本号
 * @param state           当前对象所处的业务状态
 * @param cipherAlgorithm 密钥版本使用的加密算法
 * @param fingerprint     密钥材料的指纹值
 * @param source          当前数据或配置的来源
 * @param effectiveAt     密钥版本的生效时间
 * @param retiredAt       密钥版本的停用时间
 * @param createdAt       创建时间
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
public record SecretVersionVO(UUID id, Integer versionNo, String state, String cipherAlgorithm,
                              String fingerprint, String source, Instant effectiveAt, Instant retiredAt,
                              Instant createdAt) {
}
