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

import com.devops00.spectra.core.system.lookup.DepartmentNameLookup;
import com.devops00.spectra.framework.assembler.NameFill;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 在线用户分页中的按用户分组记录。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OnlineUserPageVO {

    /** 在线用户 ID。 */
    private UUID userId;

    /** 登录账号。 */
    private String username;

    /** 用户姓名。 */
    private String realName;

    /** 用户主部门 ID。 */
    private UUID departmentId;

    /** 用户主部门名称。 */
    @NameFill(lookup = DepartmentNameLookup.class, sourceField = "departmentId")
    private String departmentName;

    /** 用户当前有效会话数。 */
    private Integer sessionCount;

    /** 用户最近一次会话登录时间。 */
    private LocalDateTime latestLoginTime;

    /** 当前用户的有效会话摘要。 */
    private List<OnlineSessionVO> sessions;
}
