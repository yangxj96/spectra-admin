package io.github.yangxj96.spectra.core.user.service.impl;

import io.github.yangxj96.spectra.common.base.BaseServiceImpl;
import io.github.yangxj96.spectra.core.user.javabean.entity.RelRoleMenu;
import io.github.yangxj96.spectra.core.user.mapper.RelRoleMenuMapper;
import io.github.yangxj96.spectra.core.user.service.RelRoleMenuService;
import org.springframework.stereotype.Service;

/**
 * 角色关联菜单中间表服务层
 */
@Service
public class RelRoleMenuServiceImpl
        extends BaseServiceImpl<RelRoleMenuMapper, RelRoleMenu>
        implements RelRoleMenuService {
}
