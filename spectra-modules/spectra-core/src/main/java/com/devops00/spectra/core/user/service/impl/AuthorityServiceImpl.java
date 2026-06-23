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

package com.devops00.spectra.core.user.service.impl;

import com.devops00.spectra.common.base.BaseServiceImpl;
import com.devops00.spectra.common.constant.Common;
import com.devops00.spectra.common.utils.TreeBuilder;
import com.devops00.spectra.core.user.javabean.converter.AuthorityConverter;
import com.devops00.spectra.core.user.javabean.entity.Authority;
import com.devops00.spectra.core.user.javabean.entity.RelRoleAuthority;
import com.devops00.spectra.core.user.javabean.vo.AuthorityTreeVO;
import com.devops00.spectra.core.user.mapper.AuthorityMapper;
import com.devops00.spectra.core.user.mapper.RelRoleAuthorityMapper;
import com.devops00.spectra.core.user.service.AuthorityService;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/// 权限service层-实现
///
/// @author yangxj96
/// @version 1.0
/// @since 2025/6/14 00:00
@Service
public class AuthorityServiceImpl extends BaseServiceImpl<AuthorityMapper, Authority> implements AuthorityService {

    private final AuthorityConverter authorityConverter;

    private final RelRoleAuthorityMapper relRoleAuthorityMapper;

    public AuthorityServiceImpl(AuthorityConverter authorityConverter, RelRoleAuthorityMapper relRoleAuthorityMapper) {
        this.authorityConverter = authorityConverter;
        this.relRoleAuthorityMapper = relRoleAuthorityMapper;
    }


    @Override
    public List<Authority> getByRelRoleId(UUID id) {
        List<RelRoleAuthority> relRoleAuthorities = relRoleAuthorityMapper.getByRoleId(id);
        if (relRoleAuthorities.isEmpty()) {
            return Collections.emptyList();
        }
        return this.listByIds(relRoleAuthorities.stream().map(RelRoleAuthority::getAuthorityId).toList());
    }

    @Override
    public @Nullable List<AuthorityTreeVO> tree() {
        List<Authority> authorities = this.list();
        List<AuthorityTreeVO> vos = authorityConverter.toTreeVOList(authorities);
        return new TreeBuilder<>(vos).buildTree(Common.PID);
    }
}
