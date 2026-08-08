/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.oa.contact.javabean.vo;

import java.util.UUID;

import lombok.Data;

/// OA 通讯录公开信息。
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/8/9
@Data
public class ContactVO {

    private UUID id;
    private String username;
    private String realName;
    private String avatar;
    private String phone;
    private String email;
    private UUID departmentId;
    private String departmentName;
}
