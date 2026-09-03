/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.core.audit;

import com.devops00.spectra.common.audit.AuditSanitizer;
import com.devops00.spectra.common.audit.AuditService;
import com.devops00.spectra.common.audit.DefaultAuditSanitizer;
import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/** Core 统一审计技术入口配置。 */
@Configuration(proxyBeanMethods = false)
public class AuditConfiguration {

    /** 默认统一审计脱敏器。 */
    @Bean
    public AuditSanitizer auditSanitizer() {
        return DefaultAuditSanitizer.INSTANCE;
    }

    /** Core 必选的 Audit 切面；没有统一审计服务时不装配技术入口。 */
    @Bean
    @ConditionalOnBean(AuditService.class)
    public AuditAspect auditAspect(SecurityContextAccessor securityContextAccessor,
                                   AuditService auditService,
                                   AuditSanitizer auditSanitizer,
                                   PlatformTransactionManager transactionManager) {
        return new AuditAspect(securityContextAccessor, auditService, auditSanitizer,
                new TransactionTemplate(transactionManager));
    }
}
