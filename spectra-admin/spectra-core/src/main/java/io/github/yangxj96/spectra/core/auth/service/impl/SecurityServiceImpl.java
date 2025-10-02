package io.github.yangxj96.spectra.core.auth.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import io.github.yangxj96.spectra.common.enums.AuthScope;
import io.github.yangxj96.spectra.core.auth.mapper.SecurityMapper;
import io.github.yangxj96.spectra.core.auth.service.SecurityService;
import io.github.yangxj96.spectra.core.system.javabean.entity.Menu;
import io.github.yangxj96.spectra.core.user.javabean.entity.Role;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * 安全服务-实现层
 */
@Slf4j
@Service
public class SecurityServiceImpl implements SecurityService {

    @Resource
    private SecurityMapper bindMapper;

    @Override
    public List<Role> getCurrentRoles() {
        return bindMapper.getRolesByUserId(StpUtil.getLoginIdAsLong());
    }

    @Override
    public List<Menu> getCurrentMenus() {
        return bindMapper.getMenusByUserId(StpUtil.getLoginIdAsLong());
    }

    @Override
    public AuthScope getCurrentMaxScope() {
        // 具体实现是没有获取到角色则返回最小范围
        // 有待仔细考虑
        List<Role> roles = this.getCurrentRoles();
        if (roles.isEmpty()) {
            return AuthScope.DEPT_ONLY;
        }
        List<AuthScope> scopes = roles.stream().map(Role::getScope).toList();
        return scopes.stream().min(Comparator.comparingInt(AuthScope::getValue)).orElse(AuthScope.DEPT_ONLY);
    }
}
