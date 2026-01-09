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

import io.github.yangxj96.spectra.common.base.BaseService;
import io.github.yangxj96.spectra.core.javabean.user.entity.Authority;
import io.github.yangxj96.spectra.core.javabean.user.vo.AuthorityTreeVO;
import org.jspecify.annotations.Nullable;

import java.util.List;

/// 权限service层
///
/// @author Jack Young
/// @version 1.0
/// @since 2025-6-14
public interface AuthorityService extends BaseService<Authority> {

    /// 根据角色ID获取角色关联的权限
    ///
    /// @param id 角色ID
    /// @return 关联的权限
    List<Authority> getByRelRoleId(long id);

    /// 获取权限树
    ///
    /// @return 权限树列表
    @Nullable List<AuthorityTreeVO> tree();
}
