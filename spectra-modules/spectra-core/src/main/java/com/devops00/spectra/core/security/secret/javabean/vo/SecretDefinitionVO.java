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

/** 脱敏密钥定义视图。 */
public record SecretDefinitionVO(String code, String name, String category, String valueType, String ownerModule,
                                 String description, boolean mutable, boolean hotReload, boolean exportable,
                                 Integer activeVersion, long pendingVersionCount, String activeFingerprint,
                                 Instant updatedAt) {
}
