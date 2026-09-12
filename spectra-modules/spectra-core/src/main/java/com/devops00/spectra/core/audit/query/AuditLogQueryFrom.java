/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.core.audit.query;

import com.devops00.spectra.common.audit.AuditCategory;
import com.devops00.spectra.common.audit.AuditRecord;
import lombok.Data;

import java.util.UUID;

/** Filters shared by unified audit list and export requests. */
@Data
public class AuditLogQueryFrom {

    private AuditCategory category;

    private String eventType;

    private UUID operatorId;

    private UUID targetId;

    private AuditRecord.Result result;

    private String from;

    private String to;
}
