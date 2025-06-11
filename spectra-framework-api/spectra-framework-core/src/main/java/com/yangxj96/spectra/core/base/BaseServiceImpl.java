/*
 * Copyright (c) 2018-2025 Jack Young (杨新杰)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.yangxj96.spectra.core.base;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yangxj96.spectra.core.exception.DataNotExistException;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * RESTFul 接口公用service层
 *
 * @param <M> 子类对应的mapper
 * @param <O> 子类对应的实体
 *            </O></M>
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
