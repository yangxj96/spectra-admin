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

package com.devops00.spectra.oa.report.service;

import com.devops00.spectra.common.port.directory.DirectoryDepartmentSnapshot;
import com.devops00.spectra.common.port.directory.DirectoryQueryPort;
import com.devops00.spectra.oa.report.javabean.converter.DepartmentStatsConverter;
import com.devops00.spectra.oa.report.javabean.entity.DepartmentStatsRow;
import com.devops00.spectra.oa.report.javabean.vo.DepartmentStatsVO;
import com.devops00.spectra.oa.report.mapper.DepartmentStatsQueryMapper;
import com.devops00.spectra.oa.report.service.impl.DepartmentStatsServiceImpl;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 部门统计统一报表查询行为测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
class DepartmentStatsServiceTest {

    @Test
    void loadsAllDepartmentStatsWithOneQueryAndFiltersZeroRows() {
        var directory = mock(DirectoryQueryPort.class);
        var queryMapper = mock(DepartmentStatsQueryMapper.class);
        var converter = mock(DepartmentStatsConverter.class);
        var engineeringId = UUID.randomUUID();
        var financeId = UUID.randomUUID();
        var emptyId = UUID.randomUUID();
        var engineering = department(engineeringId, "研发部", "总部/Z-研发部");
        var finance = department(financeId, "财务部", "总部/A-财务部");
        var empty = department(emptyId, "行政部", "总部/B-行政部");

        when(directory.listDepartments()).thenReturn(List.of(engineering, finance, empty));
        when(converter.toVO(engineering)).thenReturn(vo(engineeringId));
        when(converter.toVO(finance)).thenReturn(vo(financeId));
        when(converter.toVO(empty)).thenReturn(vo(emptyId));
        when(queryMapper.selectByDepartmentIds(anyCollection())).thenReturn(List.of(
                fullRow(engineeringId),
                zeroRow(emptyId),
                fullRow(financeId)));

        var service = new DepartmentStatsServiceImpl(directory, queryMapper, converter);

        var result = service.list(null);

        assertThat(result).extracting(DepartmentStatsVO::getDepartmentName)
                .containsExactly("总部/A-财务部", "总部/Z-研发部");
        assertThat(result.getFirst())
                .extracting(DepartmentStatsVO::getDepartmentId,
                        DepartmentStatsVO::getAssetCount,
                        DepartmentStatsVO::getAssetQuantity,
                        DepartmentStatsVO::getAssetValue,
                        DepartmentStatsVO::getSupplySkuCount,
                        DepartmentStatsVO::getSupplyStock,
                        DepartmentStatsVO::getSupplyMinStock,
                        DepartmentStatsVO::getReimbursementCount,
                        DepartmentStatsVO::getReimbursementAmount,
                        DepartmentStatsVO::getPurchaseCount,
                        DepartmentStatsVO::getPurchaseBudget)
                .containsExactly(financeId, 2L, new BigDecimal("3.000"), new BigDecimal("120.50"), 4L,
                        new BigDecimal("9.000"), new BigDecimal("2.000"), 5L, new BigDecimal("88.80"), 6L,
                        new BigDecimal("300.00"));
        verify(queryMapper).selectByDepartmentIds(argThat(ids -> ids.size() == 3
                && ids.containsAll(List.of(engineeringId, financeId, emptyId))));
    }

    /**
     * 处理部门相关数据。
     */
    private static DirectoryDepartmentSnapshot department(UUID id, String name, String path) {
        return new DirectoryDepartmentSnapshot(id, null, name, path);
    }

    /**
     * 处理部门相关数据。
     */
    private static DepartmentStatsVO vo(UUID departmentId) {
        var vo = new DepartmentStatsVO();
        vo.setDepartmentId(departmentId);
        return vo;
    }

    /**
     * 处理行数据相关数据。
     */
    private static DepartmentStatsRow fullRow(UUID departmentId) {
        var row = new DepartmentStatsRow();
        row.setDepartmentId(departmentId);
        row.setAssetCount(2L);
        row.setAssetQuantity(new BigDecimal("3.000"));
        row.setAssetValue(new BigDecimal("120.50"));
        row.setSupplySkuCount(4L);
        row.setSupplyStock(new BigDecimal("9.000"));
        row.setSupplyMinStock(new BigDecimal("2.000"));
        row.setReimbursementCount(5L);
        row.setReimbursementAmount(new BigDecimal("88.80"));
        row.setPurchaseCount(6L);
        row.setPurchaseBudget(new BigDecimal("300.00"));
        return row;
    }

    /**
     * 处理行数据相关数据。
     */
    private static DepartmentStatsRow zeroRow(UUID departmentId) {
        var row = new DepartmentStatsRow();
        row.setDepartmentId(departmentId);
        row.setAssetCount(0L);
        row.setAssetQuantity(BigDecimal.ZERO);
        row.setAssetValue(BigDecimal.ZERO);
        row.setSupplySkuCount(0L);
        row.setSupplyStock(BigDecimal.ZERO);
        row.setSupplyMinStock(BigDecimal.ZERO);
        row.setReimbursementCount(0L);
        row.setReimbursementAmount(BigDecimal.ZERO);
        row.setPurchaseCount(0L);
        row.setPurchaseBudget(BigDecimal.ZERO);
        return row;
    }
}
