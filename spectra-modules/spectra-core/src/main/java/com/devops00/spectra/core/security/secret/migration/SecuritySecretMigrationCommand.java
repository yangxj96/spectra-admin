/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.core.security.secret.migration;

import com.devops00.spectra.core.security.secret.service.SecretManagementService;

import java.util.List;
import java.util.Map;

/** 一次性迁移安全签名密钥；完成后验证码和授权变更服务只读取统一运行态。 */
public final class SecuritySecretMigrationCommand {

    private static final List<String> CODES = List.of(
            "security.verification-code-hmac", "security.authorization-change-token-hmac",
            "security.request-integrity-hmac", "security.csrf-signing-key");

    private final SecretManagementService service;

    public SecuritySecretMigrationCommand(SecretManagementService service) {
        this.service = service;
    }

    public SecretMigrationResult migrate(Map<String, String> legacyValues) {
        return SecretMigrationCommands.migrate(service, CODES, legacyValues);
    }
}
