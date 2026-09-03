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

package com.devops00.spectra.core.security.change.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.core.user.javabean.entity.User;
import com.devops00.spectra.core.user.mapper.UserMapper;
import com.devops00.spectra.core.security.change.AuthorizationEpochGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 基于 sys_user.security_version 的原子安全版本门禁。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/14
 */
@Service
@RequiredArgsConstructor
public class JdbcAuthorizationEpochGuard implements AuthorizationEpochGuard {

    private final UserMapper userMapper;

    @Override
    public void assertCurrent(UUID userId, long expectedVersion) {
        var user = userMapper.selectById(userId);
        if (user == null) {
            throw new DataNotExistException("目标用户不存在");
        }
        long actual = user.getSecurityVersion() == null ? 0L : user.getSecurityVersion();
        if (actual != expectedVersion) {
            throw new IllegalStateException("目标用户安全版本已变化，请重新生成授权变更预览");
        }
    }

    @Override
    public void advance(UUID userId, long expectedVersion) {
        var wrapper = new LambdaUpdateWrapper<User>()
                .eq(User::getId, userId)
                .eq(User::getSecurityVersion, expectedVersion)
                .setSql("security_version = security_version + 1");
        if (userMapper.update(null, wrapper) != 1) {
            throw new IllegalStateException("目标用户安全版本并发变化，授权变更已拒绝");
        }
    }
}
