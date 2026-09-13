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
import com.devops00.spectra.common.exception.DataException;
import com.devops00.spectra.core.user.javabean.entity.UserImportRow;
import com.devops00.spectra.core.user.javabean.entity.UserImportTask;
import com.devops00.spectra.core.user.javabean.enums.UserImportRowState;
import com.devops00.spectra.core.user.mapper.UserImportRowMapper;
import com.devops00.spectra.core.user.mapper.UserImportTaskMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 用户导入分块 Worker；每 100 行在独立事务中推进行状态与任务进度。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@Service
@RequiredArgsConstructor
public class UserImportExecutionWorker {

    public static final int CHUNK_SIZE = 100;

    private static final String STATE_VALID = UserImportRowState.VALID.name();

    private static final String STATE_APPLIED = UserImportRowState.APPLIED.name();

    private static final String STATE_SKIPPED = UserImportRowState.SKIPPED.name();

    private static final String STATE_ERROR = UserImportRowState.ERROR.name();

    private final UserImportTaskMapper taskMapper;

    private final UserImportRowMapper rowMapper;

    private final UserImportRowProcessor rowProcessor;

    /**
     * 处理用户相关数据。
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ChunkResult processChunk(UUID taskId, UUID operatorId, List<UserImportRow> rows, boolean skipExisting,
                                    UserImportPreviewService.ReferenceData referenceData) {
        var task = taskMapper.selectOne(new LambdaQueryWrapper<UserImportTask>()
                .eq(UserImportTask::getId, taskId)
                .eq(UserImportTask::getOperatorId, operatorId));
        if (task == null) {
            return new ChunkResult(0, 0, 0, 0);
        }

        var completed = task.getCompletedRows();
        var applied = task.getAppliedRows();
        var skipped = task.getSkippedRows();
        var failed = task.getErrorRows();
        var processed = 0;
        for (var row : rows) {
            if (!STATE_VALID.equals(row.getState())) {
                continue;
            }
            try {
                var result = rowProcessor.processInCurrentTransaction(row, skipExisting,
                        referenceData.departmentIds(), referenceData.profiles());
                row.setUserId(result.userId());
                if (result.skipped()) {
                    row.setState(STATE_SKIPPED);
                    skipped++;
                } else {
                    row.setState(STATE_APPLIED);
                    applied++;
                }
                rowMapper.updateById(row);
            } catch (RuntimeException exception) {
                row.setState(STATE_ERROR);
                row.setErrors(Map.of("apply", safeMessage(exception)));
                rowMapper.updateById(row);
                failed++;
            }
            completed++;
            processed++;
        }
        task.setCompletedRows(completed);
        task.setAppliedRows(applied);
        task.setSkippedRows(skipped);
        task.setErrorRows(failed);
        if (taskMapper.updateById(task) != 1) {
            throw new DataException("更新用户导入分块进度失败");
        }
        return new ChunkResult(processed, applied, skipped, failed);
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

    /**
     * 实现结果相关的应用服务逻辑。
     *
     * @param processedRows 本批次已完成处理的导入行数
     * @param appliedRows   本批次成功写入的导入行数
     * @param skippedRows   本批次因校验规则而跳过的导入行数
     * @param errorRows     错误行数据
     * @author yangxj96
     * @version 1.0
     * @since 2026/09/13
     */
    public record ChunkResult(int processedRows, int appliedRows, int skippedRows, int errorRows) {
    }
}
