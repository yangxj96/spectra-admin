package com.devops00.spectra.core.system.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.BaseEntity;
import com.devops00.spectra.common.base.BaseServiceImpl;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.common.constant.RegionLevel;
import com.devops00.spectra.common.utils.StrUtils;
import com.devops00.spectra.core.system.javabean.converter.RegionConverter;
import com.devops00.spectra.core.system.javabean.entity.Region;
import com.devops00.spectra.core.system.javabean.from.RegionPageFrom;
import com.devops00.spectra.core.system.javabean.vo.RegionPathVO;
import com.devops00.spectra.core.system.javabean.vo.RegionVO;
import com.devops00.spectra.core.system.mapper.RegionMapper;
import com.devops00.spectra.core.system.service.RegionService;
import com.devops00.spectra.framework.assembler.NameLookup;
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

    @Override
    public RegionPathVO getPath(UUID id) {

        List<UUID> ids = new ArrayList<>();
        List<String> names = new ArrayList<>();

        Region current = this.getBaseMapper().selectById(id);

        while (current != null) {
            ids.add(current.getId());
            names.add(current.getName());

            if (current.getPid() == null) {
                break;
            }

            current = this.getBaseMapper().selectById(current.getPid());
        }

        // 因为是从子 -> 父，需要反转
        Collections.reverse(ids);
        Collections.reverse(names);

        RegionPathVO vo = new RegionPathVO();
        vo.setIds(ids);
        vo.setNames(names);
        vo.setFullName(String.join("/", names));

        return vo;
    }
}
