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

import com.devops00.spectra.core.security.audit.AuditResult;
import com.devops00.spectra.core.security.audit.SecurityAuditEvent;
import com.devops00.spectra.core.security.audit.SecurityAuditWriter;
import com.devops00.spectra.core.security.audit.outbox.SecurityChangeOutboxProducer;
import com.devops00.spectra.core.security.change.SecurityChangeExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private final SecurityAuditWriter securityAuditWriter;

    private final SecurityChangeOutboxProducer securityChangeOutboxProducer;

    @Override
    @Transactional
    public <T> T execute(SecurityAuditEvent event, Supplier<T> mutation) {
        if (event == null || mutation == null) {
            throw new IllegalArgumentException("安全变更事件和变更操作不能为空");
        }

        securityAuditWriter.append(event.started());
        try {
            T result = mutation.get();
            var succeeded = event.withResult(AuditResult.SUCCEEDED);
            securityAuditWriter.append(succeeded);
            securityChangeOutboxProducer.publish(succeeded);
            return result;
        } catch (RuntimeException exception) {
            try {
                securityAuditWriter.append(event.withResult(AuditResult.FAILED));
            } catch (RuntimeException auditException) {
                exception.addSuppressed(auditException);
            }
            throw exception;
        }
    }
}
