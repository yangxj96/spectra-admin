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

package com.devops00.spectra.core.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.devops00.spectra.common.base.BaseEntity;
import com.devops00.spectra.common.utils.CollUtils;
import com.devops00.spectra.common.utils.TreeUtils;
import com.devops00.spectra.core.javabean.user.converter.AuthorityConverter;
import com.devops00.spectra.core.javabean.user.entity.Authority;
import com.devops00.spectra.core.javabean.user.entity.RelRoleAuthority;
import com.devops00.spectra.core.javabean.user.from.RoleAuthorityFrom;
import com.devops00.spectra.core.javabean.user.vo.AuthorityTreeVO;
import com.devops00.spectra.core.javabean.user.vo.AuthorityVO;
import com.devops00.spectra.core.mapper.user.RelRoleAuthorityMapper;
import com.devops00.spectra.core.service.user.AuthorityService;
import com.devops00.spectra.core.service.user.RelRoleAuthorityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/// 关联服务-用户和权限
///
/// @author Jack Young
/// @version 1.0
/// @since 2025-11-11
@Service
public class RelRoleAuthorityServiceImpl implements RelRoleAuthorityService {

    private final AuthorityConverter authorityConverter;

    private final RelRoleAuthorityMapper relRoleAuthorityMapper;

    private final AuthorityService authorityService;

    public RelRoleAuthorityServiceImpl(AuthorityConverter authorityConverter, RelRoleAuthorityMapper relRoleAuthorityMapper, AuthorityService authorityService) {
        this.authorityConverter = authorityConverter;
        this.relRoleAuthorityMapper = relRoleAuthorityMapper;
        this.authorityService = authorityService;
    }


    @Override
    @Transactional
    public void grant(UUID roleId, RoleAuthorityFrom from) {
        // 压缩权限树
        from.setAuthorityIds(
                TreeUtils.compressSelectedNodes(
                        authorityService.tree(),
                        new HashSet<>(from.getAuthorityIds()),
                        AuthorityTreeVO::getId
                ).stream().toList()
        );
        // 开始进入修改权限的具体执行方法
        var currentIds = relRoleAuthorityMapper.getByRoleId(roleId)
                .stream().map(RelRoleAuthority::getAuthorityId).collect(Collectors.toSet());

        var targetIds = new HashSet<>(from.getAuthorityIds());
        // 计算删除且删除
        var removeIds = new HashSet<>(currentIds);
        removeIds.removeAll(targetIds); // current - target = 删除
        if (CollUtils.isNotEmpty(removeIds)) {
            var wrapper = new LambdaQueryWrapper<RelRoleAuthority>()
                    .eq(RelRoleAuthority::getRoleId, roleId)
                    .in(RelRoleAuthority::getAuthorityId, removeIds);
            relRoleAuthorityMapper.delete(wrapper);
        }
        // 计算新增且插入
        var addIds = new HashSet<>(targetIds);
        addIds.removeAll(currentIds); // target - current = 新增
        if (CollUtils.isNotEmpty(addIds)) {
            List<RelRoleAuthority> newRelations = addIds.stream()
                    .map(addId -> RelRoleAuthority.builder()
                            .roleId(roleId)
                            .authorityId(addId)
                            .build())
                    .collect(Collectors.toList());
            relRoleAuthorityMapper.insert(newRelations);
        }
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
        List<RelRoleAuthority> relRoleAuthorities = relRoleAuthorityMapper.selectList(
                new LambdaQueryWrapper<RelRoleAuthority>()
                        .in(RelRoleAuthority::getRoleId, ids)
        );
        if (CollUtils.isEmpty(relRoleAuthorities)) {
            return new ArrayList<>();
        }
        var authorityIds = relRoleAuthorities.stream().map(RelRoleAuthority::getAuthorityId).toList();
        var coll = authorityService.list(new LambdaQueryWrapper<Authority>().in(BaseEntity::getId, authorityIds));
        return authorityConverter.toVOList(coll);
    }


}
