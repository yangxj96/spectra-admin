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

package com.devops00.spectra.core.system.lookup;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.devops00.spectra.core.system.javabean.entity.Region;
import com.devops00.spectra.core.system.mapper.RegionMapper;
import com.devops00.spectra.framework.assembler.NameLookup;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 行政区域 ID 到区域全称的批量名称查询适配器。
 * <p>
 * 该适配器只负责展示名称查询，直接读取区域 Mapper，不依赖完整区域 Service，
 * 从而避免名称装配引入 Service 自调用。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/08
 */
@Component
public class RegionNameLookup implements NameLookup<UUID> {

    private final RegionMapper regionMapper;

    /**
     * 创建区域名称查询适配器。
     *
     * @param regionMapper 读取区域 ID 与区域全称的 Mapper
     */
    public RegionNameLookup(RegionMapper regionMapper) {
        this.regionMapper = regionMapper;
    }

    /**
     * 批量读取区域全称。
     *
     * @param ids 需要查询区域全称的区域 ID 集合；null 或空集合表示无需查询
     * @return 区域 ID 到区域全称的映射；没有输入或没有匹配记录时返回空 Map，不返回 null
     */
    @Override
    public Map<UUID, String> getNameMap(Set<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return regionMapper.selectList(Wrappers.<Region>lambdaQuery()
                .select(Region::getId, Region::getFullName)
                .in(Region::getId, ids))
                .stream()
                .collect(Collectors.toMap(Region::getId, Region::getFullName));
    }
}
