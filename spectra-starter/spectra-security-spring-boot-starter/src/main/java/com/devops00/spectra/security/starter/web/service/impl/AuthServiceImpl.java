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

package com.devops00.spectra.security.starter.web.service.impl;

import com.devops00.spectra.common.constant.RedisCacheKey;
import com.devops00.spectra.security.starter.web.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 认证服务实现
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/6/28
 */
@Slf4j
@Service
@NullMarked
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void sendSmsCode(String phone) {
        // TODO: 调用短信服务发送验证码
        redisTemplate.opsForValue().set(RedisCacheKey.SMS_CODE + phone, "1234", Duration.ofMinutes(5));
    }

    @Override
    public void sendEmailCode(String email) {
        // TODO: 调用邮件服务发送验证码
        redisTemplate.opsForValue().set(RedisCacheKey.EMAIL_CODE + email, "1234", Duration.ofMinutes(5));
    }
}
