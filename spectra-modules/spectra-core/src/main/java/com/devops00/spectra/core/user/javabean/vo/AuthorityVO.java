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

package com.devops00.spectra.core.user.javabean.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/// 权限VO
///
/// @author yangxj96
/// @version 1.0
/// @since 2025/11/11 00:00
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthorityVO {

    /// 数据id.
    private UUID id;


    private UUID pid;

    /// 权限名称
    private String name;

    /// 编码
    private String code;
}
