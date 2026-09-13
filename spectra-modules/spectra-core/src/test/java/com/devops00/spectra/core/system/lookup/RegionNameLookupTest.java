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

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.devops00.spectra.core.system.javabean.entity.Region;
import com.devops00.spectra.core.system.mapper.RegionMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 区域名称 Lookup 的批量查询和空输入测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/08
 */
class RegionNameLookupTest {

    @BeforeAll
    static void registerMybatisLambdaMetadata() {
        var assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "region-name-lookup-test");
        TableInfoHelper.initTableInfo(assistant, Region.class);
    }

    @Test
    void lookupMustReturnRegionFullNamesFromOneMapperQuery() {
        UUID regionId = UUID.randomUUID();
        var region = new Region();
        region.setId(regionId);
        region.setFullName("云南省/保山市");
        var mapper = mock(RegionMapper.class);
        when(mapper.selectList(any())).thenReturn(List.of(region));

        var result = new RegionNameLookup(mapper).getNameMap(Set.of(regionId));

        assertThat(result).containsEntry(regionId, "云南省/保山市");
        verify(mapper).selectList(any());
    }

    @Test
    void lookupMustReturnEmptyMapWithoutQueryForEmptyIds() {
        var mapper = mock(RegionMapper.class);

        var result = new RegionNameLookup(mapper).getNameMap(Set.of());

        assertThat(result).isEmpty();
        verifyNoInteractions(mapper);
    }
}
