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

package com.devops00.spectra.core.security.authorization.service.impl;

import com.devops00.spectra.common.security.authorization.AuthorizationMutationFence;
import com.devops00.spectra.core.user.javabean.constant.UserStatus;
import com.devops00.spectra.core.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.UUID;

/**
 * 使用与角色、Assignment 变更相同的用户行锁保护业务写事务的授权快照。
 */
@Service
@RequiredArgsConstructor
public class JdbcAuthorizationMutationFence implements AuthorizationMutationFence {

    private final UserMapper userMapper;

    @Override
    public void lockUser(UUID userId) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("授权写入锁必须处于业务事务中");
        }
        if (userId == null) {
            throw new AccessDeniedException("当前用户不存在或已停用");
        }
        var users = userMapper.selectActiveByIdsForUpdate(List.of(userId));
        if (users.size() != 1 || !UserStatus.ACTIVE.equals(users.getFirst().getStatus())) {
            throw new AccessDeniedException("当前用户不存在或已停用");
        }
    }
}
