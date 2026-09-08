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

    /**
     * 查询只读运行时诊断信息。
     *
     * @return 返回当前 JVM、CPU、内存、磁盘和线程等运行时诊断快照；采集失败时抛出业务异常，不返回 null。
     */
    ServiceMonitorRuntimeDiagnosticVO getRuntimeDiagnostic();

    /**
     * 创建受控诊断任务。
     *
     * @param from 诊断类型、采集范围和输出格式等受控诊断任务字段。
     * @return 返回新建诊断任务的标识、类型和排队状态；参数校验或写入失败时抛出业务异常，不返回 null。
     */
    ServiceMonitorDiagnosticTaskVO createTask(ServiceMonitorDiagnosticFrom from);

    /**
     * 查询诊断任务。
     *
     * @param id 要读取详情的诊断任务唯一标识。
     * @return 返回指定诊断任务的类型、状态、执行时间和结果摘要；任务不存在或不可见时抛出业务异常，不返回 null。
     */
    ServiceMonitorDiagnosticTaskVO getTask(UUID id);

    /**
     * 查询最近诊断任务。
     *
     * @return 返回最近创建的诊断任务及其当前状态列表；没有诊断任务时返回空列表，不返回 null。
     */
    List<ServiceMonitorDiagnosticTaskVO> listTasks();

    /**
     * 获取经过权限和路径校验的诊断文件。
     *
     * @param id 要打开下载文件的诊断任务唯一标识。
     * @return 返回已通过路径白名单校验的诊断文件路径和下载文件名；任务未完成、文件不存在或校验失败时抛出业务异常，不返回 null。
     */
    DiagnosticDownload openDownload(UUID id);

    /** 诊断文件下载信息。 */
    record DiagnosticDownload(Path path, String displayName) {
    }
}
