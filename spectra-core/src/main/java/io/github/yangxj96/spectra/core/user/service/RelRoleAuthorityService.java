package io.github.yangxj96.spectra.core.user.service;

import io.github.yangxj96.spectra.core.user.javabean.from.RoleAuthorityFrom;
import io.github.yangxj96.spectra.core.user.javabean.vo.AuthorityVO;

import java.util.List;

/**
 * 关联服务-用户和权限
 */
public interface RelRoleAuthorityService {

    /**
     * 授予角色权限
     */
    void grant(Long roleId, RoleAuthorityFrom from);

    /**
     * 撤销角色权限
     */
    void revoke(Long roleId);

    /**
     * 获取角色权限
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    List<AuthorityVO> get(Long roleId);

    /**
     * 获取角色权限
     *
     * @param ids 角色ID列表
     * @return 权限列表,已去重
     */
    List<AuthorityVO> get(List<Long> ids);
}
