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

import com.yangxj96.spectra.common.base.BaseServiceImpl;
import com.yangxj96.spectra.service.user.javabean.entity.Authority;
import com.yangxj96.spectra.service.user.mapper.AuthorityMapper;
import com.yangxj96.spectra.service.user.service.AuthorityService;
import org.springframework.stereotype.Service;

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


    @Override
    public List<Authority> getByRoleIds(List<Long> roleIds) {
        return this.baseMapper.getByRoleIds(roleIds);
    }
}
