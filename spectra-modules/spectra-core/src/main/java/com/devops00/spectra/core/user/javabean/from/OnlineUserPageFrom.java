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

package com.devops00.spectra.core.user.javabean.from;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 在线用户分页筛选条件。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OnlineUserPageFrom {

    /** 按登录账号包含匹配。 */
    private String username;

    /** 按用户姓名包含匹配。 */
    private String realName;

    /** 按目标部门及其下级部门筛选。 */
    private UUID departmentId;
}
