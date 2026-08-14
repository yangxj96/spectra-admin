/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.core.security.authentication;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.devops00.spectra.core.security.authentication.entity.MfaEnrollment;
import com.devops00.spectra.core.security.authentication.entity.RecoveryCode;
import com.devops00.spectra.core.security.authentication.entity.TotpCredential;
import com.devops00.spectra.core.security.authentication.mapper.MfaEnrollmentMapper;
import com.devops00.spectra.core.security.authentication.mapper.RecoveryCodeMapper;
import com.devops00.spectra.core.security.authentication.mapper.TotpCredentialMapper;
import com.devops00.spectra.security.base.properties.SecurityProperties;
import com.devops00.spectra.security.base.security.mfa.RecoveryCodeHasher;
import com.devops00.spectra.security.base.security.mfa.TotpCodeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

/** 默认 PostgreSQL MFA 实现。 */
@Service
public class MfaServiceImpl implements MfaService {

    private static final String FACTOR_TOTP = "TOTP";
    private static final String PENDING = "PENDING";
    private static final String ACTIVE = "ACTIVE";

    private final MfaEnrollmentMapper enrollmentMapper;
    private final TotpCredentialMapper credentialMapper;
    private final RecoveryCodeMapper recoveryCodeMapper;
    private final SecurityProperties properties;
    private final Clock clock = Clock.systemUTC();
    private final SecureRandom random = new SecureRandom();

    public MfaServiceImpl(MfaEnrollmentMapper enrollmentMapper, TotpCredentialMapper credentialMapper,
                          RecoveryCodeMapper recoveryCodeMapper, SecurityProperties properties) {
        this.enrollmentMapper = enrollmentMapper;
        this.credentialMapper = credentialMapper;
        this.recoveryCodeMapper = recoveryCodeMapper;
        this.properties = properties;
    }

    @Override
    @Transactional
    public MfaEnrollmentResult beginTotpEnrollment(UUID userId) {
        MfaEnrollment enrollment = new MfaEnrollment();
        enrollment.setId(UUID.randomUUID());
        enrollment.setUserId(userId);
        enrollment.setFactorType(FACTOR_TOTP);
        enrollment.setState(PENDING);
        enrollment.setCreatedAt(Instant.now(clock));
        enrollment.setVersion(0L);
        if (enrollmentMapper.insert(enrollment) != 1) {
            throw new IllegalStateException("创建 MFA 登记失败");
        }

        String secret = TotpCodeService.generateSecret();
        TotpSecretCipher.EncryptedSecret encrypted = new TotpSecretCipher(properties).encrypt(secret);
        TotpCredential credential = new TotpCredential();
        credential.setEnrollmentId(enrollment.getId());
        credential.setEncryptedSecret(encrypted.combined());
        credential.setKeyVersion(encrypted.keyVersion());
        credential.setCreatedAt(Instant.now(clock));
        if (credentialMapper.insert(credential) != 1) {
            throw new IllegalStateException("保存 MFA 密钥失败");
        }
        String account = userId.toString();
        String issuer = java.net.URLEncoder.encode(properties.getMfaTotpIssuer(), StandardCharsets.UTF_8);
        String label = java.net.URLEncoder.encode(properties.getMfaTotpIssuer() + ":" + account, StandardCharsets.UTF_8);
        String uri = "otpauth://totp/" + label + "?secret="
                + secret
                + "&issuer=" + issuer + "&algorithm=SHA1&digits=6&period=30";
        return new MfaEnrollmentResult(enrollment.getId(), uri, secret);
    }

    @Override
    @Transactional
    public List<String> confirmTotpEnrollment(UUID userId, UUID enrollmentId, String code) {
        MfaEnrollment enrollment = findEnrollment(userId, enrollmentId, PENDING);
        String secret = decryptSecret(enrollment);
        if (!TotpCodeService.matches(secret, code, clock, 1)) {
            throw new IllegalArgumentException("TOTP 验证码错误");
        }
        enrollment.setState(ACTIVE);
        enrollment.setEnrolledAt(Instant.now(clock));
        if (enrollmentMapper.updateById(enrollment) != 1) {
            throw new IllegalStateException("激活 MFA 登记失败");
        }

        List<String> recoveryCodes = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            String recoveryCode = generateRecoveryCode();
            RecoveryCode entity = new RecoveryCode();
            entity.setId(UUID.randomUUID());
            entity.setEnrollmentId(enrollmentId);
            entity.setCodeHash(RecoveryCodeHasher.hash(recoveryCode));
            entity.setVersion(0L);
            if (recoveryCodeMapper.insert(entity) != 1) {
                throw new IllegalStateException("保存 Recovery Code 失败");
            }
            recoveryCodes.add(recoveryCode);
        }
        return List.copyOf(recoveryCodes);
    }

    @Override
    public boolean verifyTotp(UUID userId, String code) {
        MfaEnrollment enrollment = findActiveEnrollment(userId);
        return enrollment != null && TotpCodeService.matches(decryptSecret(enrollment), code, clock, 1);
    }

    @Override
    @Transactional
    public boolean consumeRecoveryCode(UUID userId, String code) {
        MfaEnrollment enrollment = findActiveEnrollment(userId);
        if (enrollment == null || code == null || code.isBlank()) {
            return false;
        }
        List<RecoveryCode> codes = recoveryCodeMapper.selectList(new LambdaQueryWrapper<RecoveryCode>()
                .eq(RecoveryCode::getEnrollmentId, enrollment.getId())
                .isNull(RecoveryCode::getUsedAt));
        for (RecoveryCode recoveryCode : codes) {
            if (RecoveryCodeHasher.matches(code, recoveryCode.getCodeHash())) {
                int updated = recoveryCodeMapper.update(null, new LambdaUpdateWrapper<RecoveryCode>()
                        .eq(RecoveryCode::getId, recoveryCode.getId())
                        .eq(RecoveryCode::getVersion, recoveryCode.getVersion())
                        .isNull(RecoveryCode::getUsedAt)
                        .set(RecoveryCode::getUsedAt, Instant.now(clock))
                        .set(RecoveryCode::getVersion, recoveryCode.getVersion() + 1));
                return updated == 1;
            }
        }
        return false;
    }

    private MfaEnrollment findActiveEnrollment(UUID userId) {
        return enrollmentMapper.selectOne(new LambdaQueryWrapper<MfaEnrollment>()
                .eq(MfaEnrollment::getUserId, userId)
                .eq(MfaEnrollment::getFactorType, FACTOR_TOTP)
                .eq(MfaEnrollment::getState, ACTIVE)
                .last("LIMIT 1"));
    }

    private MfaEnrollment findEnrollment(UUID userId, UUID enrollmentId, String state) {
        MfaEnrollment enrollment = enrollmentMapper.selectOne(new LambdaQueryWrapper<MfaEnrollment>()
                .eq(MfaEnrollment::getId, enrollmentId)
                .eq(MfaEnrollment::getUserId, userId)
                .eq(MfaEnrollment::getFactorType, FACTOR_TOTP)
                .eq(MfaEnrollment::getState, state));
        if (enrollment == null) {
            throw new IllegalArgumentException("MFA 登记不存在或状态无效");
        }
        return enrollment;
    }

    private String decryptSecret(MfaEnrollment enrollment) {
        TotpCredential credential = credentialMapper.selectById(enrollment.getId());
        if (credential == null) {
            throw new IllegalStateException("MFA 密钥不存在");
        }
        return new TotpSecretCipher(properties).decrypt(credential.getKeyVersion(), credential.getEncryptedSecret());
    }

    private String generateRecoveryCode() {
        byte[] bytes = new byte[8];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes).toUpperCase(java.util.Locale.ROOT);
    }
}
