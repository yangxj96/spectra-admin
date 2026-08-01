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
import com.devops00.spectra.common.base.BaseServiceImpl;
import com.devops00.spectra.common.constant.Common;
import com.devops00.spectra.common.exception.BuiltinDataException;
import com.devops00.spectra.common.exception.DataException;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.common.utils.TreeBuilder;
import com.devops00.spectra.core.user.javabean.converter.AuthorityConverter;
import com.devops00.spectra.core.user.javabean.entity.Authority;
import com.devops00.spectra.core.user.javabean.entity.RelRoleAuthority;
import com.devops00.spectra.core.user.javabean.from.AuthoritySaveFrom;
import com.devops00.spectra.core.user.javabean.vo.AuthorityTreeVO;
import com.devops00.spectra.core.user.mapper.AuthorityMapper;
import com.devops00.spectra.core.user.mapper.RelRoleAuthorityMapper;
import com.devops00.spectra.core.user.service.AuthorityService;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/// 权限service层-实现
///
/// @author yangxj96
/// @version 1.0
/// @since 2025/6/14 00:00
@Slf4j
@Service
public class AuthorityServiceImpl extends BaseServiceImpl<AuthorityMapper, Authority> implements AuthorityService {

    private final AuthorityConverter authorityConverter;

    private final RelRoleAuthorityMapper relRoleAuthorityMapper;

    public AuthorityServiceImpl(AuthorityConverter authorityConverter, RelRoleAuthorityMapper relRoleAuthorityMapper) {
        this.authorityConverter = authorityConverter;
        this.relRoleAuthorityMapper = relRoleAuthorityMapper;
    }

    @Override
    @Transactional
    public void created(AuthoritySaveFrom from) {
        validateParent(from.getPid());
        ensureCodeAvailable(from.getCode(), null);
        if (!this.save(authorityConverter.toEntity(from))) {
            throw new DataException("创建权限失败");
        }
    }

    @Override
    @Transactional
    public void modify(AuthoritySaveFrom from) {
        var current = this.getById(from.getId());
        if (current == null) {
            throw new DataNotExistException("权限不存在");
        }
        if ("*".equals(current.getCode())) {
            throw new BuiltinDataException("顶级权限不可修改");
        }
        validateParent(from.getPid());
        ensureCodeAvailable(from.getCode(), from.getId());
        authorityConverter.updateEntity(from, current);
        if (!this.updateById(current)) {
            throw new DataException("修改权限失败");
        }
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        var current = this.getById(id);
        if (current == null) {
            throw new DataNotExistException("权限不存在");
        }
        if ("*".equals(current.getCode())) {
            throw new BuiltinDataException("顶级权限不可删除");
        }
        if (this.count(new LambdaQueryWrapper<Authority>().eq(Authority::getPid, id)) > 0) {
            throw new DataException("存在子权限,不可删除");
        }
        if (relRoleAuthorityMapper.selectCount(new LambdaQueryWrapper<RelRoleAuthority>()
                .eq(RelRoleAuthority::getAuthorityId, id)) > 0) {
            throw new DataException("权限已分配给角色,不可删除");
        }
        if (!this.removeById(id)) {
            throw new DataException("删除权限失败");
        }
    }

    private void validateParent(UUID pid) {
        if (pid != null && this.getById(pid) == null) {
            throw new DataNotExistException("父级权限不存在");
        }
    }

    private void ensureCodeAvailable(String code, UUID currentId) {
        var wrapper = new LambdaQueryWrapper<Authority>().eq(Authority::getCode, code);
        if (currentId != null) {
            wrapper.ne(Authority::getId, currentId);
        }
        if (this.count(wrapper) > 0) {
            throw new DataException("权限编码已存在");
        }
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
