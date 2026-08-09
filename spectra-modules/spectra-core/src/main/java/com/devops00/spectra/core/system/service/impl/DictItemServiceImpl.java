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

package com.devops00.spectra.core.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.devops00.spectra.common.base.BaseServiceImpl;
import com.devops00.spectra.core.system.javabean.entity.DictItem;
import com.devops00.spectra.core.system.mapper.DictItemMapper;
import com.devops00.spectra.core.system.service.DictItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/// 字典(字典数据)业务层-实现
///
/// @author yangxj96
/// @version 1.0
/// @since 2025/6/18 00:00
@Slf4j
@Service
public class DictItemServiceImpl extends BaseServiceImpl<DictItemMapper, DictItem> implements DictItemService {

    @Override
    public List<DictItem> listByGid(UUID gid) {
        var wrapper = new LambdaQueryWrapper<DictItem>().eq(DictItem::getGid, gid);
        return this.list(wrapper);
    }
}
