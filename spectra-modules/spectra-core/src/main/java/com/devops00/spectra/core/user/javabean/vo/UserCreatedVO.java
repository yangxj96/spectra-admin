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

import java.io.Serializable;
import java.util.UUID;

/**
 * 用户创建成功响应
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreatedVO implements Serializable {

    /**
     * 新创建的用户 ID
     */
    private UUID id;

    /**
     * 新创建的用户姓名。
     */
    private String realName;
}
