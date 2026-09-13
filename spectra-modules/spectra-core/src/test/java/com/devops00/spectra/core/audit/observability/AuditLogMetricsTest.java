/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.core.audit.observability;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuditLogMetricsTest {

    @Test
    void recordsOnlyLowCardinalityQueryMetrics() {
        var registry = new SimpleMeterRegistry();
        var metrics = new AuditLogMetrics(registry);

        metrics.recordQuery("export", "succeeded");

        assertEquals(1.0, registry.get("audit_log_queries_total")
                .tag("operation", "EXPORT")
                .tag("outcome", "SUCCEEDED")
                .counter()
                .count());
    }
}
