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
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.BaseServiceImpl;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.common.constant.ConfiguredValueType;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.common.utils.StrUtils;
import com.devops00.spectra.core.system.javabean.converter.ConfiguredConverter;
import com.devops00.spectra.core.system.javabean.entity.Configured;
import com.devops00.spectra.core.system.javabean.from.ConfiguredFrom;
import com.devops00.spectra.core.system.javabean.from.ConfiguredPageFrom;
import com.devops00.spectra.core.system.javabean.vo.ConfiguredVO;
import com.devops00.spectra.core.system.mapper.ConfiguredMapper;
import com.devops00.spectra.core.system.service.ConfiguredService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/// 系统配置Service层默认实现
///
/// @author yangxj96
/// @version 1.0
/// @since 2025/11/6 00:00
@Service
public class ConfiguredServiceImpl extends BaseServiceImpl<ConfiguredMapper, Configured> implements ConfiguredService {

    private final ConfiguredConverter configuredConverter;

    public ConfiguredServiceImpl(ConfiguredConverter configuredConverter) {
        this.configuredConverter = configuredConverter;
    }


    @Override
    @Transactional
    public void modify(ConfiguredFrom params) {
        var db = this.getById(params.getId());
        if (db == null) {
            throw new DataNotExistException("系统配置不存在");
        }
        db.setValue(params.getValue());
        db.setRemarks(params.getRemarks());
        this.updateById(db);
    }

    @Override
    public IPage<ConfiguredVO> page(PageFrom page, ConfiguredPageFrom params) {
        // 条件构建
        var wrapper = new LambdaQueryWrapper<Configured>()
                .like(StrUtils.isNotBlank(params.getKey()), Configured::getKey, params.getKey());
        // 查询并转换相关内容
        var db = this.page(page.toPage(), wrapper);
        return configuredConverter.toVOPage(db);
    }

    @Override
    @Transactional
    public void upsert(String key, String value, String remarks) {
        var existing = this.getOne(
                new LambdaQueryWrapper<Configured>().eq(Configured::getKey, key));
        if (existing != null) {
            existing.setValue(value);
            existing.setRemarks(remarks);
            this.updateById(existing);
        } else {
            var entity = new Configured();
            entity.setKey(key);
            entity.setValue(value);
            entity.setType(ConfiguredValueType.TEXT);
            entity.setRemarks(remarks);
            this.save(entity);
        }
    }

}
