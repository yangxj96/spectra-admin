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

import com.devops00.spectra.common.base.BaseService;
import com.devops00.spectra.core.user.javabean.entity.Authority;
import com.devops00.spectra.core.user.javabean.from.AuthoritySaveFrom;
import com.devops00.spectra.core.user.javabean.vo.AuthorityTreeVO;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * 权限service层
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/11/11 00:00
 */
public interface AuthorityService extends BaseService<Authority> {

    /**
     * 创建权限
     */
    void created(AuthoritySaveFrom from);

    /**
     * 修改权限
     */
    void modify(AuthoritySaveFrom from);

    /**
     * 删除权限
     */
    void deleteById(UUID id);

    /**
     * 根据角色ID获取角色关联的权限
     *
     * @param id
     *            角色ID
     * @return 关联的权限
     */
    List<Authority> getByRelRoleId(UUID id);

    /**
     * 获取权限树
     *
     * @return 权限树列表
     */
    @Nullable
    List<AuthorityTreeVO> tree();
}
