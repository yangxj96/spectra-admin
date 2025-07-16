package com.yangxj96.spectra.service.user.share;

import com.yangxj96.spectra.service.user.javabean.mapstruct.AuthorityMapstruct;
import com.yangxj96.spectra.service.user.service.AuthorityService;
import com.yangxj96.spectra.share.javabean.ShareAuthority;
import com.yangxj96.spectra.share.service.ShareAuthorityService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 共享权限服务实现
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/7/16
 */
@Service
public class ShareAuthorityServiceImpl implements ShareAuthorityService {

    @Resource
    private AuthorityService authorityService;

    @Resource
    private AuthorityMapstruct authorityMapstruct;

    @Override
    public List<ShareAuthority> getByRoleIds(List<Long> roleIds) {
        var authorities = authorityService.getByRoleIds(roleIds);
        return authorityMapstruct.toShares(authorities);
    }

}
