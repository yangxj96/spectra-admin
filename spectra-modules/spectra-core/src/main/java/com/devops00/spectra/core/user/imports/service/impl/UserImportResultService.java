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

package com.devops00.spectra.core.user.imports.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.devops00.spectra.common.exception.DataException;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import com.devops00.spectra.core.user.imports.entity.UserImportRow;
import com.devops00.spectra.core.user.imports.entity.UserImportTask;
import com.devops00.spectra.core.user.imports.javabean.vo.UserImportRowVO;
import com.devops00.spectra.core.user.imports.javabean.vo.UserImportTaskVO;
import com.devops00.spectra.core.user.imports.mapper.UserImportRowMapper;
import com.devops00.spectra.core.user.imports.mapper.UserImportTaskMapper;
import com.devops00.spectra.framework.serialization.mapper.TimeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/** 用户导入任务与错误行的查询及响应映射服务。 */
@Service
@RequiredArgsConstructor
public class UserImportResultService {

    private static final String STATE_ERROR = "ERROR";

    private final UserImportTaskMapper taskMapper;

    private final UserImportRowMapper rowMapper;

    private final SecurityContextAccessor securityContextAccessor;

    private final TimeMapper timeMapper;

    /** 查询当前操作者可见的导入任务详情。 */
    public UserImportTaskVO detail(UUID id) {
        return toVO(requireTask(id));
    }

    /** 查询当前操作者可见的导入错误行。 */
    public List<UserImportRowVO> errors(UUID id) {
        var task = taskMapper.selectOne(new LambdaQueryWrapper<UserImportTask>()
                .eq(UserImportTask::getId, id)
                .eq(UserImportTask::getOperatorId, currentOperatorId()));
        if (task == null) {
            throw new DataNotExistException("用户导入任务不存在");
        }
        return rowMapper.selectList(new LambdaQueryWrapper<UserImportRow>()
                .eq(UserImportRow::getTaskId, task.getId())
                .eq(UserImportRow::getState, STATE_ERROR)
                .orderByAsc(UserImportRow::getRowNumber))
                .stream()
                .map(this::toRowVO)
                .toList();
    }

    /** 读取当前操作者的任务，供写入用例执行权限边界校验。 */
    public UserImportTask requireTask(UUID id) {
        var task = taskMapper.selectOne(new LambdaQueryWrapper<UserImportTask>()
                .eq(UserImportTask::getId, id)
                .eq(UserImportTask::getOperatorId, currentOperatorId()));
        if (task == null) {
            throw new DataNotExistException("用户导入任务不存在");
        }
        return task;
    }

    /** 按任务和操作者读取异步执行上下文。 */
    public UserImportTask findTask(UUID taskId, UUID operatorId) {
        return taskMapper.selectOne(new LambdaQueryWrapper<UserImportTask>()
                .eq(UserImportTask::getId, taskId)
                .eq(UserImportTask::getOperatorId, operatorId));
    }

    /** 将任务映射为 API 响应。 */
    public UserImportTaskVO toVO(UserImportTask task) {
        return toVO(task, null);
    }

    /** 将任务和一次性 Preview token 映射为 API 响应。 */
    public UserImportTaskVO toVO(UserImportTask task, String previewToken) {
        var result = new UserImportTaskVO();
        result.setId(task.getId());
        result.setFileName(task.getFileName());
        result.setFileHash(task.getFileHash());
        result.setSkipExisting(task.isSkipExisting());
        result.setStatus(task.getStatus());
        result.setExpiresAt(timeMapper.toLocalDateTime(task.getExpiresAt()));
        result.setPreviewExpiresAt(timeMapper.toLocalDateTime(task.getPreviewExpiresAt()));
        result.setTotalRows(task.getTotalRows());
        result.setValidRows(task.getValidRows());
        result.setErrorRows(task.getErrorRows());
        result.setSkippedRows(task.getSkippedRows());
        result.setAppliedRows(task.getAppliedRows());
        result.setCompletedRows(task.getCompletedRows());
        result.setAssignmentCount(task.getAssignmentCount());
        result.setAccessBoundaryCount(task.getAccessBoundaryCount());
        result.setGrantBoundaryCount(task.getGrantBoundaryCount());
        result.setPreviewToken(previewToken);
        return result;
    }

    /** 读取当前安全主体 ID。 */
    public UUID currentOperatorId() {
        var operatorId = securityContextAccessor.currentUserId();
        if (operatorId == null) {
            throw new DataException("无法识别当前安全主体");
        }
        return operatorId;
    }

    private UserImportRowVO toRowVO(UserImportRow row) {
        var result = new UserImportRowVO();
        result.setId(row.getId());
        result.setRowNumber(row.getRowNumber());
        result.setRowKey(row.getRowKey());
        result.setState(row.getState());
        result.setUserId(row.getUserId());
        var errors = new ArrayList<String>();
        if (row.getErrors() != null) {
            row.getErrors().values().forEach(value -> {
                if (value instanceof Collection<?> collection) {
                    collection.forEach(item -> errors.add(String.valueOf(item)));
                } else {
                    errors.add(String.valueOf(value));
                }
            });
        }
        result.setErrors(errors);
        return result;
    }
}
