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

package com.devops00.spectra.core.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.devops00.spectra.common.exception.DataException;
import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import com.devops00.spectra.core.user.javabean.entity.UserImportRow;
import com.devops00.spectra.core.user.javabean.entity.UserImportTask;
import com.devops00.spectra.core.user.javabean.enums.UserImportRowState;
import com.devops00.spectra.core.user.javabean.enums.UserImportTaskStatus;
import com.devops00.spectra.core.user.javabean.from.UserImportApplyFrom;
import com.devops00.spectra.core.user.javabean.vo.UserImportTaskVO;
import com.devops00.spectra.core.user.mapper.UserImportRowMapper;
import com.devops00.spectra.core.user.mapper.UserImportTaskMapper;
import com.devops00.spectra.core.user.provider.DefaultUserPasswordProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.security.concurrent.DelegatingSecurityContextRunnable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * 用户导入 Apply 用例，负责一次性 token 消费、异步调度和最终任务状态汇总。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserImportExecutionService {

    private static final String STATUS_PREVIEWED = UserImportTaskStatus.PREVIEWED.name();

    private static final String STATUS_APPLYING = UserImportTaskStatus.APPLYING.name();

    private static final String STATUS_SUCCEEDED = UserImportTaskStatus.SUCCEEDED.name();

    private static final String STATUS_PARTIAL_FAILED = UserImportTaskStatus.PARTIAL_FAILED.name();

    private static final String STATUS_FAILED = UserImportTaskStatus.FAILED.name();

    private final UserImportTaskMapper taskMapper;

    private final UserImportRowMapper rowMapper;

    private final UserImportPreviewService previewService;

    private final UserImportResultService resultService;

    private final SecurityContextAccessor securityContextAccessor;

    private final UserImportExecutionWorker executionWorker;

    private final DefaultUserPasswordProvider defaultUserPasswordProvider;

    @Qualifier("userImportTaskExecutor")
    private final TaskExecutor userImportTaskExecutor;

    /**
     * 校验并消费 Preview token，提交异步 Apply 任务。
     *
     * @param id     待应用的导入任务标识。
     * @param params Preview Apply 确认参数。
     * @return 返回任务进入异步处理时的状态摘要。
     */
    @Transactional
    public UserImportTaskVO apply(UUID id, UserImportApplyFrom params) {
        var task = resultService.requireTask(id);
        if (STATUS_SUCCEEDED.equals(task.getStatus())
                || STATUS_PARTIAL_FAILED.equals(task.getStatus())
                || STATUS_FAILED.equals(task.getStatus())
                || STATUS_APPLYING.equals(task.getStatus())) {
            return resultService.toVO(task);
        }
        if (!STATUS_PREVIEWED.equals(task.getStatus())) {
            throw new DataException("当前导入任务不可 Apply: " + task.getStatus());
        }
        previewService.validateApply(task, params);
        var encodedDefaultPasswordHash = defaultUserPasswordProvider.requireEncodedPassword();

        var claimedAt = Instant.now();
        var operatorId = currentOperatorId();
        var claim = new LambdaUpdateWrapper<UserImportTask>()
                .eq(UserImportTask::getId, task.getId())
                .eq(UserImportTask::getOperatorId, operatorId)
                .eq(UserImportTask::getStatus, STATUS_PREVIEWED)
                .isNull(UserImportTask::getPreviewConsumedAt)
                .set(UserImportTask::getStatus, STATUS_APPLYING)
                .set(UserImportTask::getPreviewConsumedAt, claimedAt)
                .set(UserImportTask::getPreviewTokenHash, null)
                .set(UserImportTask::getCompletedRows, task.getErrorRows())
                .set(UserImportTask::getSkippedRows, 0)
                .set(UserImportTask::getAppliedRows, 0);
        if (taskMapper.update(null, claim) != 1) {
            throw new DataException("用户导入任务已被其他请求处理");
        }
        task.setStatus(STATUS_APPLYING);
        task.setPreviewConsumedAt(claimedAt);
        task.setPreviewTokenHash(null);
        task.setCompletedRows(task.getErrorRows());
        task.setSkippedRows(0);
        task.setAppliedRows(0);

        var securityContext = SecurityContextHolder.getContext();
        try {
            userImportTaskExecutor.execute(new DelegatingSecurityContextRunnable(
                    () -> processApply(task.getId(), operatorId, encodedDefaultPasswordHash), securityContext));
        } catch (RuntimeException exception) {
            markApplyFailed(task.getId(), operatorId, exception);
            throw new DataException("无法启动用户导入任务: " + safeMessage(exception), exception);
        }
        return resultService.toVO(task);
    }

    /**
     * 使用 Apply 开始时读取的默认密码哈希处理整批导入数据。
     *
     * @param taskId                     待处理的导入任务标识。
     * @param operatorId                 发起 Apply 的操作者标识。
     * @param encodedDefaultPasswordHash 本批次统一使用的默认密码哈希。
     */
    private void processApply(UUID taskId, UUID operatorId, String encodedDefaultPasswordHash) {
        try {
            var task = resultService.findTask(taskId, operatorId);
            if (task == null) {
                return;
            }
            var rows = rowMapper.selectList(new LambdaQueryWrapper<UserImportRow>()
                    .eq(UserImportRow::getTaskId, taskId)
                    .orderByAsc(UserImportRow::getRowNumber));
            var validRows = rows.stream()
                    .filter(row -> UserImportRowState.VALID.name().equals(row.getState()))
                    .toList();
            var referenceData = previewService.loadReferenceData();
            var processed = 0;
            var applied = task.getAppliedRows();
            var skipped = task.getSkippedRows();
            var failed = task.getErrorRows();
            for (int start = 0; start < validRows.size(); start += UserImportExecutionWorker.CHUNK_SIZE) {
                var end = Math.min(start + UserImportExecutionWorker.CHUNK_SIZE, validRows.size());
                var chunk = executionWorker.processChunk(taskId, operatorId, validRows.subList(start, end),
                        task.isSkipExisting(), referenceData, encodedDefaultPasswordHash);
                processed += chunk.processedRows();
                applied = chunk.appliedRows();
                skipped = chunk.skippedRows();
                failed = chunk.errorRows();
            }
            task.setAppliedRows(applied);
            task.setSkippedRows(skipped);
            task.setErrorRows(failed);
            task.setStatus(failed == 0 ? STATUS_SUCCEEDED : processed == 0 ? STATUS_FAILED : STATUS_PARTIAL_FAILED);
            if (taskMapper.updateById(task) != 1) {
                throw new DataException("更新用户导入最终状态失败");
            }
            log.info("用户批量导入完成: taskId={}, status={}, applied={}, skipped={}, failed={}", task.getId(),
                    task.getStatus(), applied, skipped, failed);
        } catch (RuntimeException exception) {
            markApplyFailed(taskId, operatorId, exception);
        }
    }

    /**
     * 设置应用。
     */
    private void markApplyFailed(UUID taskId, UUID operatorId, RuntimeException exception) {
        taskMapper.update(null, new LambdaUpdateWrapper<UserImportTask>()
                .eq(UserImportTask::getId, taskId)
                .eq(UserImportTask::getOperatorId, operatorId)
                .eq(UserImportTask::getStatus, STATUS_APPLYING)
                .set(UserImportTask::getStatus, STATUS_FAILED));
        log.error("用户批量导入执行失败: taskId={}", taskId, exception);
    }

    /**
     * 查询标识。
     */
    private UUID currentOperatorId() {
        var operatorId = securityContextAccessor.currentUserId();
        if (operatorId == null) {
            throw new DataException("无法识别当前安全主体");
        }
        return operatorId;
    }

    /**
     * 处理安全消息相关数据。
     */
    private String safeMessage(RuntimeException exception) {
        var message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return "导入行处理失败";
        }
        return message.length() > 500 ? message.substring(0, 500) : message;
    }
}
