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

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 部门直属成员选择所需的最小用户信息。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/28
 */
@Data
@NoArgsConstructor
public class DepartmentMemberCandidateVO {

    /** 用户 ID。 */
    private UUID id;

    /** 登录用户名。 */
    private String username;

    /** 用户姓名。 */
    private String realName;

    /** 账号生命周期状态。 */
    private String status;

    /** 用户是否以该部门作为主部门。 */
    private boolean primaryMember;

    /** 用户是否通过关联关系属于该部门。 */
    private boolean associatedMember;
}
