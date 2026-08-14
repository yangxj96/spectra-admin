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

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.devops00.spectra.common.base.BaseEntity;
import com.devops00.spectra.common.utils.CollUtils;
import com.devops00.spectra.common.utils.TreeUtils;
import com.devops00.spectra.core.authorization.LegacyAuthorizationWriteGuard;
import com.devops00.spectra.core.user.javabean.converter.AuthorityConverter;
import com.devops00.spectra.core.user.javabean.entity.Authority;
import com.devops00.spectra.core.user.javabean.entity.RelRoleAuthority;
import com.devops00.spectra.core.user.javabean.from.RoleAuthorityFrom;
import com.devops00.spectra.core.user.javabean.vo.AuthorityTreeVO;
import com.devops00.spectra.core.user.javabean.vo.AuthorityVO;
import com.devops00.spectra.core.user.mapper.RelRoleAuthorityMapper;
import com.devops00.spectra.core.user.service.AuthorityService;
import com.devops00.spectra.core.user.service.RelRoleAuthorityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 关联服务-用户和权限
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/11/11 00:00
 */
@Slf4j
@Service
public class RelRoleAuthorityServiceImpl implements RelRoleAuthorityService {

    private final AuthorityConverter authorityConverter;

    private final RelRoleAuthorityMapper relRoleAuthorityMapper;

    private final AuthorityService authorityService;

    public RelRoleAuthorityServiceImpl(AuthorityConverter authorityConverter, RelRoleAuthorityMapper relRoleAuthorityMapper,
                                       AuthorityService authorityService) {
        this.authorityConverter = authorityConverter;
        this.relRoleAuthorityMapper = relRoleAuthorityMapper;
        this.authorityService = authorityService;
    }

    @Override
    @Transactional
    public void grant(UUID roleId, RoleAuthorityFrom from) {
        LegacyAuthorizationWriteGuard.reject("旧角色权限关联写入口");
    }

    @Override
    @Transactional
    public void revoke(UUID roleId) {
        // 删除角色关联的权限
        var wrapper = new LambdaQueryWrapper<RelRoleAuthority>().eq(RelRoleAuthority::getRoleId, roleId);
        relRoleAuthorityMapper.delete(wrapper);
    }

    @Override
    public List<AuthorityVO> get(UUID roleId) {
        var authority = authorityService.getByRelRoleId(roleId);
        return authorityConverter.toVOList(authority);
    }

    @Override
    public List<AuthorityVO> get(List<UUID> ids) {
        List<RelRoleAuthority> relRoleAuthorities = relRoleAuthorityMapper
                .selectList(new LambdaQueryWrapper<RelRoleAuthority>().in(RelRoleAuthority::getRoleId, ids));
        if (CollUtils.isEmpty(relRoleAuthorities)) {
            return new ArrayList<>();
        }
        var authorityIds = relRoleAuthorities.stream().map(RelRoleAuthority::getAuthorityId).toList();
        var coll = authorityService.list(new LambdaQueryWrapper<Authority>().in(BaseEntity::getId, authorityIds));
        return authorityConverter.toVOList(coll);
    }
}
