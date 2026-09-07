/*
 * Copyright 2018-2026 yangxj96
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.devops00.spectra.oa.report.service.impl;

import com.devops00.spectra.common.port.directory.DirectoryDepartmentSnapshot;
import com.devops00.spectra.common.port.directory.DirectoryQueryPort;
import com.devops00.spectra.oa.asset.mapper.AssetMapper;
import com.devops00.spectra.oa.purchase.mapper.PurchaseMapper;
import com.devops00.spectra.oa.reimbursement.mapper.ReimbursementMapper;
import com.devops00.spectra.oa.report.javabean.converter.DepartmentStatsConverter;
import com.devops00.spectra.oa.report.javabean.vo.DepartmentStatsVO;
import com.devops00.spectra.oa.supply.mapper.SupplyItemMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 部门统计通过 Directory Port 获取部门快照的行为测试。 */
class DepartmentStatsServiceImplTest {

    @Test
    void usesDepartmentSnapshotsWithoutCoreDepartmentService() {
        var directory = mock(DirectoryQueryPort.class);
        var converter = mock(DepartmentStatsConverter.class);
        var assetMapper = mock(AssetMapper.class);
        var supplyItemMapper = mock(SupplyItemMapper.class);
        var reimbursementMapper = mock(ReimbursementMapper.class);
        var purchaseMapper = mock(PurchaseMapper.class);
        var departmentId = UUID.randomUUID();
        var department = new DirectoryDepartmentSnapshot(departmentId, null, "研发部", "总部/研发部");
        var vo = new DepartmentStatsVO();
        vo.setDepartmentId(departmentId);
        when(directory.listDepartments()).thenReturn(List.of(department));
        when(converter.toVO(department)).thenReturn(vo);
        when(assetMapper.selectMaps(any())).thenReturn(List.of());
        when(supplyItemMapper.selectMaps(any())).thenReturn(List.of());
        when(reimbursementMapper.selectMaps(any())).thenReturn(List.of());
        when(purchaseMapper.selectMaps(any())).thenReturn(List.of());

        var service = new DepartmentStatsServiceImpl(directory, assetMapper, supplyItemMapper, reimbursementMapper, purchaseMapper, converter);

        assertThat(service.list(null)).isEmpty();
        verify(directory).listDepartments();
    }
}
