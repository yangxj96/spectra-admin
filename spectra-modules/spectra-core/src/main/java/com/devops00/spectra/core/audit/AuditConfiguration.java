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

/**
 * Core 统一审计技术入口配置。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@Configuration(proxyBeanMethods = false)
public class AuditConfiguration {

    /** 默认统一审计脱敏器。 */
    @Bean
    public AuditSanitizer auditSanitizer() {
        return new DefaultAuditSanitizer();
    }

    /** Core 必选的 Audit 切面；没有统一审计服务时不装配技术入口。 */
    @Bean
    @ConditionalOnBean(AuditService.class)
    public AuditAspect auditAspect(SecurityContextAccessor securityContextAccessor,
                                   AuditService auditService,
                                   AuditSanitizer auditSanitizer,
                                   PlatformTransactionManager transactionManager,
                                   AuditFailureResolver failureResolver,
                                   AuditFailureRecorder failureRecorder) {
        return new AuditAspect(securityContextAccessor, auditService, auditSanitizer,
                new TransactionTemplate(transactionManager), failureResolver, failureRecorder);
    }
}
