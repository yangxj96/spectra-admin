package com.yangxj96.spectra.share.service;

import com.yangxj96.spectra.share.javabean.ShareRole;

import java.util.List;

/**
 * 共享角色服务
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/7/16
 */
public interface ShareRoleService {

    /**
     * 根据用户ID获取用户的角色信息
     *
     * @param uid 用户ID
     * @return 角色列表
     */
    List<ShareRole> getByUserId(Long uid);
}
