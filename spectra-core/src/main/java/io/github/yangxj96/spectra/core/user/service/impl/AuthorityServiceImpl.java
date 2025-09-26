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

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.yangxj96.spectra.common.base.BaseEntity;
import io.github.yangxj96.spectra.common.base.BaseServiceImpl;
import io.github.yangxj96.spectra.core.user.javabean.entity.Authority;
import io.github.yangxj96.spectra.core.user.javabean.entity.RelRoleAuthority;
import io.github.yangxj96.spectra.core.user.mapper.AuthorityMapper;
import io.github.yangxj96.spectra.core.user.mapper.RelRoleAuthorityMapper;
import io.github.yangxj96.spectra.core.user.service.AuthorityService;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 权限service层-实现
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
@Service
public class AuthorityServiceImpl extends BaseServiceImpl<AuthorityMapper, Authority> implements AuthorityService {

    @Resource
    private RelRoleAuthorityMapper relRoleAuthorityMapper;

    @Override
    public List<Authority> getByRelRoleAuthority(List<RelRoleAuthority> relRoleAuthorities) {
        if (relRoleAuthorities == null || CollectionUtils.isEmpty(relRoleAuthorities)) {
            return new ArrayList<>();
        }
        List<Long> authorityIds = relRoleAuthorities.stream().map(RelRoleAuthority::getAuthorityId).toList();
        return this.list(new LambdaQueryWrapper<Authority>().in(BaseEntity::getId, authorityIds));
    }

    @Override
    public List<Authority> getByRelRoleId(long id) {
        List<RelRoleAuthority> relRoleAuthorities = relRoleAuthorityMapper.getByRoleId(id);
        if (relRoleAuthorities.isEmpty()) {
            return Collections.emptyList();
        }
        return this.listByIds(relRoleAuthorities.stream().map(RelRoleAuthority::getAuthorityId).toList());
    }
}
