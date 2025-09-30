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

package io.github.yangxj96.spectra.core.user.service.impl;

import io.github.yangxj96.spectra.core.user.service.PermissionService;
import org.springframework.stereotype.Service;

/**
 * 权限service层-实现
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
@Service
public class PermissionServiceImpl implements PermissionService {


    //@Override
    //public void saveRoleRelevanceAuthorityByRoleId(long id, RoleAuthorityFrom from) {
    //    // 权限树过滤
    //    List<AuthorityTreeVO> authorityTree = authorityService.tree();
    //    // 压缩选中权限：全选子节点 → 只保留父节点
    //    from.setAuthorityIds(
    //            TreeUtils.compressSelectedNodes(
    //                    authorityTree,
    //                    new HashSet<>(from.getAuthorityIds()),
    //                    AuthorityTreeVO::getId
    //            ).stream().toList()
    //    );
    //    // 进行保存
    //    roleService.saveAuthorityById(id, from);
    //}
    //
    //@Override
    //public void saveRoleRelevanceMenuByRoleId(long id, RoleMenuFrom from) {
    //    roleService.saveMenuById(id, from);
    //}


}
