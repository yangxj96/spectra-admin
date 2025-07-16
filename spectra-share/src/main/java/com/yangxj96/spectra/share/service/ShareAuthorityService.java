package com.yangxj96.spectra.share.service;

import com.yangxj96.spectra.share.javabean.ShareAuthority;

import java.util.List;

/**
 * 共享权限服务
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/7/16
 */
public interface ShareAuthorityService {

    /**
     * 根据角色ID获取权限列表
     *
     * @param roleIds 角色ID列表
     * @return 权限列表
     */
    List<ShareAuthority> getByRoleIds(List<Long> roleIds);
}
