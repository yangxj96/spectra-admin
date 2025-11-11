/*
 *  Copyright 2018-2025 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.github.yangxj96.spectra.core.service.user;

import io.github.yangxj96.spectra.core.javabean.user.from.RoleAuthorityFrom;
import io.github.yangxj96.spectra.core.javabean.user.vo.AuthorityVO;

import java.util.List;

/**
 * 关联服务-用户和权限
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-11-11
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
