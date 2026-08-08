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

package com.devops00.spectra.oa.report.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.core.system.javabean.entity.Department;
import com.devops00.spectra.core.system.service.DepartmentService;
import com.devops00.spectra.oa.asset.javabean.entity.Asset;
import com.devops00.spectra.oa.asset.mapper.AssetMapper;
import com.devops00.spectra.oa.purchase.javabean.entity.Purchase;
import com.devops00.spectra.oa.purchase.mapper.PurchaseMapper;
import com.devops00.spectra.oa.reimbursement.javabean.entity.Reimbursement;
import com.devops00.spectra.oa.reimbursement.mapper.ReimbursementMapper;
import com.devops00.spectra.oa.report.javabean.converter.DepartmentStatsConverter;
import com.devops00.spectra.oa.report.javabean.from.DepartmentStatsFrom;
import com.devops00.spectra.oa.report.javabean.vo.DepartmentStatsVO;
import com.devops00.spectra.oa.report.service.DepartmentStatsService;
import com.devops00.spectra.oa.supply.javabean.entity.SupplyItem;
import com.devops00.spectra.oa.supply.mapper.SupplyItemMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/// 部门维度统计服务实现。
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/8/7
@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentStatsServiceImpl implements DepartmentStatsService {

    private static final String[] HEADERS = {
            "部门", "资产条目数", "资产数量", "资产金额", "办公用品 SKU 数", "当前库存", "最低库存",
            "报销单数", "报销金额", "采购申请数", "采购预算"
    };

    private final DepartmentService departmentService;
    private final AssetMapper assetMapper;
    private final SupplyItemMapper supplyItemMapper;
    private final ReimbursementMapper reimbursementMapper;
    private final PurchaseMapper purchaseMapper;
    private final DepartmentStatsConverter departmentStatsConverter;

    @Override
    public List<DepartmentStatsVO> list(DepartmentStatsFrom from) {
        UUID departmentId = from == null ? null : from.getDepartmentId();
        Map<UUID, DepartmentStatsVO> result = departmentService.list().stream()
                .filter(department -> departmentId == null || departmentId.equals(department.getId()))
                .collect(Collectors.toMap(Department::getId, department -> {
                    var vo = departmentStatsConverter.toVO(department);
                    vo.setDepartmentName(department.getPath() == null
                            ? department.getName() : department.getPath());
                    return vo;
                }, (left, right) -> left, HashMap::new));

        if (result.isEmpty() && departmentId != null) {
            return Collections.emptyList();
        }

        mergeAssetStats(result, departmentId);
        mergeSupplyStats(result, departmentId);
        mergeReimbursementStats(result, departmentId);
        mergePurchaseStats(result, departmentId);

        return result.values().stream()
                // 只返回当前数据权限范围内确有业务数据的部门，避免聚合接口泄露无权部门名称。
                .filter(this::hasBusinessData)
                .sorted(Comparator.comparing(DepartmentStatsVO::getDepartmentName,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();
    }

    @Override
    public byte[] export(DepartmentStatsFrom from) {
        try (var workbook = new XSSFWorkbook(); var output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("部门统计");
            CellStyle headerStyle = createHeaderStyle(workbook);
            Row header = sheet.createRow(0);
            for (int index = 0; index < HEADERS.length; index++) {
                Cell cell = header.createCell(index);
                cell.setCellValue(HEADERS[index]);
                cell.setCellStyle(headerStyle);
            }

            List<DepartmentStatsVO> rows = list(from);
            for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
                DepartmentStatsVO stats = rows.get(rowIndex);
                Row row = sheet.createRow(rowIndex + 1);
                setText(row, 0, stats.getDepartmentName());
                setNumber(row, 1, stats.getAssetCount());
                setNumber(row, 2, stats.getAssetQuantity());
                setNumber(row, 3, stats.getAssetValue());
                setNumber(row, 4, stats.getSupplySkuCount());
                setNumber(row, 5, stats.getSupplyStock());
                setNumber(row, 6, stats.getSupplyMinStock());
                setNumber(row, 7, stats.getReimbursementCount());
                setNumber(row, 8, stats.getReimbursementAmount());
                setNumber(row, 9, stats.getPurchaseCount());
                setNumber(row, 10, stats.getPurchaseBudget());
            }
            for (int index = 0; index < HEADERS.length; index++) {
                sheet.autoSizeColumn(index);
            }
            workbook.write(output);
            return output.toByteArray();
        } catch (IOException exception) {
            log.error("导出部门统计失败", exception);
            throw new DataSaveException("导出部门统计失败");
        }
    }

    private boolean hasBusinessData(DepartmentStatsVO stats) {
        return stats.getAssetCount() > 0 || stats.getSupplySkuCount() > 0 || stats.getReimbursementCount() > 0
                || stats.getPurchaseCount() > 0;
    }

    private void mergeAssetStats(Map<UUID, DepartmentStatsVO> result, UUID departmentId) {
        QueryWrapper<Asset> wrapper = aggregateWrapper(departmentId);
        wrapper.select("department_id", "COUNT(*) AS asset_count", "COALESCE(SUM(quantity), 0) AS asset_quantity",
                "COALESCE(SUM(acquisition_amount), 0) AS asset_value").groupBy("department_id");
        assetMapper.selectMaps(wrapper).forEach(row -> merge(result, row, stats -> {
            stats.setAssetCount(number(row, "asset_count").longValue());
            stats.setAssetQuantity(decimal(row, "asset_quantity"));
            stats.setAssetValue(decimal(row, "asset_value"));
        }));
    }

    private void mergeSupplyStats(Map<UUID, DepartmentStatsVO> result, UUID departmentId) {
        QueryWrapper<SupplyItem> wrapper = aggregateWrapper(departmentId);
        wrapper.select("department_id", "COUNT(*) AS supply_sku_count", "COALESCE(SUM(current_stock), 0) AS supply_stock",
                "COALESCE(SUM(min_stock), 0) AS supply_min_stock").groupBy("department_id");
        supplyItemMapper.selectMaps(wrapper).forEach(row -> merge(result, row, stats -> {
            stats.setSupplySkuCount(number(row, "supply_sku_count").longValue());
            stats.setSupplyStock(decimal(row, "supply_stock"));
            stats.setSupplyMinStock(decimal(row, "supply_min_stock"));
        }));
    }

    private void mergeReimbursementStats(Map<UUID, DepartmentStatsVO> result, UUID departmentId) {
        QueryWrapper<Reimbursement> wrapper = aggregateWrapper(departmentId);
        wrapper.select("department_id", "COUNT(*) AS reimbursement_count",
                "COALESCE(SUM(total_amount), 0) AS reimbursement_amount").groupBy("department_id");
        reimbursementMapper.selectMaps(wrapper).forEach(row -> merge(result, row, stats -> {
            stats.setReimbursementCount(number(row, "reimbursement_count").longValue());
            stats.setReimbursementAmount(decimal(row, "reimbursement_amount"));
        }));
    }

    private void mergePurchaseStats(Map<UUID, DepartmentStatsVO> result, UUID departmentId) {
        QueryWrapper<Purchase> wrapper = aggregateWrapper(departmentId);
        wrapper.select("department_id", "COUNT(*) AS purchase_count", "COALESCE(SUM(budget_amount), 0) AS purchase_budget")
                .groupBy("department_id");
        purchaseMapper.selectMaps(wrapper).forEach(row -> merge(result, row, stats -> {
            stats.setPurchaseCount(number(row, "purchase_count").longValue());
            stats.setPurchaseBudget(decimal(row, "purchase_budget"));
        }));
    }

    private <T> QueryWrapper<T> aggregateWrapper(UUID departmentId) {
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        wrapper.isNotNull("department_id");
        if (departmentId != null) {
            wrapper.eq("department_id", departmentId);
        }
        // BaseEntity 声明了 created_at 的默认排序；聚合查询必须显式按分组字段排序，
        // 否则 PostgreSQL 会要求将 created_at 也加入 GROUP BY。
        wrapper.orderByAsc("department_id");
        return wrapper;
    }

    private void merge(Map<UUID, DepartmentStatsVO> result, Map<String, Object> row,
            Consumer<DepartmentStatsVO> consumer) {
        UUID departmentId = uuid(row.get("department_id"));
        DepartmentStatsVO stats = result.get(departmentId);
        if (stats != null) {
            consumer.accept(stats);
        }
    }

    private static UUID uuid(Object value) {
        return value instanceof UUID id ? id : value == null ? null : UUID.fromString(value.toString());
    }

    private static Number number(Map<String, Object> row, String key) {
        Object value = row.get(key);
        if (value == null) {
            value = row.entrySet().stream()
                    .filter(entry -> entry.getKey().toLowerCase(Locale.ROOT).equals(key))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElse(0L);
        }
        return value instanceof Number number ? number : new BigDecimal(value.toString());
    }

    private static BigDecimal decimal(Map<String, Object> row, String key) {
        return new BigDecimal(number(row, key).toString());
    }

    private static CellStyle createHeaderStyle(XSSFWorkbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        return style;
    }

    private static void setText(Row row, int column, String value) {
        row.createCell(column).setCellValue(value == null ? "" : value);
    }

    private static void setNumber(Row row, int column, Number value) {
        row.createCell(column).setCellValue(value == null ? 0 : value.doubleValue());
    }
}
