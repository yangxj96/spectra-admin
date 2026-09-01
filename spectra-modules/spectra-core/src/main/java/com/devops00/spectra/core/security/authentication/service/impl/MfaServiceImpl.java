/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.core.security.authentication.service.impl;

import com.devops00.spectra.common.audit.RequestCorrelationContext;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.devops00.spectra.core.security.authentication.javabean.entity.MfaEnrollment;
import com.devops00.spectra.core.security.authentication.javabean.enums.MfaEnrollmentState;
import com.devops00.spectra.core.security.authentication.javabean.entity.RecoveryCode;
import com.devops00.spectra.core.security.authentication.javabean.entity.TotpCredential;
import com.devops00.spectra.core.security.authentication.javabean.vo.MfaEnrollmentResult;
import com.devops00.spectra.core.security.authentication.javabean.vo.MfaStatusVO;
import com.devops00.spectra.core.security.authentication.mapper.MfaEnrollmentMapper;
import com.devops00.spectra.core.security.authentication.mapper.RecoveryCodeMapper;
import com.devops00.spectra.core.security.authentication.mapper.TotpCredentialMapper;
import com.devops00.spectra.core.security.authentication.service.MfaService;
import com.devops00.spectra.core.security.authentication.util.TotpSecretCipher;
import com.devops00.spectra.core.security.audit.outbox.SecurityChangeOutboxProducer;
import com.devops00.spectra.core.system.constant.SystemConfigKeys;
import com.devops00.spectra.core.system.service.ConfiguredService;
import com.devops00.spectra.core.user.javabean.entity.User;
import com.devops00.spectra.core.user.mapper.UserMapper;
import com.devops00.spectra.security.base.properties.SecurityProperties;
import com.devops00.spectra.security.base.audit.AuditResult;
import com.devops00.spectra.security.base.audit.SecurityAuditEvent;
import com.devops00.spectra.security.base.audit.SecurityAuditWriter;
import com.devops00.spectra.security.base.security.mfa.RecoveryCodeHasher;
import com.devops00.spectra.security.base.security.mfa.TotpCodeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/** 默认 PostgreSQL MFA 实现。 */
@Service
public class MfaServiceImpl implements MfaService {

    private static final String FACTOR_TOTP = "TOTP";
    private static final String PENDING = MfaEnrollmentState.PENDING.name();
    private static final String ACTIVE = MfaEnrollmentState.ACTIVE.name();

    private final MfaEnrollmentMapper enrollmentMapper;
    private final TotpCredentialMapper credentialMapper;
    private final RecoveryCodeMapper recoveryCodeMapper;
    private final UserMapper userMapper;
    private final ConfiguredService configuredService;
    private final SecurityProperties properties;
    private final SecurityAuditWriter securityAuditWriter;

    private final SecurityChangeOutboxProducer securityChangeOutboxProducer;
    private final Clock clock = Clock.systemUTC();
    private final SecureRandom random = new SecureRandom();

    public MfaServiceImpl(MfaEnrollmentMapper enrollmentMapper, TotpCredentialMapper credentialMapper,
                          RecoveryCodeMapper recoveryCodeMapper, UserMapper userMapper, ConfiguredService configuredService,
                          SecurityProperties properties,
                          SecurityAuditWriter securityAuditWriter,
                          SecurityChangeOutboxProducer securityChangeOutboxProducer) {
        this.enrollmentMapper = enrollmentMapper;
        this.credentialMapper = credentialMapper;
        this.recoveryCodeMapper = recoveryCodeMapper;
        this.userMapper = userMapper;
        this.configuredService = configuredService;
        this.properties = properties;
        this.securityAuditWriter = securityAuditWriter;
        this.securityChangeOutboxProducer = securityChangeOutboxProducer;
    }

    @Override
    @Transactional
    public MfaEnrollmentResult beginTotpEnrollment(UUID userId) {
        if (findActiveEnrollment(userId) != null) {
            throw new IllegalStateException("当前用户已启用 MFA");
        }
        MfaEnrollment enrollment = new MfaEnrollment();
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
        String account = resolveAccount(userId);
        String issuerName = configuredService.findValue(SystemConfigKeys.SYSTEM_NAME)
                .orElse(properties.getMfaTotpIssuer());
        String issuer = URLEncoder.encode(issuerName, StandardCharsets.UTF_8);
        String label = URLEncoder.encode(issuerName + ":" + account, StandardCharsets.UTF_8);
        String uri = "otpauth://totp/" + label + "?secret="
                + secret
                + "&issuer=" + issuer + "&algorithm=SHA1&digits=6&period=30";
        appendAudit("AUTH_CHALLENGE_CREATED", userId, Map.of("factorType", FACTOR_TOTP),
                Map.of("state", PENDING), "TOTP 登记开始");
        return new MfaEnrollmentResult(enrollment.getId(), uri, secret);
    }

    @Override
    public MfaStatusVO status(UUID userId) {
        MfaEnrollment enrollment = findActiveEnrollment(userId);
        return new MfaStatusVO(enrollment != null, enrollment == null ? null : enrollment.getFactorType());
    }

    /**
     * 转换、解析或规范化数据（{@code resolveAccount}）。
     */
    private String resolveAccount(UUID userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new IllegalStateException("MFA 登记用户不存在");
        }
        String account = user.getUsername();
        if (account == null || account.isBlank()) {
            throw new IllegalStateException("MFA 登记用户登录账号不存在");
        }
        return account.trim();
    }

    @Override
    @Transactional
    public List<String> confirmTotpEnrollment(UUID userId, UUID enrollmentId, String code) {
        MfaEnrollment enrollment = findEnrollment(userId, enrollmentId, PENDING);
        String secret = decryptSecret(enrollment).secret();
        if (!TotpCodeService.matches(secret, code, clock, 1)) {
            appendAudit("AUTH_CHALLENGE_FAILED", userId, Map.of("factorType", FACTOR_TOTP),
                    Map.of("state", PENDING), "TOTP 登记验证码错误");
            throw new IllegalArgumentException("TOTP 验证码错误");
        }
        enrollment.setState(ACTIVE);
        enrollment.setEnrolledAt(Instant.now(clock));
        if (enrollmentMapper.updateById(enrollment) != 1) {
            throw new IllegalStateException("激活 MFA 登记失败");
        }

        List<String> recoveryCodes = createRecoveryCodes(enrollmentId);
        appendAudit("AUTH_CHALLENGE_SUCCEEDED", userId, Map.of("factorType", FACTOR_TOTP, "state", PENDING),
                Map.of("factorType", FACTOR_TOTP, "state", ACTIVE), "TOTP 登记确认");
        appendAudit("MFA_FACTOR_ENROLLED", userId, Map.of(), Map.of("factorType", FACTOR_TOTP), null);
        return List.copyOf(recoveryCodes);
    }

    @Override
    @Transactional
    public boolean verifyTotp(UUID userId, String code) {
        MfaEnrollment enrollment = findActiveEnrollment(userId);
        DecryptedSecret decrypted = enrollment == null ? null : decryptSecret(enrollment);
        boolean verified = decrypted != null && TotpCodeService.matches(decrypted.secret(), code, clock, 1);
        if (verified) {
            migrateSecretIfRequired(enrollment, decrypted);
        }
        appendAudit(verified ? "MFA_FACTOR_VERIFIED" : "AUTH_CHALLENGE_FAILED", userId,
                Map.of("factorType", FACTOR_TOTP), Map.of("verified", verified), "TOTP 校验");
        return verified;
    }

    @Override
    @Transactional
    public boolean consumeRecoveryCode(UUID userId, String code) {
        MfaEnrollment enrollment = findActiveEnrollment(userId);
        if (enrollment == null || code == null || code.isBlank()) {
            appendAudit("AUTH_CHALLENGE_FAILED", userId, Map.of("factorType", "RECOVERY_CODE"),
                    Map.of("verified", false), "Recovery Code 校验失败");
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
                if (updated == 1) {
                    appendAudit("MFA_RECOVERY_CODE_USED", userId, Map.of(), Map.of("factorType", "RECOVERY_CODE"), null);
                }
                return updated == 1;
            }
        }
        boolean replayed = recoveryCodeMapper.selectList(new LambdaQueryWrapper<RecoveryCode>()
                .eq(RecoveryCode::getEnrollmentId, enrollment.getId())
                .isNotNull(RecoveryCode::getUsedAt))
                .stream()
                .anyMatch(recoveryCode -> RecoveryCodeHasher.matches(code, recoveryCode.getCodeHash()));
        appendAudit(replayed ? "MFA_RECOVERY_CODE_REPLAYED" : "AUTH_CHALLENGE_FAILED", userId,
                Map.of("factorType", "RECOVERY_CODE"), Map.of("verified", false),
                replayed ? "Recovery Code 重放" : "Recovery Code 校验失败");
        return false;
    }

    @Override
    @Transactional
    public List<String> rotateRecoveryCodes(UUID userId) {
        MfaEnrollment enrollment = findActiveEnrollment(userId);
        if (enrollment == null) {
            throw new IllegalStateException("当前用户没有激活的 TOTP MFA");
        }
        recoveryCodeMapper.update(null, new LambdaUpdateWrapper<RecoveryCode>()
                .eq(RecoveryCode::getEnrollmentId, enrollment.getId())
                .isNull(RecoveryCode::getUsedAt)
                .set(RecoveryCode::getUsedAt, Instant.now(clock))
                .setSql("version = version + 1"));
        List<String> recoveryCodes = createRecoveryCodes(enrollment.getId());
        appendAudit("MFA_RECOVERY_CODES_ROTATED", userId, Map.of(),
                Map.of("factorType", "RECOVERY_CODE", "count", recoveryCodes.size()), "Recovery Code 轮换");
        return List.copyOf(recoveryCodes);
    }

    @Override
    @Transactional
    public void disableTotp(UUID userId, String code) {
        MfaEnrollment enrollment = findActiveEnrollment(userId);
        if (enrollment == null) {
            throw new IllegalStateException("当前用户没有启用 MFA");
        }
        boolean verified = verifyTotp(userId, code) || consumeRecoveryCode(userId, code);
        if (!verified) {
            throw new IllegalArgumentException("MFA 验证码或恢复码错误");
        }
        enrollment.setState(MfaEnrollmentState.REVOKED.name());
        enrollment.setRevokedAt(Instant.now(clock));
        if (enrollmentMapper.updateById(enrollment) != 1) {
            throw new IllegalStateException("停用 MFA 失败");
        }
        appendAudit("MFA_FACTOR_REVOKED", userId, Map.of("factorType", FACTOR_TOTP),
                Map.of("factorType", FACTOR_TOTP, "state", MfaEnrollmentState.REVOKED.name()), "用户停用 MFA");
    }

    /**
     * 创建或构建目标数据（{@code createRecoveryCodes}）。
     */
    private List<String> createRecoveryCodes(UUID enrollmentId) {
        List<String> recoveryCodes = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            String recoveryCode = generateRecoveryCode();
            RecoveryCode entity = new RecoveryCode();
            entity.setEnrollmentId(enrollmentId);
            entity.setCodeHash(RecoveryCodeHasher.hash(recoveryCode));
            entity.setVersion(0L);
            if (recoveryCodeMapper.insert(entity) != 1) {
                throw new IllegalStateException("保存 Recovery Code 失败");
            }
            recoveryCodes.add(recoveryCode);
        }
        return recoveryCodes;
    }

    @Override
    public boolean hasActiveTotp(UUID userId) {
        return findActiveEnrollment(userId) != null;
    }

    @Override
    public boolean hasAnyTotpEnrollment(UUID userId) {
        return enrollmentMapper.selectCount(new LambdaQueryWrapper<MfaEnrollment>()
                .eq(MfaEnrollment::getUserId, userId)
                .eq(MfaEnrollment::getFactorType, FACTOR_TOTP)) > 0;
    }

    @Override
    public boolean hasNonRevokedTotpEnrollment(UUID userId) {
        return enrollmentMapper.selectCount(new LambdaQueryWrapper<MfaEnrollment>()
                .eq(MfaEnrollment::getUserId, userId)
                .eq(MfaEnrollment::getFactorType, FACTOR_TOTP)
                .in(MfaEnrollment::getState, PENDING, ACTIVE)) > 0;
    }

    /**
     * 更新或推进目标状态（{@code appendAudit}）。
     */
    private void appendAudit(String eventType, UUID targetId, Map<String, Object> before,
                             Map<String, Object> after, String reason) {
        var event = new SecurityAuditEvent(null, eventType, targetId, targetId,
                null, null, null, before, after, reason, null, auditResult(eventType),
                RequestCorrelationContext.current().correlationId());
        securityAuditWriter.append(event);
        securityChangeOutboxProducer.publish(event);
    }

    /**
     * 处理内部业务逻辑（{@code auditResult}）。
     */
    private AuditResult auditResult(String eventType) {
        return switch (eventType) {
            case "AUTH_CHALLENGE_FAILED", "MFA_RECOVERY_CODE_REPLAYED" -> AuditResult.FAILED;
            default -> AuditResult.SUCCEEDED;
        };
    }

    /**
     * 查询或获取目标数据（{@code findActiveEnrollment}）。
     */
    private MfaEnrollment findActiveEnrollment(UUID userId) {
        return enrollmentMapper.selectOne(new LambdaQueryWrapper<MfaEnrollment>()
                .eq(MfaEnrollment::getUserId, userId)
                .eq(MfaEnrollment::getFactorType, FACTOR_TOTP)
                .eq(MfaEnrollment::getState, ACTIVE)
                .last("LIMIT 1"));
    }

    /**
     * 查询或获取目标数据（{@code findEnrollment}）。
     */
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

    /**
     * 执行加密或解密处理（{@code decryptSecret}）。
     */
    private DecryptedSecret decryptSecret(MfaEnrollment enrollment) {
        TotpCredential credential = credentialMapper.selectOne(new LambdaQueryWrapper<TotpCredential>()
                .eq(TotpCredential::getEnrollmentId, enrollment.getId()));
        if (credential == null) {
            throw new IllegalStateException("MFA 密钥不存在");
        }
        TotpSecretCipher cipher = new TotpSecretCipher(properties);
        return new DecryptedSecret(credential, cipher.decrypt(credential.getKeyVersion(), credential.getEncryptedSecret()), cipher);
    }

    /**
     * 处理内部业务逻辑（{@code migrateSecretIfRequired}）。
     */
    private void migrateSecretIfRequired(MfaEnrollment enrollment, DecryptedSecret decrypted) {
        if (decrypted.cipher().isCurrentVersion(decrypted.credential().getKeyVersion())) {
            return;
        }
        TotpSecretCipher.EncryptedSecret encrypted = decrypted.cipher()
                .reencrypt(decrypted.credential().getKeyVersion(), decrypted.credential().getEncryptedSecret());
        int updated = credentialMapper.update(null, new LambdaUpdateWrapper<TotpCredential>()
                .eq(TotpCredential::getEnrollmentId, enrollment.getId())
                .eq(TotpCredential::getKeyVersion, decrypted.credential().getKeyVersion())
                .set(TotpCredential::getEncryptedSecret, encrypted.combined())
                .set(TotpCredential::getKeyVersion, encrypted.keyVersion()));
        if (updated == 1) {
            appendAudit("MFA_FACTOR_REKEYED", enrollment.getUserId(),
                    Map.of("keyVersion", decrypted.credential().getKeyVersion()),
                    Map.of("keyVersion", encrypted.keyVersion()), "TOTP 密钥轮换迁移");
        }
    }

    /**
     * 创建或构建目标数据（{@code generateRecoveryCode}）。
     */
    private String generateRecoveryCode() {
        byte[] bytes = new byte[8];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes).toUpperCase(Locale.ROOT);
    }

    private record DecryptedSecret(TotpCredential credential, String secret, TotpSecretCipher cipher) {
    }
}
