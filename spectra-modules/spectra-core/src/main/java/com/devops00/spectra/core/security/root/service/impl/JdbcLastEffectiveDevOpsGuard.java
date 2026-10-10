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

package com.devops00.spectra.core.security.root.service.impl;

import com.devops00.spectra.common.audit.AuditService;
import com.devops00.spectra.core.security.root.LastEffectiveDevOpsGuard;
import com.devops00.spectra.core.security.root.RootGovernanceConflictException;
import com.devops00.spectra.core.security.root.RootGovernanceException;
import com.devops00.spectra.core.security.root.RootPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 在数据库行锁和审计可用性门禁下保护 DEV_OPS 生命周期。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/14
 */
@Service
@RequiredArgsConstructor
public class JdbcLastEffectiveDevOpsGuard implements LastEffectiveDevOpsGuard {

    private final RootPolicyRepository rootPolicyRepository;
    private final AuditService auditService;

    @Override
    public Snapshot lockForChange() {
        requireTransaction();
        auditService.assertAvailable();
        var policy = rootPolicyRepository.lock();
        return new Snapshot(policy, rootPolicyRepository.countEffectiveDevOpsUsers());
    }

    @Override
    public void assertWithinLimits(Snapshot before) {
        requireTransaction();
        if (before == null) {
            throw new RootGovernanceException("缺少 Root 策略锁定快照，拒绝变更");
        }
        long current = rootPolicyRepository.countEffectiveDevOpsUsers();
        if (current < before.effectiveCount() && current < before.policy().minEffectiveDevOpsUsers()) {
            throw new RootGovernanceConflictException("拒绝移除最后一个有效 DEV_OPS");
        }
        if (current > before.effectiveCount() && current > before.policy().maxDevOpsUsers()) {
            throw new RootGovernanceConflictException("已达到 maxDevOpsUsers，拒绝新增 DEV_OPS");
        }
    }

    private void requireTransaction() {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new RootGovernanceException("Root 人数变更必须在数据库事务内执行");
        }
    }
}
