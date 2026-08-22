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

import com.devops00.spectra.core.security.authorization.domain.UserAuthorizationStatus;
import com.devops00.spectra.core.system.service.impl.DepartmentServiceImpl;
import com.devops00.spectra.core.user.javabean.constant.UserStatus;
import com.devops00.spectra.framework.assembler.NameFill;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 用户分页的VO
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/6/14 00:00
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UserPageVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID。
     */
    private UUID id;

    /**
     * 工号/员工编号。
     */
    private String employeeNo;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 用户状态
     */
    private UserStatus status;

    /**
     * 最近一次生命周期变更原因。
     */
    private String statusReason;

    /**
     * 离职时间。
     */
    private Instant departedAt;

    /**
     * 当前授权状态。
     */
    private UserAuthorizationStatus authorizationStatus;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 语言
     */
    private String language;

    /**
     * 时区
     */
    private String timezone;

    /**
     * 角色列表
     */
    private List<RoleVO> roles;

    /**
     * 组织机构ID
     */
    private UUID departmentId;

    /**
     * 组织机构名称
     */
    @NameFill(lookup = DepartmentServiceImpl.class, sourceField = "departmentId")
    private String departmentName;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
