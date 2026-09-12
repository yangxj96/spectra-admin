/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.core.audit;

import com.devops00.spectra.common.audit.AuditRecord;
import com.devops00.spectra.common.audit.AuditService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 在业务事务回滚后独立保存失败审计记录。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/9/13
 */
@Component
public class AuditFailureRecorder {

    private final AuditService auditService;
    private final TransactionTemplate transactionTemplate;

    /**
     * 创建失败记录器，并固定使用独立事务。
     *
     * @param auditService 统一审计服务
     * @param transactionManager Core 事务管理器
     */
    public AuditFailureRecorder(AuditService auditService, PlatformTransactionManager transactionManager) {
        this.auditService = auditService;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    /**
     * 在独立事务中写入失败记录。
     *
     * @param record 失败审计记录
     */
    public void record(AuditRecord record) {
        transactionTemplate.executeWithoutResult(status -> auditService.record(record));
    }
}
