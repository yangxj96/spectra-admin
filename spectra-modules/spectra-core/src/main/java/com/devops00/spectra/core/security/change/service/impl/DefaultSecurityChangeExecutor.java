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

package com.devops00.spectra.core.security.change.service.impl;

import com.devops00.spectra.common.audit.AuditRecord;
import com.devops00.spectra.common.audit.AuditService;
import com.devops00.spectra.core.audit.AuditFailureRecorder;
import com.devops00.spectra.core.audit.AuditFailureResolver;
import com.devops00.spectra.core.security.change.SecurityChangeExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.function.Supplier;

/**
 * 安全相关写操作的统一事务执行器。
 * <p>
 * 先写入 STARTED 审计事实，再运行变更；结果审计与变更复用同一个数据库事务，任何审计失败都会让事务失败。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/14
 */
@Service
@RequiredArgsConstructor
public class DefaultSecurityChangeExecutor implements SecurityChangeExecutor {

    private final AuditService auditService;
    private final AuditFailureResolver failureResolver;
    private final AuditFailureRecorder failureRecorder;

    @Override
    @Transactional
    public <T> T execute(AuditRecord event, Supplier<T> mutation) {
        if (event == null || mutation == null) {
            throw new IllegalArgumentException("安全变更事件和变更操作不能为空");
        }

        auditService.record(event.withResult(AuditRecord.Result.STARTED));
        try {
            T result = mutation.get();
            var succeeded = event.withResult(AuditRecord.Result.SUCCEEDED);
            auditService.record(succeeded);
            return result;
        } catch (RuntimeException exception) {
            AuditRecord failed = failedRecord(event, exception);
            if (TransactionSynchronizationManager.isSynchronizationActive()
                    && TransactionSynchronizationManager.isActualTransactionActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCompletion(int status) {
                        recordFailure(failed, exception);
                    }
                });
            } else {
                recordFailure(failed, exception);
            }
            throw exception;
        }
    }

    /**
     * 处理记录相关数据。
     */
    private AuditRecord failedRecord(AuditRecord event, RuntimeException exception) {
        AuditRecord failed = event.withResult(AuditRecord.Result.FAILED);
        return new AuditRecord(failed.eventId(), failed.category(), failed.eventType(), failed.targetId(),
                failed.result(), failed.occurredAt(), failed.context(), failed.before(), failed.after(),
                failed.reason(), failed.httpSummary(), failureResolver.resolve(exception));
    }

    /**
     * 记录失败。
     */
    private void recordFailure(AuditRecord failed, RuntimeException original) {
        try {
            failureRecorder.record(failed);
        } catch (RuntimeException auditException) {
            if (auditException != original) {
                original.addSuppressed(auditException);
            }
        }
    }
}
