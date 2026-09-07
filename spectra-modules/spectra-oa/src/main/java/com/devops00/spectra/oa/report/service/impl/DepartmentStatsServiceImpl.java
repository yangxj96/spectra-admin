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

import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.common.port.directory.DirectoryDepartmentSnapshot;
import com.devops00.spectra.common.port.directory.DirectoryQueryPort;
import com.devops00.spectra.oa.report.javabean.converter.DepartmentStatsConverter;
import com.devops00.spectra.oa.report.javabean.entity.DepartmentStatsRow;
import com.devops00.spectra.oa.report.javabean.from.DepartmentStatsFrom;
import com.devops00.spectra.oa.report.javabean.vo.DepartmentStatsVO;
import com.devops00.spectra.oa.report.mapper.DepartmentStatsQueryMapper;
import com.devops00.spectra.oa.report.service.DepartmentStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 部门维度统计服务实现。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/7
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentStatsServiceImpl implements DepartmentStatsService {

    private static final String[] HEADERS = {"部门", "资产条目数", "资产数量", "资产金额", "办公用品 SKU 数", "当前库存", "最低库存", "报销单数", "报销金额", "采购申请数", "采购预算"};

    private final DirectoryQueryPort directoryQueryPort;
    private final DepartmentStatsQueryMapper departmentStatsQueryMapper;
    private final DepartmentStatsConverter departmentStatsConverter;

    @Override
    public List<DepartmentStatsVO> list(DepartmentStatsFrom from) {
        UUID departmentId = from == null ? null : from.getDepartmentId();
        Map<UUID, DepartmentStatsVO> result = directoryQueryPort.listDepartments()
                .stream()
                .filter(department -> departmentId == null || departmentId.equals(department.id()))
                .collect(Collectors.toMap(DirectoryDepartmentSnapshot::id, department -> {
                    var vo = departmentStatsConverter.toVO(department);
                    vo.setDepartmentName(department.path() == null ? department.name() : department.path());
                    return vo;
                }, (left, right) -> left, HashMap::new));

        if (result.isEmpty()) {
            return Collections.emptyList();
        }
        departmentStatsQueryMapper.selectByDepartmentIds(result.keySet())
                .forEach(row -> merge(result, row));

        return result.values()
                .stream()
                // 只返回当前数据权限范围内确有业务数据的部门，避免聚合接口泄露无权部门名称。
                .filter(this::hasBusinessData)
                .sorted(Comparator.comparing(DepartmentStatsVO::getDepartmentName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
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
            throw new DataSaveException("导出部门统计失败", exception);
        }
    }

    /**
     * 判断条件是否满足（{@code hasBusinessData}）。
     */
    private boolean hasBusinessData(DepartmentStatsVO stats) {
        return stats.getAssetCount() > 0 || stats.getSupplySkuCount() > 0 || stats.getReimbursementCount() > 0 || stats.getPurchaseCount() > 0;
    }

    /**
     * 合并统一报表查询返回的统计行。
     */
    private void merge(Map<UUID, DepartmentStatsVO> result, DepartmentStatsRow row) {
        DepartmentStatsVO stats = result.get(row.getDepartmentId());
        if (stats != null) {
            stats.setAssetCount(row.getAssetCount());
            stats.setAssetQuantity(row.getAssetQuantity());
            stats.setAssetValue(row.getAssetValue());
            stats.setSupplySkuCount(row.getSupplySkuCount());
            stats.setSupplyStock(row.getSupplyStock());
            stats.setSupplyMinStock(row.getSupplyMinStock());
            stats.setReimbursementCount(row.getReimbursementCount());
            stats.setReimbursementAmount(row.getReimbursementAmount());
            stats.setPurchaseCount(row.getPurchaseCount());
            stats.setPurchaseBudget(row.getPurchaseBudget());
        }
    }

    /**
     * 创建或构建目标数据（{@code createHeaderStyle}）。
     */
    private static CellStyle createHeaderStyle(XSSFWorkbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        return style;
    }

    /**
     * 更新或推进目标状态（{@code setText}）。
     */
    private static void setText(Row row, int column, String value) {
        row.createCell(column).setCellValue(value == null ? "" : value);
    }

    /**
     * 更新或推进目标状态（{@code setNumber}）。
     */
    private static void setNumber(Row row, int column, Number value) {
        row.createCell(column).setCellValue(value == null ? 0 : value.doubleValue());
    }
}
