/*
 *  Copyright 2018-2026 yangxj96
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

package com.devops00.spectra.core.user.service;

import com.devops00.spectra.core.user.javabean.from.RoleAuthorityFrom;
import com.devops00.spectra.core.user.javabean.vo.AuthorityVO;

import java.util.List;
import java.util.UUID;

/**
 * 关联服务-用户和权限
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/11/11 00:00
 */
public interface RelRoleAuthorityService {

    /**
     * 授予角色权限
     *
     * @param roleId
     *            角色ID
     * @param from
     *            权限关联信息
     */
    void grant(UUID roleId, RoleAuthorityFrom from);

    /**
     * 撤销角色权限
     *
     * @param roleId
     *            角色ID
     */
    void revoke(UUID roleId);

    /**
     * 获取角色权限
     *
     * @param roleId
     *            角色ID
     * @return 权限列表
     */
    List<AuthorityVO> get(UUID roleId);

    /**
     * 获取角色权限
     *
     * @param ids
     *            角色ID列表
     * @return 权限列表,已去重
     */
    List<AuthorityVO> get(List<UUID> ids);
}
