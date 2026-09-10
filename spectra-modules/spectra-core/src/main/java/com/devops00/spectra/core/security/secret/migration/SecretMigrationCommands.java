/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.core.security.secret.migration;

import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.core.security.secret.service.SecretManagementService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** 迁移命令的共同执行器。 */
final class SecretMigrationCommands {

    private SecretMigrationCommands() {
    }

    static SecretMigrationResult migrate(SecretManagementService service, List<String> codes,
                                         Map<String, String> legacyValues) {
        if (legacyValues == null) {
            throw new DataSaveException("旧密钥快照不能为空");
        }
        var migrated = new ArrayList<String>();
        for (String code : codes) {
            String value = legacyValues.get(code);
            if (value == null || value.isBlank()) {
                continue;
            }
            service.createPending(code, value, "MIGRATION");
            migrated.add(code);
        }
        return new SecretMigrationResult(migrated.size(), migrated);
    }
}
