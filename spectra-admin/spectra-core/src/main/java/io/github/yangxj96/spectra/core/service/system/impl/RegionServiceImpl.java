package io.github.yangxj96.spectra.core.service.system.impl;


import io.github.yangxj96.spectra.common.base.BaseEntity;
import io.github.yangxj96.spectra.common.base.BaseServiceImpl;
import io.github.yangxj96.spectra.common.constant.RegionLevel;
import io.github.yangxj96.spectra.common.utils.StrUtils;
import io.github.yangxj96.spectra.core.javabean.system.converter.RegionConverter;
import io.github.yangxj96.spectra.core.javabean.system.entity.Region;
import io.github.yangxj96.spectra.core.javabean.system.vo.RegionVO;
import io.github.yangxj96.spectra.core.mapper.system.RegionMapper;
import io.github.yangxj96.spectra.common.assembler.NameLookup;
import io.github.yangxj96.spectra.core.service.system.RegionService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/// 行政区域实现Service
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/1/30 13:57
@Service
public class RegionServiceImpl extends BaseServiceImpl<RegionMapper, Region> implements RegionService, NameLookup<String> {

    private final RegionConverter converter;

    public RegionServiceImpl(RegionConverter converter) {
        this.converter = converter;
    }


    @Override
    public Map<String, String> getNameMap(Set<String> ids) {
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
        List<Region> regions = lambdaQuery()
                .eq(Region::getLevel, RegionLevel.of(level))
                .eq(StrUtils.isNotBlank(id), Region::getPid, id)
                .list();
        return converter.toVOList(regions);
    }
}
