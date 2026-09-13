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

package com.devops00.spectra.oa.contact.javabean.vo;

import lombok.Data;

import java.util.UUID;

/**
 * OA 通讯录公开信息。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/9
 */
@Data
public class ContactVO {

    /**
     * 主键 ID。
     */
    private UUID id;

    /**
     * 工号/员工编号。
     */
    private String employeeNo;

    /**
     * 姓名。
     */
    private String realName;

    /** 登录用户名。 */
    private String username;

    /**
     * 头像。
     */
    private String avatar;

    /**
     * 手机号。
     */
    private String phone;

    /**
     * 邮箱。
     */
    private String email;

    /**
     * 部门 ID。
     */
    private UUID departmentId;

    /**
     * 部门名称字段。
     */
    private String departmentName;
}
