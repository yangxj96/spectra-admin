/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.core.security.secret.migration;

import java.util.List;

/** 一次性密钥迁移的脱敏结果；不包含任何密钥值。 */
public record SecretMigrationResult(int migratedCount, List<String> codes) {

    public SecretMigrationResult {
        codes = List.copyOf(codes);
    }
}
