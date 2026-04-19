package com.devops00.spectra.core.service.system.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.assembler.NameLookup;
import com.devops00.spectra.common.base.BaseEntity;
import com.devops00.spectra.common.base.BaseServiceImpl;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.common.constant.RegionLevel;
import com.devops00.spectra.common.utils.StrUtils;
import com.devops00.spectra.core.javabean.system.converter.RegionConverter;
import com.devops00.spectra.core.javabean.system.entity.Region;
import com.devops00.spectra.core.javabean.system.from.RegionPageFrom;
import com.devops00.spectra.core.javabean.system.vo.RegionVO;
import com.devops00.spectra.core.mapper.system.RegionMapper;
import com.devops00.spectra.core.service.system.RegionService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/// 行政区域实现Service
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/1/30 13:57
@Service
public class RegionServiceImpl extends BaseServiceImpl<RegionMapper, Region> implements RegionService, NameLookup<UUID> {

    private final RegionConverter converter;

    public RegionServiceImpl(RegionConverter converter) {
        this.converter = converter;
    }

    @Override
    public Map<UUID, String> getNameMap(Set<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return lambdaQuery()
                .in(BaseEntity::getId, ids)
                .list()
                .stream()
                .collect(Collectors.toMap(BaseEntity::getId, Region::getFullName));
    }

    @Override
    public List<RegionVO> lazyTree(Integer level, String id) {
        UUID pid = null;

        if (StrUtils.isNotBlank(id)) {
            pid = UUID.fromString(id);
        }

        List<Region> regions = lambdaQuery()
                .eq(Region::getLevel, RegionLevel.of(level))
                .eq(pid != null, Region::getPid, pid)
                .list();
        return converter.toVOList(regions);
    }

    @Override
    public IPage<RegionVO> page(PageFrom page, RegionPageFrom params) {
        // 条件构建
        var wrapper = new LambdaQueryWrapper<Region>()
                .orderByAsc(Region::getCode);
        // 查询并转换相关内容
        var db = this.page(page.toPage(), wrapper);
        return converter.toVOPage(db);
    }
}
