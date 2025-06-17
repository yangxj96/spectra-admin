/*
 *  Copyright 2025 yangxj96.com
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
 *
 */

package com.yangxj96.spectra.core.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import com.yangxj96.spectra.common.utils.CollUtils;
import com.yangxj96.spectra.core.javabean.entity.Account;
import com.yangxj96.spectra.core.javabean.from.UsernamePasswordFrom;
import com.yangxj96.spectra.core.javabean.vo.TokenVO;
import com.yangxj96.spectra.core.service.AccountService;
import com.yangxj96.spectra.core.service.AuthService;
import jakarta.annotation.Resource;
import jakarta.security.auth.message.AuthException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.security.auth.login.LoginException;

/**
 * 认证service层-实现
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Resource
    private AccountService accountService;

    @Resource
    private BCryptPasswordEncoder encoder;

    @Resource
    private HttpServletRequest request;

    @Override
    public TokenVO login(UsernamePasswordFrom params) throws LoginException {
        // 验证码查询
        // 账户查询
        Account datum = accountService.getByUsername(params.getUsername());
        // 账号不存在或者密码匹配失败
        if (null == datum || !encoder.matches(params.getPassword(), datum.getPassword())) {
            throw new LoginException("账号或密码错误");
        }
        if (!CollUtils.contains(request.getHeaderNames(), "user-agent")) {
            throw new AuthException("认证错误");
        }
        StpUtil.login(
                datum.getId(),
                new SaLoginParameter()
                        .setDeviceType("PC")
                        .setIsLastingCookie(false)
                        .setIsWriteHeader(false)
                        .setTerminalExtra("user_id", datum.getUserId())
        );

        return TokenVO.builder()
                .id(datum.getId())
                .username(datum.getUsername())
                .accessToken(StpUtil.getTokenValue())
                .authorities(StpUtil.getPermissionList())
                .roles(StpUtil.getRoleList())
                .build();
    }

    @Override
    public void logout() {
        StpUtil.logout();
    }

}
