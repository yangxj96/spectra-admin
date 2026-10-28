/*
 * Copyright 2018-2026 yangxj96
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package com.devops00.spectra.core.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.devops00.spectra.common.exception.DataException;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.core.user.javabean.constant.UserStatus;
import com.devops00.spectra.core.user.javabean.entity.User;
import com.devops00.spectra.core.user.mapper.UserMapper;
import com.devops00.spectra.core.user.service.UserSecurityStateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 用户安全状态读取实现。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/29
 */
@Service
@RequiredArgsConstructor
public class UserSecurityStateServiceImpl implements UserSecurityStateService {

    private final UserMapper userMapper;

    @Override
    public UserSecuritySnapshot load(UUID userId) {
        var user = userMapper.selectById(userId);
        if (user == null) {
            throw new DataNotExistException("目标用户不存在");
        }
        return snapshot(user);
    }

    @Override
    public UserSecuritySnapshot lockActive(UUID userId) {
        var user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getId, userId)
                .last("FOR UPDATE"));
        if (user == null) {
            throw new DataNotExistException("目标用户不存在");
        }
        if (user.getStatus() == null || !UserStatus.ACTIVE.equals(user.getStatus())) {
            throw new DataException("只有 ACTIVE 用户可以执行该安全操作");
        }
        return snapshot(user);
    }

    private static UserSecuritySnapshot snapshot(User user) {
        return new UserSecuritySnapshot(user.getId(), user.getStatus() == null ? null : user.getStatus().name(),
                user.getSecurityVersion() == null ? 0L : user.getSecurityVersion());
    }
}
