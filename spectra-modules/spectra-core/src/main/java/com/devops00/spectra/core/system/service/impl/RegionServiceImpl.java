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
import com.devops00.spectra.common.base.BaseEntity;
import com.devops00.spectra.common.base.BaseServiceImpl;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.common.constant.RegionLevel;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.common.utils.StrUtils;
import com.devops00.spectra.core.system.javabean.converter.RegionConverter;
import com.devops00.spectra.core.system.javabean.entity.Region;
import com.devops00.spectra.core.system.javabean.from.RegionFrom;
import com.devops00.spectra.core.system.javabean.from.RegionPageFrom;
import com.devops00.spectra.core.system.javabean.vo.RegionPathVO;
import com.devops00.spectra.core.system.javabean.vo.RegionVO;
import com.devops00.spectra.core.system.mapper.RegionMapper;
import com.devops00.spectra.core.system.service.RegionService;
import com.devops00.spectra.framework.assembler.NameLookup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/// 行政区域实现Service
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/1/30 13:57
@Slf4j
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

    @Override
    public RegionVO created(RegionFrom params) {
        Region region = converter.toEntity(params);
        this.save(region);
        return converter.toVO(region);
    }

    @Override
    public RegionVO modify(RegionFrom params) {
        Region region = this.getById(params.getId());
        if (region == null) {
            throw new DataNotExistException("行政区划不存在");
        }
        converter.toEntity(params, region);
        this.updateById(region);
        return converter.toVO(region);
    }

    @Override
    public void deleteById(UUID id) {
        this.removeById(id);
    }
}
