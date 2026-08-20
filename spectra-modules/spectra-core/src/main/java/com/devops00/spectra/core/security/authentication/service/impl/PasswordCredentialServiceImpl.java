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

package com.devops00.spectra.core.security.authentication.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.common.exception.EntityUpdateException;
import com.devops00.spectra.core.security.authentication.javabean.entity.PasswordCredential;
import com.devops00.spectra.core.security.authentication.mapper.PasswordCredentialMapper;
import com.devops00.spectra.core.security.authentication.service.PasswordCredentialService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@NullMarked
@RequiredArgsConstructor
public class PasswordCredentialServiceImpl implements PasswordCredentialService {

    private final PasswordCredentialMapper mapper;

    @Override
    public @Nullable PasswordCredential getByUserId(UUID userId) {
        return mapper.selectOne(new LambdaQueryWrapper<PasswordCredential>().eq(PasswordCredential::getUserId, userId));
    }

    @Override
    @Transactional
    public void createOrReplace(UUID userId, String passwordHash, boolean mustChange) {
        var current = getByUserId(userId);
        if (current == null) {
            var credential = new PasswordCredential();
            credential.setUserId(userId);
            credential.setPasswordHash(passwordHash);
            credential.setChangedAt(Instant.now());
            credential.setMustChange(mustChange);
            credential.setFailedAttempts(0);
            if (mapper.insert(credential) != 1) {
                throw new DataSaveException("创建密码凭证失败");
            }
            return;
        }
        update(current, passwordHash, mustChange);
    }

    @Override
    @Transactional
    public void updatePassword(UUID userId, String passwordHash, boolean mustChange) {
        var credential = getByUserId(userId);
        if (credential == null) {
            throw new DataNotExistException("密码凭证不存在");
        }
        update(credential, passwordHash, mustChange);
    }

    private void update(PasswordCredential credential, String passwordHash, boolean mustChange) {
        credential.setPasswordHash(passwordHash);
        credential.setChangedAt(Instant.now());
        credential.setMustChange(mustChange);
        credential.setFailedAttempts(0);
        credential.setLockedUntil(null);
        if (mapper.updateById(credential) != 1) {
            throw new EntityUpdateException("更新密码凭证失败");
        }
    }
}
