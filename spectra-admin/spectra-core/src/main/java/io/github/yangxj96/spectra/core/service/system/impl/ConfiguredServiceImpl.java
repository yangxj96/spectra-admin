package io.github.yangxj96.spectra.core.service.system.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.yangxj96.spectra.common.base.BaseServiceImpl;
import io.github.yangxj96.spectra.common.base.javabean.from.PageFrom;
import io.github.yangxj96.spectra.common.exception.DataNotExistException;
import io.github.yangxj96.spectra.common.utils.StrUtils;
import io.github.yangxj96.spectra.core.javabean.system.converter.ConfiguredConverter;
import io.github.yangxj96.spectra.core.javabean.system.entity.Configured;
import io.github.yangxj96.spectra.core.javabean.system.from.ConfiguredFrom;
import io.github.yangxj96.spectra.core.javabean.system.from.ConfiguredPageFrom;
import io.github.yangxj96.spectra.core.javabean.system.vo.ConfiguredVO;
import io.github.yangxj96.spectra.core.mapper.system.ConfiguredMapper;
import io.github.yangxj96.spectra.core.service.system.ConfiguredService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * 系统配置Service层默认实现
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-11-06
 */
@Service
public class ConfiguredServiceImpl extends BaseServiceImpl<ConfiguredMapper, Configured> implements ConfiguredService {

    @Resource
    private ConfiguredConverter configuredConverter;

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
        var result = new Page<ConfiguredVO>();
        // 条件构建
        var wrapper = new LambdaQueryWrapper<Configured>()
                .like(StrUtils.isNotBlank(params.getKey()), Configured::getKey, params.getKey());
        // 查询并转换相关内容
        var db = this.page(page.toPage(), wrapper);
        BeanUtils.copyProperties(db, result);
        result.setRecords(configuredConverter.toVOs(db.getRecords()));
        return result;
    }
}
