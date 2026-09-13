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

package com.devops00.spectra.oa.report.mapper;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/** 部门统计统一报表查询 SQL 契约测试。 */
class DepartmentStatsQueryMapperSqlContractTest {

    @Test
    void keepsAllDepartmentAggregatesAndVisibleDepartmentFilterInOneQuery() throws IOException {
        String mapper = readMapper();

        assertThat(mapper).contains("<select id=\"selectByDepartmentIds\"")
                .contains("FULL OUTER JOIN")
                .contains("spectra_oa.oa_asset")
                .contains("spectra_oa.oa_supply_item")
                .contains("spectra_oa.oa_reimbursement")
                .contains("spectra_oa.oa_purchase")
                .contains("asset_count")
                .contains("asset_quantity")
                .contains("asset_value")
                .contains("supply_sku_count")
                .contains("supply_stock")
                .contains("supply_min_stock")
                .contains("reimbursement_count")
                .contains("reimbursement_amount")
                .contains("purchase_count")
                .contains("purchase_budget");
        assertThat(mapper).doesNotContain("AssetMapper")
                .doesNotContain("SupplyItemMapper")
                .doesNotContain("ReimbursementMapper")
                .doesNotContain("PurchaseMapper");
        assertThat(mapper.split("department_id IN", -1)).hasSize(5);
        assertThat(mapper.split("FULL OUTER JOIN", -1)).hasSize(4);
    }

    private static String readMapper() throws IOException {
        try (var resource = DepartmentStatsQueryMapperSqlContractTest.class.getClassLoader()
                .getResourceAsStream("mapper/report/DepartmentStatsQueryMapper.xml")) {
            if (resource == null) {
                throw new IOException("找不到部门统计 Mapper XML");
            }
            return new String(resource.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
