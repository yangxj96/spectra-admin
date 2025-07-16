/*
 *  Copyright 2025 yangxj96.com
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
 *
 */

package com.yangxj96.spectra.service.user.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.yangxj96.spectra.common.base.BaseServiceImpl;
import com.yangxj96.spectra.service.user.javabean.entity.Role;
import com.yangxj96.spectra.service.user.mapper.RoleMapper;
import com.yangxj96.spectra.service.user.service.RoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 角色service层-实现
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
@Service
public class RoleServiceImpl extends BaseServiceImpl<RoleMapper, Role> implements RoleService {

    @Override
    public List<Role> getByUserId(Long uid) {
        return this.baseMapper.getByUserId(uid);
    }

    @Override
    @Transactional
    public int removeRelevanceRoles(Long uid) {
        return this.baseMapper.removeRelevanceRoles(uid);
    }

    @Override
    @Transactional
    public int insertRelevanceRoles(Long uid, List<Long> roleIds) {
        int num = 0;
        for (Long roleId : roleIds) {
            num += this.baseMapper.insertRelevanceRole(IdWorker.getId(), uid, roleId);
        }
        return num;
    }
}
