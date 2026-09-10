/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.devops00.spectra.core.security.secret.javabean.vo;

import java.time.Instant;
import java.util.UUID;

/** 脱敏密钥版本视图，不包含密文和明文。 */
public record SecretVersionVO(UUID id, Integer versionNo, String state, String cipherAlgorithm,
                              String fingerprint, String source, Instant effectiveAt, Instant retiredAt,
                              Instant createdAt) {
}
