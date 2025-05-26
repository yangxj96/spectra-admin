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

    protected M bindMapper;

    protected BaseServiceImpl(M bindMapper) {
        this.bindMapper = bindMapper;
    }

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