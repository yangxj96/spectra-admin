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

package com.devops00.spectra.oa.report.controller;

import com.devops00.spectra.common.audit.Audit;
import com.devops00.spectra.oa.report.javabean.from.DepartmentStatsFrom;
import com.devops00.spectra.oa.report.javabean.vo.DepartmentStatsVO;
import com.devops00.spectra.oa.report.service.DepartmentStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 报表主接口
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/3/5 23:23
 */
@Slf4j
@RestController
@RequestMapping("/oa/report")
@RequiredArgsConstructor
public class ReportController {

    private final DepartmentStatsService departmentStatsService;

    /**
     * 查询部门维度统计。
     */
    @Audit("'查询部门维度统计'")
    @GetMapping(value = "/department", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'oa:report:read')")
    public List<DepartmentStatsVO> department(DepartmentStatsFrom from) {
        return departmentStatsService.list(from);
    }

    /**
     * 导出部门维度统计。
     */
    @Audit("'导出部门维度统计'")
    @GetMapping(value = "/department/export", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'oa:report:read')")
    public ResponseEntity<byte[]> exportDepartment(DepartmentStatsFrom from) {
        String filename = URLEncoder.encode("部门统计.xlsx", StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename)
                .body(departmentStatsService.export(from));
    }
}
