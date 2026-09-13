/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.core.audit.javabean.vo;

import java.util.List;

/** Unified audit page result. */
public record AuditLogPageVO(List<AuditLogVO> records, long total, long current, long size) {

    public AuditLogPageVO {
        records = records == null ? List.of() : List.copyOf(records);
    }
}
