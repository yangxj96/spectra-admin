/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.core.audit.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.springframework.stereotype.Component;

import java.util.Locale;

/** Low-cardinality counters for unified audit queries. */
@Component
public class AuditLogMetrics {

    private final MeterRegistry registry;

    public AuditLogMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordQuery(String operation, String outcome) {
        Counter.builder("audit_log_queries_total")
                .tags(Tags.of("operation", safeTag(operation), "outcome", safeTag(outcome)))
                .register(registry)
                .increment();
    }

    private static String safeTag(String value) {
        return value == null || value.isBlank() ? "UNKNOWN" : value.trim().toUpperCase(Locale.ROOT);
    }
}
