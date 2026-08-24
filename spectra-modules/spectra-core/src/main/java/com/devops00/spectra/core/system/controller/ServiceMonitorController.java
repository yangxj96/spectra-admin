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

package com.devops00.spectra.core.system.controller;

import com.devops00.spectra.common.annotation.Encrypt;
import com.devops00.spectra.core.system.javabean.from.ServiceMonitorHistoryFrom;
import com.devops00.spectra.core.system.javabean.from.ServiceMonitorAlertRuleFrom;
import com.devops00.spectra.core.system.javabean.from.ServiceMonitorDiagnosticFrom;
import com.devops00.spectra.core.system.javabean.vo.ServiceMonitorAlertEventVO;
import com.devops00.spectra.core.system.javabean.vo.ServiceMonitorAlertRuleVO;
import com.devops00.spectra.core.system.javabean.vo.ServiceMonitorAlertSummaryVO;
import com.devops00.spectra.core.system.javabean.vo.ServiceMonitorDiagnosticTaskVO;
import com.devops00.spectra.core.system.javabean.vo.ServiceMonitorHistoryVO;
import com.devops00.spectra.core.system.javabean.vo.ServiceMonitorOverviewVO;
import com.devops00.spectra.core.system.javabean.vo.ServiceMonitorRuntimeDiagnosticVO;
import com.devops00.spectra.core.system.service.ServiceMonitorAlertService;
import com.devops00.spectra.core.system.service.ServiceMonitorDiagnosticService;
import com.devops00.spectra.core.system.service.ServiceMonitorService;
import com.devops00.spectra.log.base.annotation.ULog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/**
 * 服务监控总览。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/08/23 00:00
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/service/monitor")
public class ServiceMonitorController {

    private final ServiceMonitorService bindService;
    private final ServiceMonitorAlertService alertService;
    private final ServiceMonitorDiagnosticService diagnosticService;

    /**
     * 获取服务监控总览。
     *
     * @return 服务监控总览
     */
    @ULog("'获取服务监控总览'")
    @GetMapping(value = "/overview", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'system:monitor:read')")
    public ServiceMonitorOverviewVO getOverview() {
        return bindService.getOverview();
    }

    /**
     * 查询服务监控历史趋势。
     *
     * @param from 查询条件
     * @return 历史趋势
     */
    @ULog("'查询服务监控历史'")
    @GetMapping(value = "/history", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'system:monitor:read')")
    public ServiceMonitorHistoryVO getHistory(ServiceMonitorHistoryFrom from) {
        return bindService.getHistory(from);
    }

    /**
     * 查询或获取目标数据（{@code getAlertSummary}）。
     */
    @ULog("'查询服务监控告警摘要'")
    @GetMapping(value = "/alerts/summary", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'system:monitor:alert')")
    public ServiceMonitorAlertSummaryVO getAlertSummary() {
        return alertService.getSummary();
    }

    /**
     * 查询或获取目标数据（{@code listAlertRules}）。
     */
    @ULog("'查询服务监控告警规则'")
    @GetMapping(value = "/alerts/rules", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'system:monitor:alert')")
    public List<ServiceMonitorAlertRuleVO> listAlertRules() {
        return alertService.listRules();
    }

    /**
     * 更新或推进目标状态（{@code modifyAlertRule}）。
     */
    @ULog("'修改服务监控告警规则'")
    @PutMapping(value = "/alerts/rules/{id}", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'system:monitor:configure')")
    public void modifyAlertRule(@PathVariable UUID id, @Validated @RequestBody ServiceMonitorAlertRuleFrom from) {
        alertService.modifyRule(id, from);
    }

    /**
     * 查询或获取目标数据（{@code listAlertEvents}）。
     */
    @ULog("'查询服务监控告警事件'")
    @GetMapping(value = "/alerts/events", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'system:monitor:alert')")
    public List<ServiceMonitorAlertEventVO> listAlertEvents(
                                                            @RequestParam(defaultValue = "true") boolean activeOnly) {
        return alertService.listEvents(activeOnly);
    }

    /**
     * 查询或获取目标数据（{@code getRuntimeDiagnostic}）。
     */
    @ULog("'查询服务运行时诊断'")
    @GetMapping(value = "/diagnostics/runtime", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'system:monitor:diagnose')")
    public ServiceMonitorRuntimeDiagnosticVO getRuntimeDiagnostic() {
        return diagnosticService.getRuntimeDiagnostic();
    }

    /**
     * 创建或构建目标数据（{@code createDiagnosticTask}）。
     */
    @ULog("'创建服务监控诊断任务'")
    @PostMapping(value = "/diagnostics/tasks", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'system:monitor:diagnose')")
    public ServiceMonitorDiagnosticTaskVO createDiagnosticTask(
                                                               @Validated @RequestBody ServiceMonitorDiagnosticFrom from) {
        return diagnosticService.createTask(from);
    }

    /**
     * 查询或获取目标数据（{@code listDiagnosticTasks}）。
     */
    @ULog("'查询服务监控诊断任务'")
    @GetMapping(value = "/diagnostics/tasks", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'system:monitor:diagnose')")
    public List<ServiceMonitorDiagnosticTaskVO> listDiagnosticTasks() {
        return diagnosticService.listTasks();
    }

    /**
     * 查询或获取目标数据（{@code getDiagnosticTask}）。
     */
    @ULog("'查询服务监控诊断任务状态'")
    @GetMapping(value = "/diagnostics/tasks/{id}", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'system:monitor:diagnose')")
    public ServiceMonitorDiagnosticTaskVO getDiagnosticTask(@PathVariable UUID id) {
        return diagnosticService.getTask(id);
    }

    /**
     * 处理内部业务逻辑（{@code downloadDiagnosticTask}）。
     */
    @Encrypt(response = false)
    @ULog("'下载服务监控诊断文件'")
    @GetMapping(value = "/diagnostics/tasks/{id}/download", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'system:monitor:diagnose')")
    public ResponseEntity<Resource> downloadDiagnosticTask(@PathVariable UUID id) {
        var download = diagnosticService.openDownload(id);
        var resource = new FileSystemResource(download.path());
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(download.displayName(), StandardCharsets.UTF_8)
                .build());
        return ResponseEntity.ok().headers(headers).body(resource);
    }
}
