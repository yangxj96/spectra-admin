package com.devops00.spectra.core.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.BaseServiceImpl;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
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
/// @author Jack Young
/// @version 1.0
/// @since 2025-11-06
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

}
