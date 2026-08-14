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

package com.devops00.spectra.core.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.common.exception.EntityUpdateException;
import com.devops00.spectra.core.auth.javabean.entity.AuthenticationIdentity;
import com.devops00.spectra.core.auth.mapper.AuthenticationIdentityMapper;
import com.devops00.spectra.core.auth.service.AuthenticationIdentifierHash;
import com.devops00.spectra.core.auth.service.AuthenticationIdentityService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * 目标 authentication_identity 表的密码身份服务。
 */
@Service
@NullMarked
@RequiredArgsConstructor
public class AuthenticationIdentityServiceImpl implements AuthenticationIdentityService {

    private static final String METHOD_PASSWORD = "PASSWORD";

    private static final String PROVIDER_LOCAL = "LOCAL";

    private static final String STATE_ACTIVE = "ACTIVE";

    private static final String STATE_REVOKED = "REVOKED";

    private final AuthenticationIdentityMapper mapper;

    @Override
    public @Nullable AuthenticationIdentity findPasswordIdentity(String identifier) {
        return findIdentity(METHOD_PASSWORD, identifier);
    }

    @Override
    public @Nullable AuthenticationIdentity findIdentity(String methodCode, String identifier) {
        var wrapper = new LambdaQueryWrapper<AuthenticationIdentity>()
                .eq(AuthenticationIdentity::getMethodCode, methodCode)
                .eq(AuthenticationIdentity::getProviderCode, PROVIDER_LOCAL)
                .eq(AuthenticationIdentity::getIdentifierHash, AuthenticationIdentifierHash.digest(identifier))
                .eq(AuthenticationIdentity::getState, STATE_ACTIVE)
                .last("LIMIT 1");
        return mapper.selectOne(wrapper);
    }

    @Override
    @Transactional
    public AuthenticationIdentity createPasswordIdentity(UUID userId, String identifier) {
        return createIdentity(userId, METHOD_PASSWORD, identifier);
    }

    @Override
    @Transactional
    public AuthenticationIdentity createIdentity(UUID userId, String methodCode, String identifier) {
        var identifierHash = AuthenticationIdentifierHash.digest(identifier);
        var existing = mapper.selectOne(new LambdaQueryWrapper<AuthenticationIdentity>()
                .eq(AuthenticationIdentity::getMethodCode, methodCode)
                .eq(AuthenticationIdentity::getProviderCode, PROVIDER_LOCAL)
                .eq(AuthenticationIdentity::getIdentifierHash, identifierHash)
                .last("LIMIT 1"));
        if (existing != null) {
            if (!userId.equals(existing.getUserId())) {
                throw new DataSaveException("认证身份已被其他用户绑定");
            }
            existing.setState(STATE_ACTIVE);
            existing.setVerifiedAt(Instant.now());
            if (mapper.updateById(existing) != 1) {
                throw new EntityUpdateException("恢复认证身份失败");
            }
            return existing;
        }
        var identity = new AuthenticationIdentity();
        identity.setId(UUID.randomUUID());
        identity.setUserId(userId);
        identity.setMethodCode(methodCode);
        identity.setProviderCode(PROVIDER_LOCAL);
        identity.setIdentifierHash(AuthenticationIdentifierHash.digest(identifier));
        identity.setState(STATE_ACTIVE);
        identity.setVerifiedAt(Instant.now());
        if (mapper.insert(identity) != 1) {
            throw new DataSaveException("创建密码认证身份失败");
        }
        return identity;
    }

    @Override
    @Transactional
    public void updatePasswordIdentifier(UUID userId, String identifier) {
        var identity = findByUserId(userId);
        if (identity == null) {
            createPasswordIdentity(userId, identifier);
            return;
        }
        identity.setIdentifierHash(AuthenticationIdentifierHash.digest(identifier));
        identity.setState(STATE_ACTIVE);
        identity.setVerifiedAt(Instant.now());
        if (mapper.updateById(identity) != 1) {
            throw new EntityUpdateException("更新密码认证身份失败");
        }
    }

    @Override
    @Transactional
    public void revokeByUserId(UUID userId) {
        var identities = mapper.selectList(new LambdaQueryWrapper<AuthenticationIdentity>()
                .eq(AuthenticationIdentity::getUserId, userId)
                .eq(AuthenticationIdentity::getState, STATE_ACTIVE));
        for (AuthenticationIdentity identity : identities) {
            identity.setState(STATE_REVOKED);
            mapper.updateById(identity);
        }
    }

    @Override
    @Transactional
    public void revokeByUserIdAndMethod(UUID userId, String methodCode) {
        var identities = mapper.selectList(new LambdaQueryWrapper<AuthenticationIdentity>()
                .eq(AuthenticationIdentity::getUserId, userId)
                .eq(AuthenticationIdentity::getMethodCode, methodCode)
                .eq(AuthenticationIdentity::getState, STATE_ACTIVE));
        for (AuthenticationIdentity identity : identities) {
            identity.setState(STATE_REVOKED);
            mapper.updateById(identity);
        }
    }

    private @Nullable AuthenticationIdentity findByUserId(UUID userId) {
        List<AuthenticationIdentity> identities = mapper.selectList(new LambdaQueryWrapper<AuthenticationIdentity>()
                .eq(AuthenticationIdentity::getUserId, userId)
                .eq(AuthenticationIdentity::getMethodCode, METHOD_PASSWORD)
                .eq(AuthenticationIdentity::getProviderCode, PROVIDER_LOCAL)
                .orderByDesc(AuthenticationIdentity::getCreatedAt)
                .last("LIMIT 1"));
        return identities.isEmpty() ? null : identities.getFirst();
    }
}
