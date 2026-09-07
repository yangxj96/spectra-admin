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

package com.devops00.spectra.core.system.service;

import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.core.system.javabean.converter.RegionConverter;
import com.devops00.spectra.core.system.javabean.entity.RegionPathRow;
import com.devops00.spectra.core.system.javabean.vo.RegionPathVO;
import com.devops00.spectra.core.system.mapper.RegionMapper;
import com.devops00.spectra.core.system.service.impl.RegionServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 行政区域路径查询行为测试。 */
class RegionServiceImplTest {

    private static final int MAX_DEPTH = 64;

    @Test
    void returnsRootPathWithOneMapperQuery() {
        var mapper = mock(RegionMapper.class);
        var id = UUID.randomUUID();
        when(mapper.selectPath(id, MAX_DEPTH)).thenReturn(List.of(row(id, null, "中国", 0)));

        RegionPathVO result = service(mapper).getPath(id);

        assertThat(result.getIds()).containsExactly(id);
        assertThat(result.getNames()).containsExactly("中国");
        assertThat(result.getFullName()).isEqualTo("中国");
        verify(mapper).selectPath(id, MAX_DEPTH);
    }

    @Test
    void assemblesThreeLevelPathInRootToLeafOrderWithOneMapperQuery() {
        var mapper = mock(RegionMapper.class);
        var rootId = UUID.randomUUID();
        var provinceId = UUID.randomUUID();
        var cityId = UUID.randomUUID();
        when(mapper.selectPath(cityId, MAX_DEPTH)).thenReturn(List.of(
                row(cityId, provinceId, "昆明市", 0),
                row(provinceId, rootId, "云南省", 1),
                row(rootId, null, "中国", 2)));

        RegionPathVO result = service(mapper).getPath(cityId);

        assertThat(result.getIds()).containsExactly(rootId, provinceId, cityId);
        assertThat(result.getNames()).containsExactly("中国", "云南省", "昆明市");
        assertThat(result.getFullName()).isEqualTo("中国/云南省/昆明市");
        verify(mapper).selectPath(cityId, MAX_DEPTH);
    }

    @Test
    void returnsEmptyPathWhenRegionDoesNotExist() {
        var mapper = mock(RegionMapper.class);
        var id = UUID.randomUUID();
        when(mapper.selectPath(id, MAX_DEPTH)).thenReturn(List.of());

        RegionPathVO result = service(mapper).getPath(id);

        assertThat(result.getIds()).isEmpty();
        assertThat(result.getNames()).isEmpty();
        assertThat(result.getFullName()).isEmpty();
        verify(mapper).selectPath(id, MAX_DEPTH);
    }

    @Test
    void assemblesFinitePathWhenRecursiveRowsContainCycle() {
        var mapper = mock(RegionMapper.class);
        var firstId = UUID.randomUUID();
        var secondId = UUID.randomUUID();
        when(mapper.selectPath(firstId, MAX_DEPTH)).thenReturn(List.of(
                row(firstId, secondId, "节点一", 0),
                row(secondId, firstId, "节点二", 1)));

        RegionPathVO result = service(mapper).getPath(firstId);

        assertThat(result.getIds()).containsExactly(secondId, firstId);
        assertThat(result.getNames()).containsExactly("节点二", "节点一");
        assertThat(result.getFullName()).isEqualTo("节点二/节点一");
        verify(mapper).selectPath(firstId, MAX_DEPTH);
    }

    @Test
    void rejectsPathThatExceedsMaximumDepthWithDataException() {
        var mapper = mock(RegionMapper.class);
        var id = UUID.randomUUID();
        var rows = IntStream.rangeClosed(0, MAX_DEPTH + 1)
                .mapToObj(depth -> row(UUID.randomUUID(), null, "节点" + depth, depth))
                .toList();
        when(mapper.selectPath(id, MAX_DEPTH)).thenReturn(rows);

        assertThatThrownBy(() -> service(mapper).getPath(id))
                .isInstanceOf(DataSaveException.class)
                .hasMessage("行政区划路径超过最大深度");
        verify(mapper).selectPath(id, MAX_DEPTH);
    }

    private static RegionServiceImpl service(RegionMapper mapper) {
        return new RegionServiceImpl(mapper, mock(RegionConverter.class));
    }

    private static RegionPathRow row(UUID id, UUID pid, String name, int depth) {
        var row = new RegionPathRow();
        row.setId(id);
        row.setPid(pid);
        row.setName(name);
        row.setDepth(depth);
        return row;
    }
}
