package com.devops00.spectra.core.service.auth.impl;


import com.devops00.spectra.common.exception.KaptchaNotMatchException;
import com.devops00.spectra.common.utils.CollUtils;
import com.devops00.spectra.common.utils.ObjUtils;
import com.devops00.spectra.core.javabean.auth.converter.AuthConverter;
import com.devops00.spectra.core.javabean.user.entity.Role;
import com.devops00.spectra.core.javabean.user.entity.User;
import com.devops00.spectra.core.javabean.user.vo.AuthorityVO;
import com.devops00.spectra.core.service.auth.AccountService;
import com.devops00.spectra.core.service.common.KaptchaService;
import com.devops00.spectra.core.service.user.RelRoleAuthorityService;
import com.devops00.spectra.core.service.user.RelUserRoleService;
import com.devops00.spectra.core.service.user.UserService;
import com.devops00.spectra.security.base.exception.LoginException;
import com.devops00.spectra.security.base.javabean.entity.SecurityUser;
import com.devops00.spectra.security.base.strategy.provider.UsernamePasswordAuthenticationProvider;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/// 用户名密码登录
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/2/17 23:37
@Service
@NullMarked
public class LoginUsernamePasswordProvider extends UsernamePasswordAuthenticationProvider {

    private final KaptchaService kaptchaService;

    private final RelRoleAuthorityService relRoleAuthorityService;

    private final RelUserRoleService relUserRoleService;

    private final UserService userService;

    private final AccountService accountService;

    private final PasswordEncoder passwordEncoder;

    private final AuthConverter authConverter;

    public LoginUsernamePasswordProvider(KaptchaService kaptchaService, RelRoleAuthorityService relRoleAuthorityService, RelUserRoleService relUserRoleService, UserService userService, AccountService accountService, PasswordEncoder passwordEncoder, AuthConverter authConverter) {
        this.kaptchaService = kaptchaService;
        this.relRoleAuthorityService = relRoleAuthorityService;
        this.relUserRoleService = relUserRoleService;
        this.userService = userService;
        this.accountService = accountService;
        this.passwordEncoder = passwordEncoder;
        this.authConverter = authConverter;
    }

    @Override
    public Authentication login(String username, String password) throws AuthenticationException {
        // 查询账户信息
        var account = accountService.getByLoginName(username);
        if (account == null || !passwordEncoder.matches(password, account.getPassword())) {
            throw new LoginException("账号或密码错误");
        }
        var user = userService.getById(account.getUserId());
        if (user == null) {
            throw new LoginException("用户不存在");
        }
        // 验证通过,封装返回
        var su = toSecurityUser(user);

        return new UsernamePasswordAuthenticationToken(su, null, su.getAuthorities());
    }

    @Override
    public void kaptchaValidate(String kaptcha) {
        if (kaptchaService.isCheck() == Boolean.TRUE) {
            var code = kaptchaService.getKaptchaCode();
            if (!kaptcha.equals(code)) {
                throw new KaptchaNotMatchException("验证码错误");
            }
        }
    }

    @Override
    public void kaptchaDelete() {
        kaptchaService.deleteBySessionId();
    }

    /// 数据库用户实体转SpringSecurity使用的用户对象
    ///
    /// @param user 数据库用户实体
    /// @return SpringSecuity的用户对象
    ///
    public SecurityUser toSecurityUser(Object user) {
        if (!(user instanceof User u)) {
            throw new LoginException("用户信息不正常");
        }

        var securityUser = authConverter.toSecurityUser(u);

        var authorities = new ArrayList<SimpleGrantedAuthority>();

        List<Object> roleObjects = this.getUserRole(securityUser.getId());
        List<Role> roles = ObjUtils.castList(roleObjects, Role.class);

        if (CollUtils.isNotEmpty(roles)) {

            authorities.addAll(
                    roles
                            .stream()
                            .map(i -> new SimpleGrantedAuthority(i.getCode()))
                            .toList()
            );

            List<String> roleIds = roles.stream()
                    .map(Role::getId)
                    .toList();

            List<Object> authorityObjects = this.getUserAuthority(roleIds);
            List<AuthorityVO> authority = ObjUtils.castList(authorityObjects, AuthorityVO.class);

            if (CollUtils.isNotEmpty(authority)) {
                authorities.addAll(
                        authority
                                .stream()
                                .map(i -> new SimpleGrantedAuthority(i.getCode()))
                                .toList()
                );
            }
        }

        securityUser.setAuthorities(authorities);
        // 开始填充角色和权限
        return securityUser;
    }

    ///
    /// 获取用户角色信息
    ///
    /// @param userId 用户 ID
    /// @return 角色列表,无角色则返回空数组
    ///
    public List<Object> getUserRole(String userId) {
        var roles = relUserRoleService.getRoles(userId);
        return roles == null
                ? Collections.emptyList()
                : new ArrayList<>(roles);
    }

    ///
    /// 获取角色包含的权限信息
    ///
    /// @param roles 角色 ID 列表
    /// @return 权限列表
    ///
    public List<Object> getUserAuthority(List<String> roles) {
        if (roles.isEmpty()) {
            return Collections.emptyList();
        }
        var authorities = relRoleAuthorityService.get(roles);
        return authorities == null
                ? Collections.emptyList()
                : new ArrayList<>(authorities);
    }
}
