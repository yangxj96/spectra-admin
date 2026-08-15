/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.core.security.audit;

import java.time.Instant;

/**
 * 安全审计归档存储端口。
 *
 * <p>实现必须提供不可变对象或等价的 WORM 语义；应用层不得通过该端口删除或覆盖归档对象。</p>
 */
public interface SecurityAuditArchiveBackend {

    /** 后端标识，必须和 retention policy 中的 backend 一致。 */
    String id();

    /** 写入一份带保留截止时间的归档对象。 */
    SecurityAuditArchiveReceipt put(String objectKey, byte[] content, Instant retainUntil);

    /** 按 manifest 中的 URI 读取归档对象，供恢复校验使用。 */
    byte[] read(String objectUri);
}
