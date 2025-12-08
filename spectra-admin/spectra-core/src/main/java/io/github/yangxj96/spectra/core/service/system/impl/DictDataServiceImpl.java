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

package io.github.yangxj96.spectra.core.service.system.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.yangxj96.spectra.common.base.BaseServiceImpl;
import io.github.yangxj96.spectra.core.javabean.system.entity.DictData;
import io.github.yangxj96.spectra.core.mapper.system.DictDataMapper;
import io.github.yangxj96.spectra.core.service.system.DictDataService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 字典(字典数据)业务层-实现
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-18
 */
@Service
public class DictDataServiceImpl extends BaseServiceImpl<DictDataMapper, DictData> implements DictDataService {

    @Override
    public List<DictData> listByGid(Long gid) {
        var wrapper = new LambdaQueryWrapper<DictData>()
                .eq(DictData::getGid, gid);
        return this.list(wrapper);
    }
}
