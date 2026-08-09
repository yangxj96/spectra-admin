/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.oa.contact.javabean.vo;

import java.util.UUID;

import lombok.Data;

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
     * 用户名。
     */
    private String username;

    /**
     * 真实姓名。
     */
    private String realName;

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
