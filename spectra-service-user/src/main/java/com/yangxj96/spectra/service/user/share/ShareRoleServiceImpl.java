package com.yangxj96.spectra.service.user.share;

import com.yangxj96.spectra.service.user.javabean.mapstruct.RoleMapstruct;
import com.yangxj96.spectra.service.user.service.RoleService;
import com.yangxj96.spectra.share.javabean.ShareRole;
import com.yangxj96.spectra.share.service.ShareRoleService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 共享角色服务是西安
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/7/16
 */
@Service
public class ShareRoleServiceImpl implements ShareRoleService {

    @Resource
    private RoleService roleService;

    @Resource
    private RoleMapstruct roleMapstruct;

    @Override
    public List<ShareRole> getByUserId(Long uid) {
        var roles = roleService.getByUserId(uid);
        return roleMapstruct.toShares(roles);
    }

}
