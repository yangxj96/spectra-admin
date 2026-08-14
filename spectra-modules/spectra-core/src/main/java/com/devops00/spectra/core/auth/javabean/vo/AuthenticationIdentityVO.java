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

package com.devops00.spectra.core.auth.javabean.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/** 目标认证身份响应；不返回原始标识，只返回摘要对应的元数据。 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationIdentityVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private UUID id;

    private String methodCode;

    private String providerCode;

    private String state;

    private Instant verifiedAt;

    private Boolean current;
}
