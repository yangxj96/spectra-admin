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

package com.devops00.spectra.core.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.devops00.spectra.common.base.BaseServiceImpl;
import com.devops00.spectra.core.system.javabean.entity.DictGroup;
import com.devops00.spectra.core.system.mapper.DictGroupMapper;
import com.devops00.spectra.core.system.service.DictGroupService;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

/// 字典(字典类型)业务层实现
///
/// @author Jack Young
/// @version 1.0
/// @since 2025-6-18
@Service
public class DictGroupServiceImpl extends BaseServiceImpl<DictGroupMapper, DictGroup> implements DictGroupService {

    @Override
    public @Nullable DictGroup getByCode(String code) {
        return this.getOne(
                new LambdaQueryWrapper<DictGroup>()
                        .eq(DictGroup::getCode, code)
                        .last("LIMIT 1")
        );
    }
}
