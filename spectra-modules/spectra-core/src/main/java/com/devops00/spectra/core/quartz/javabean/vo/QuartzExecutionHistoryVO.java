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

package com.devops00.spectra.core.quartz.javabean.vo;

import com.devops00.spectra.core.quartz.javabean.enums.QuartzExecutionHistoryStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 封装Quartz相关的响应数据。
 *
 * @param id                数据记录的唯一标识
 * @param fireInstanceId    数据记录的唯一标识
 * @param jobKey            执行历史对应的 Quartz JobKey
 * @param triggerKey        触发器键
 * @param jobType           作业类型
 * @param jobClassName      执行作业的实现类全限定类名
 * @param triggerType       触发器类型
 * @param status            业务状态
 * @param scheduledFireAt   计划触发时间
 * @param actualFireAt      实际触发时间
 * @param startedAt         上传或处理任务的开始时间
 * @param finishedAt        作业执行结束时间
 * @param durationMs        作业本次执行耗时（毫秒）
 * @param schedulerInstance 执行作业的调度器实例标识
 * @param correlationId     关联标识
 * @param parameterVersion  参数版本
 * @param parameterSha256   作业参数 JSON 的 SHA-256 摘要
 * @param resultSummary     作业执行结果的摘要说明
 * @param errorCode         错误编码
 * @param errorMessage      错误消息
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
public record QuartzExecutionHistoryVO(
                                       UUID id,
                                       String fireInstanceId,
                                       String jobKey,
                                       String triggerKey,
                                       String jobType,
                                       String jobClassName,
                                       String triggerType,
                                       QuartzExecutionHistoryStatus status,
                                       LocalDateTime scheduledFireAt,
                                       LocalDateTime actualFireAt,
                                       LocalDateTime startedAt,
                                       LocalDateTime finishedAt,
                                       Long durationMs,
                                       String schedulerInstance,
                                       String correlationId,
                                       String parameterVersion,
                                       String parameterSha256,
                                       String resultSummary,
                                       String errorCode,
                                       String errorMessage) {
}
