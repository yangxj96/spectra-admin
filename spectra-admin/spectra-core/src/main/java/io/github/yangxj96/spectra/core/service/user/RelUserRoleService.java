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

import io.github.yangxj96.spectra.core.javabean.user.entity.RelUserRole;
import io.github.yangxj96.spectra.core.javabean.user.entity.Role;

import java.util.List;

/**
 * 关联服务-用户和角色
 */
public interface RelUserRoleService {

    /**
     * 授予用户角色
     */
    void grant(Long userId, List<Long> roleIds);

    /**
     * 撤销用户角色(全部)
     *
     * @param userId 用户ID
     */
    void revoke(Long userId);

    /**
     * 撤销用户角色(指定的角色)
     *
     * @param userId  用户ID
     * @param roleIds 需要撤销的角色列表
     */
    void revoke(Long userId, List<Long> roleIds);

    /**
     * 根据角色ID获取关联关系
     *
     * @param roleId 角色ID
     * @return 这个角色有的关联关系
     */
    List<RelUserRole> getRelByRoleId(Long roleId);

    /**
     * 获取用户角色
     *
     * @param userId 角色ID
     * @return 用户角色列表
     */
    List<Role> getRoles(Long userId);

}
