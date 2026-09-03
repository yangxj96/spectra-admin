/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.core.security.audit.outbox;

import com.devops00.spectra.core.security.audit.SecurityAuditEvent;

/**
 * 安全变更 outbox 的下游动作扩展端口。
 *
 * <p>Core 只负责事务投递、租约和结果确认；归档索引、通知或外部同步模块通过此端口接入，
 * 不得反向依赖安全事实表的 Mapper。</p>
 */
public interface SecurityChangeOutboxHandler {

    /** 是否接收指定事件类型。 */
    boolean supports(String eventType);

    /** 执行一个已经脱敏的安全变更动作。 */
    void handle(SecurityChangeOutboxRepository.SecurityChangeOutboxEvent event,
                SecurityAuditEvent auditEvent);
}
