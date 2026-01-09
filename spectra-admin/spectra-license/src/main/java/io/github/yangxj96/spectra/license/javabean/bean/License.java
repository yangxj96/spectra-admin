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

package io.github.yangxj96.spectra.license.javabean.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

/// 许可证实体
///
/// @author Jack Young
/// @version 1.0
/// @since 2025-11-11
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class License implements Serializable {

    /**
     * 自定义ID
     */
    private Long id;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 注册时间
     */
    private Instant issuedAt;

    /**
     * 到期时间
     */
    private Instant expiresAt;

    /**
     * 硬件ID
     */
    private String hwid;

    /**
     * 签名
     */
    private String signature;

}
