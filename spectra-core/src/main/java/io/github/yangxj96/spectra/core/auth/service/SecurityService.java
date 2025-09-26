package io.github.yangxj96.spectra.core.auth.service;

import io.github.yangxj96.spectra.core.system.javabean.entity.Menu;
import io.github.yangxj96.spectra.core.user.javabean.entity.Role;

import java.util.List;

/**
 * 安全服务
 */
public interface SecurityService {

    List<Role> getCurrentRoles();

    List<Menu> getCurrentMenus();
}
