/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.core.security.authentication.service;

import com.devops00.spectra.core.security.authentication.javabean.entity.UserContact;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/** 用户认证与通知联系方式服务。 */
public interface UserContactService {

    String PHONE = "PHONE";

    String EMAIL = "EMAIL";

    String ACTIVE = "ACTIVE";

    /** 创建或更新用户已验证的联系方式。 */
    UserContact upsertVerified(UUID userId, String contactType, String value);

    /** 查询用户当前有效联系方式。 */
    List<UserContact> listActiveByUserId(UUID userId);

    /** 批量查询用户当前有效联系方式。 */
    Map<UUID, List<UserContact>> listActiveByUserIds(List<UUID> userIds);

    /** 撤销用户指定类型的当前有效联系方式。 */
    void revokeByUserIdAndType(UUID userId, String contactType);
}
