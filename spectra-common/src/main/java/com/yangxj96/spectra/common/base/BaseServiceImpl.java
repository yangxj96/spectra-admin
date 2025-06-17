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

package com.yangxj96.spectra.common.base;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yangxj96.spectra.common.exception.DataNotExistException;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * RESTFul 接口公用service层
 *
 * @param <M> 子类对应的mapper
 * @param <O> 子类对应的实体
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
public class BaseServiceImpl<M extends BaseMapper<O>, O extends BaseEntity>
        extends ServiceImpl<M, O> implements BaseService<O> {

    @Override
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional(rollbackFor = {Exception.class})
    public void create(O datum) {
        if (!this.save(datum)) {
            throw new RuntimeException("插入数据失败");
        }
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public Boolean delete(Long id) {
        var datum = getById(id);
        if (datum == null) {
            throw new DataNotExistException("数据不存在");
        }
        return this.removeById(datum.getId());
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public void modify(O datum) {
        if (null == getById(datum.getId())) {
            throw new DataNotExistException("数据不存在");
        }
        if (!this.updateById(datum)) {
            throw new RuntimeException("更新数据失败");
        }
    }
}
