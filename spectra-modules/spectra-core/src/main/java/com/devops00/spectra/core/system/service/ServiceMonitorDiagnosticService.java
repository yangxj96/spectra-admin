/* Copyright 2018-2026 yangxj96 */

package com.devops00.spectra.core.system.service;

import com.devops00.spectra.core.system.javabean.from.ServiceMonitorDiagnosticFrom;
import com.devops00.spectra.core.system.javabean.vo.ServiceMonitorDiagnosticTaskVO;
import com.devops00.spectra.core.system.javabean.vo.ServiceMonitorRuntimeDiagnosticVO;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

/** 服务监控运行时诊断和受控诊断文件服务。 */
public interface ServiceMonitorDiagnosticService {

    /** 查询只读运行时诊断信息。 */
    ServiceMonitorRuntimeDiagnosticVO getRuntimeDiagnostic();

    /** 创建受控诊断任务。 */
    ServiceMonitorDiagnosticTaskVO createTask(ServiceMonitorDiagnosticFrom from);

    /** 查询诊断任务。 */
    ServiceMonitorDiagnosticTaskVO getTask(UUID id);

    /** 查询最近诊断任务。 */
    List<ServiceMonitorDiagnosticTaskVO> listTasks();

    /** 获取经过权限和路径校验的诊断文件。 */
    DiagnosticDownload openDownload(UUID id);

    /** 诊断文件下载信息。 */
    record DiagnosticDownload(Path path, String displayName) {
    }
}
