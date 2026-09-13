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

import com.devops00.spectra.core.system.javabean.from.ServiceMonitorDiagnosticFrom;
import com.devops00.spectra.core.system.javabean.vo.ServiceMonitorDiagnosticTaskVO;
import com.devops00.spectra.core.system.javabean.vo.ServiceMonitorRuntimeDiagnosticVO;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

/**
 * 服务监控运行时诊断和受控诊断文件服务。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
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

    /**
     * 定义诊断相关的应用服务契约。
     *
     * @param path        诊断文件或资源的存储路径
     * @param displayName 诊断文件的下载名称
     * @author yangxj96
     * @version 1.0
     * @since 2026/09/13
     */
    record DiagnosticDownload(Path path, String displayName) {
    }
}
