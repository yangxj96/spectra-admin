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

import com.devops00.spectra.common.constant.RedisCacheKey;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.common.exception.SpectraException;
import com.devops00.spectra.core.security.authentication.javabean.entity.AuthenticationIdentity;
import com.devops00.spectra.core.security.authentication.service.AuthenticationIdentityBindingService;
import com.devops00.spectra.core.security.authentication.service.AuthenticationIdentityService;
import com.devops00.spectra.security.base.constant.LoginType;
import com.devops00.spectra.security.base.properties.SecurityProperties;
import com.devops00.spectra.security.base.util.VerificationCodeDigest;
import com.devops00.spectra.security.base.util.VerificationCodeRedisStore;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/** 认证身份绑定用例实现，不读取或写入旧 sys_account。 */
@Service
@NullMarked
@RequiredArgsConstructor
public class AuthenticationIdentityBindingServiceImpl implements AuthenticationIdentityBindingService {

    private static final String STATE_ACTIVE = "ACTIVE";

    private final @Qualifier("securityRedisTemplate") RedisTemplate<String, Object> redisTemplate;

    private final SecurityProperties securityProperties;

    private final AuthenticationIdentityService identityService;

    @Override
    public List<AuthenticationIdentity> listByUserId(UUID userId) {
        return identityService.listByUserId(userId);
    }

    @Override
    @Transactional
    public void bindPhone(UUID userId, String phone, String code) {
        consumeBindingCode(RedisCacheKey.BIND_PHONE_CODE, phone, code);
        identityService.createIdentity(userId, LoginType.SMS.name(), phone);
    }

    @Override
    @Transactional
    public void bindEmail(UUID userId, String email, String code) {
        consumeBindingCode(RedisCacheKey.BIND_EMAIL_CODE, email, code);
        identityService.createIdentity(userId, LoginType.EMAIL.name(), email);
    }

    @Override
    @Transactional
    public void unbind(UUID userId, UUID identityId) {
        var identities = identityService.listByUserId(userId);
        var identity = identities.stream()
                .filter(item -> identityId.equals(item.getId()))
                .findFirst()
                .orElseThrow(() -> new DataNotExistException("认证身份不存在"));
        if (LoginType.PASSWORD.name().equals(identity.getMethodCode())) {
            throw new SpectraException("密码认证身份不允许解绑");
        }
        if (identities.stream().filter(item -> STATE_ACTIVE.equals(item.getState())).count() <= 1) {
            throw new SpectraException("至少需要保留一个认证身份");
        }
        identityService.revokeByUserIdAndId(userId, identityId);
    }

    private void consumeBindingCode(String prefix, String address, String code) {
        if (code == null || code.isBlank()) {
            throw new SpectraException("绑定验证码不能为空");
        }
        var key = prefix + address;
        var digest = VerificationCodeDigest.digest(code, securityProperties.getVerificationCodeHmacKey());
        if (!VerificationCodeRedisStore.compareAndDelete(redisTemplate, key, digest)) {
            throw new SpectraException("绑定验证码无效或已过期");
        }
    }
}
