package com.yangxj96.spectra.security.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import com.yangxj96.spectra.core.utils.CollUtils;
import com.yangxj96.spectra.security.entity.dto.Account;
import com.yangxj96.spectra.security.entity.from.UsernamePasswordFrom;
import com.yangxj96.spectra.security.entity.vo.TokenVO;
import com.yangxj96.spectra.security.service.AccountService;
import com.yangxj96.spectra.security.service.AuthService;
import jakarta.annotation.Resource;
import jakarta.security.auth.message.AuthException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.security.auth.login.LoginException;

/**
 * 认证服务
 *
 * @author 杨新杰
 * @since 2025/5/26 19:01
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
        UserAgent agent = UserAgentUtil.parse(request.getHeader("user-agent"));
        StpUtil.login(
                datum.getId(),
                new SaLoginParameter()
                        .setDeviceType(agent.isMobile() ? "Android" : "PC")
                        .setIsLastingCookie(false)
                        .setIsWriteHeader(false)
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
