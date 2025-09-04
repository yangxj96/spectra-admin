package io.github.yangxj96.spectra.core.user.service.impl;

import io.github.yangxj96.spectra.common.base.BaseServiceImpl;
import io.github.yangxj96.spectra.core.user.javabean.entity.RelRoleAuthority;
import io.github.yangxj96.spectra.core.user.mapper.RelRoleAuthorityMapper;
import io.github.yangxj96.spectra.core.user.service.RelRoleAuthorityService;
import org.springframework.stereotype.Service;

/**
 * 角色关联权限中间表服务层
 */
@Service
public class RelRoleAuthorityServiceImpl
        extends BaseServiceImpl<RelRoleAuthorityMapper, RelRoleAuthority>
        implements RelRoleAuthorityService {
}
