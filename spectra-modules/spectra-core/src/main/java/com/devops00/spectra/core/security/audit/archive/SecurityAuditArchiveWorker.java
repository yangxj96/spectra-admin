/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.core.security.audit.archive;

import com.devops00.spectra.common.port.audit.SecurityAuditArchiveBackend;
import com.devops00.spectra.common.port.audit.SecurityAuditArchiveIntegrity;
import com.devops00.spectra.common.port.audit.SecurityAuditArchiveReceipt;
import com.devops00.spectra.common.scheduler.ScheduledEffectType;
import com.devops00.spectra.common.scheduler.ScheduledJobDescriptor;
import com.devops00.spectra.common.scheduler.ScheduledJobType;
import com.devops00.spectra.common.scheduler.ScheduledLoopContext;
import com.devops00.spectra.common.scheduler.ScheduledLoopCycleResult;
import com.devops00.spectra.common.scheduler.ScheduledLoopHandler;
import com.devops00.spectra.common.scheduler.ScheduledRunScope;
import com.devops00.spectra.common.scheduler.ScheduledScheduleKind;
import com.devops00.spectra.core.security.audit.service.SecurityAuditArchiveAuditTrail;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

/** 安全审计归档 manifest 的租约 worker。外部对象存储调用不持有数据库事务。 */
@Slf4j
@Component
public class SecurityAuditArchiveWorker implements ScheduledLoopHandler {

    private static final int BATCH_SIZE = 20;
    private static final int MAX_ATTEMPTS = 10;
    private static final Duration LEASE_DURATION = Duration.ofSeconds(60);
    private static final Duration LEASE_RENEWAL_INTERVAL = Duration.ofSeconds(20);
    private static final long MAX_BACKOFF_SECONDS = 900;

    private static final ScheduledJobDescriptor DESCRIPTOR = ScheduledJobDescriptor.builder()
            .jobKey("security.security-audit.archive")
            .handlerKey("security.security-audit.archive")
            .name("安全审计归档")
            .module("security")
            .jobType(ScheduledJobType.LOOP)
            .runScope(ScheduledRunScope.SINGLETON)
            .scheduleKind(ScheduledScheduleKind.FIXED_DELAY)
            .effectType(ScheduledEffectType.EXTERNAL_IDEMPOTENT)
            .parameterSchema(Map.of())
            .supportedActions(Set.of("VIEW", "START", "DRAIN_STOP"))
            .executionPolicy(Map.of(
                    "heartbeatIntervalMs", 5000L,
                    "leaseDurationMs", LEASE_DURATION.toMillis(),
                    "errorLogIntervalMs", 60000L,
                    "batchSize", BATCH_SIZE,
                    "maxAttempts", MAX_ATTEMPTS))
            .build();

    private final SecurityAuditArchiveManifestRepository manifestRepository;
    private final SecurityAuditArchiveDataRepository dataRepository;
    private final ObjectProvider<SecurityAuditArchiveBackend> backendProvider;
    private final SecurityAuditArchiveAuditTrail auditTrail;
    private final TransactionTemplate transactionTemplate;
    private final Clock clock;
    private final String defaultOwner;
    private final AtomicLong pendingGauge = new AtomicLong();
    private final AtomicLong oldestPendingAgeSeconds = new AtomicLong();
    private final Counter completedCounter;
    private final Counter failedCounter;
    private final Counter verificationFailedCounter;
    private final Counter restoredCounter;
    private final Timer archiveLatency;
    private final ScheduledExecutorService leaseRenewalExecutor = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "security-audit-archive-lease-renewer");
        thread.setDaemon(true);
        return thread;
    });

    @Autowired
    public SecurityAuditArchiveWorker(SecurityAuditArchiveManifestRepository manifestRepository,
                                      SecurityAuditArchiveDataRepository dataRepository,
                                      ObjectProvider<SecurityAuditArchiveBackend> backendProvider,
                                      SecurityAuditArchiveAuditTrail auditTrail,
                                      PlatformTransactionManager transactionManager,
                                      MeterRegistry meterRegistry) {
        this(manifestRepository, dataRepository, backendProvider, auditTrail, new TransactionTemplate(transactionManager),
                meterRegistry, Clock.systemUTC(), "security-audit-archive");
    }

    SecurityAuditArchiveWorker(SecurityAuditArchiveManifestRepository manifestRepository,
                               SecurityAuditArchiveDataRepository dataRepository,
                               ObjectProvider<SecurityAuditArchiveBackend> backendProvider,
                               SecurityAuditArchiveAuditTrail auditTrail,
                               TransactionTemplate transactionTemplate,
                               MeterRegistry meterRegistry,
                               Clock clock,
                               String defaultOwner) {
        this.manifestRepository = manifestRepository;
        this.dataRepository = dataRepository;
        this.backendProvider = backendProvider;
        this.auditTrail = auditTrail;
        this.transactionTemplate = transactionTemplate;
        this.clock = clock;
        this.defaultOwner = defaultOwner;
        Gauge.builder("security_audit_archive_pending", pendingGauge, AtomicLong::doubleValue)
                .description("尚未完成安全审计归档或恢复校验的 manifest 数量")
                .register(meterRegistry);
        Gauge.builder("security_audit_archive_oldest_pending_age_seconds", oldestPendingAgeSeconds,
                AtomicLong::doubleValue)
                .description("安全审计归档最老待处理 manifest 年龄")
                .register(meterRegistry);
        this.completedCounter = Counter.builder("security_audit_archive_completed_total")
                .description("安全审计归档对象成功写入数量")
                .register(meterRegistry);
        this.failedCounter = Counter.builder("security_audit_archive_failed_total")
                .description("安全审计归档或恢复处理失败次数")
                .register(meterRegistry);
        this.verificationFailedCounter = Counter.builder("security_audit_archive_verification_failed_total")
                .description("安全审计归档完整性或范围校验失败次数")
                .register(meterRegistry);
        this.restoredCounter = Counter.builder("security_audit_archive_restored_total")
                .description("安全审计归档恢复校验成功数量")
                .register(meterRegistry);
        this.archiveLatency = Timer.builder("security_audit_archive_latency")
                .description("安全审计归档从计划创建到对象写入的耗时")
                .register(meterRegistry);
    }

    @Override
    public ScheduledJobDescriptor descriptor() {
        return DESCRIPTOR;
    }

    @PreDestroy
    void shutdownLeaseRenewal() {
        leaseRenewalExecutor.shutdownNow();
    }

    /** 直接执行一批归档或恢复校验，供测试和统一调度器调用。 */
    public BatchResult processBatch() {
        return processBatch(defaultOwner, BATCH_SIZE);
    }

    @Override
    public ScheduledLoopCycleResult runCycle(ScheduledLoopContext context) {
        BatchResult result = processBatch(owner(context), BATCH_SIZE);
        boolean failed = result.failed() > 0;
        return ScheduledLoopCycleResult.builder()
                .processed(result.processed())
                .failed(result.failed())
                .errorCode(failed ? "SECURITY_AUDIT_ARCHIVE_FAILURE" : null)
                .sanitizedMessage(failed ? "安全审计归档存在处理失败事件" : null)
                .context(Map.of(
                        "claimed", result.claimed(),
                        "pending", result.pending()))
                .build();
    }

    BatchResult processBatch(String owner, int batchSize) {
        if (owner == null || owner.isBlank()) {
            throw new IllegalArgumentException("安全审计归档 worker owner 不能为空");
        }
        if (batchSize <= 0) {
            throw new IllegalArgumentException("安全审计归档 batchSize 必须大于 0");
        }
        Instant now = clock.instant();
        var manifests = transactionTemplate.execute(status -> manifestRepository.claimBatch(
                owner, now, now.plus(LEASE_DURATION), batchSize, MAX_ATTEMPTS));
        int claimed = manifests == null ? 0 : manifests.size();
        int processed = 0;
        int failed = 0;
        if (manifests != null) {
            for (var manifest : manifests) {
                try {
                    processOne(manifest, owner, null);
                    processed++;
                } catch (RuntimeException exception) {
                    failed++;
                    failedCounter.increment();
                    if (isVerificationState(manifest.state())) {
                        verificationFailedCounter.increment();
                    }
                    recordFailure(manifest, owner, exception);
                }
            }
        }
        return metrics(claimed, processed, failed);
    }

    /** 对指定 ARCHIVED/RESTORE_PENDING manifest 立即执行一次校验。 */
    public SecurityAuditArchiveOrchestrator.ManifestView verifyNow(UUID manifestId, UUID operatorId) {
        if (manifestId == null) {
            throw new IllegalArgumentException("manifestId 不能为空");
        }
        String owner = defaultOwner + ":manual:" + UUID.randomUUID();
        Instant now = clock.instant();
        var claimed = transactionTemplate.execute(status -> manifestRepository.claimOne(
                manifestId, owner, now, now.plus(LEASE_DURATION), MAX_ATTEMPTS));
        if (claimed == null) {
            throw new IllegalStateException("归档计划不存在、状态不允许校验或当前仍被其他 worker 占用");
        }
        try {
            processOne(claimed, owner, operatorId);
        } catch (RuntimeException exception) {
            failedCounter.increment();
            verificationFailedCounter.increment();
            recordFailure(claimed, owner, exception);
            throw exception;
        }
        var result = manifestRepository.find(manifestId);
        if (result == null) {
            throw new IllegalStateException("归档校验完成后 manifest 不存在");
        }
        return toView(result);
    }

    private void processOne(SecurityAuditArchiveManifestRepository.ArchiveManifest manifest,
                            String owner,
                            UUID operatorOverride) {
        ScheduledFuture<?> renewal = leaseRenewalExecutor.scheduleAtFixedRate(
                () -> renewLease(manifest, owner),
                LEASE_RENEWAL_INTERVAL.toMillis(),
                LEASE_RENEWAL_INTERVAL.toMillis(),
                java.util.concurrent.TimeUnit.MILLISECONDS);
        try {
            SecurityAuditArchiveBackend backend = backendProvider.getIfAvailable();
            if (backend == null) {
                throw new IllegalStateException("安全审计归档 backend 未启用");
            }
            assertBackendPolicy(backend);
            if ("PLANNED".equals(manifest.state()) || "FAILED".equals(manifest.state())) {
                archive(manifest, owner, backend, operatorOverride);
                return;
            }
            if (isVerificationState(manifest.state())) {
                verify(manifest, owner, backend, operatorOverride);
                return;
            }
            throw new IllegalStateException("安全审计归档状态不可处理: " + manifest.state());
        } finally {
            renewal.cancel(false);
        }
    }

    private void renewLease(SecurityAuditArchiveManifestRepository.ArchiveManifest manifest, String owner) {
        try {
            Instant now = clock.instant();
            if (manifestRepository.renewLease(manifest.manifestId(), owner, now.plus(LEASE_DURATION), now) != 1) {
                log.warn("安全审计归档租约续期未生效: manifestId={}", manifest.manifestId());
            }
        } catch (RuntimeException exception) {
            log.warn("安全审计归档租约续期失败: manifestId={}, error={}", manifest.manifestId(),
                    errorMessage(exception));
        }
    }

    private void archive(SecurityAuditArchiveManifestRepository.ArchiveManifest manifest,
                         String owner,
                         SecurityAuditArchiveBackend backend,
                         UUID operatorOverride) {
        UUID operatorId = operatorOverride == null ? operatorId(manifest) : operatorOverride;
        auditTrail.append("SECURITY_AUDIT_ARCHIVE_STARTED", operatorId, manifest.partitionName(),
                "manifestId=" + manifest.manifestId());
        var snapshot = dataRepository.snapshot(manifest.partitionName(), manifest.rangeStart(), manifest.rangeEnd());
        byte[] content = snapshot.content();
        String digest = SecurityAuditArchiveIntegrity.sha256(content);
        Instant now = clock.instant();
        Instant retainUntil = retentionDeadline(manifest.rangeEnd(), now,
                manifestRepository.totalRetentionYears());
        String objectKey = manifest.partitionName() + "/" + manifest.manifestId() + ".jsonl";
        SecurityAuditArchiveReceipt receipt = backend.put(objectKey, content, retainUntil);
        if (!sameDigest(digest, receipt.contentSha256()) || content.length != receipt.contentLength()) {
            throw new IllegalStateException("归档 backend 返回的完整性回执不匹配");
        }
        transactionTemplate.executeWithoutResult(status -> {
            if (manifestRepository.markArchived(manifest.manifestId(), owner, receipt.objectUri(), digest,
                    content.length, snapshot.rowCount(), now, operatorId, clock.instant()) != 1) {
                throw new DataAccessException("安全审计归档租约已失效，未确认对象写入") {
                };
            }
            auditTrail.append("SECURITY_AUDIT_ARCHIVE_COMPLETED", operatorId, manifest.partitionName(),
                    "manifestId=" + manifest.manifestId() + ";rows=" + snapshot.rowCount());
        });
        archiveLatency.record(Duration.between(manifest.createdAt(), now));
        completedCounter.increment();
    }

    private void verify(SecurityAuditArchiveManifestRepository.ArchiveManifest manifest,
                        String owner,
                        SecurityAuditArchiveBackend backend,
                        UUID operatorOverride) {
        UUID operatorId = operatorOverride == null ? operatorId(manifest) : operatorOverride;
        if (manifest.objectUri() == null
                || manifest.objectUri().isBlank()
                || manifest.contentSha256() == null
                || manifest.rowCount() == null) {
            throw new IllegalStateException("归档 manifest 缺少对象完整性元数据");
        }
        if (!backend.exists(manifest.objectUri())) {
            throw new IllegalStateException("归档对象不存在");
        }
        if (manifest.contentLength() == null) {
            byte[] content = backend.read(manifest.objectUri());
            SecurityAuditArchiveIntegrity.verify(content, manifest.contentSha256());
        } else {
            backend.verify(manifest.objectUri(), manifest.contentSha256(), manifest.contentLength());
        }
        var source = dataRepository.snapshot(manifest.partitionName(), manifest.rangeStart(), manifest.rangeEnd());
        String sourceDigest = SecurityAuditArchiveIntegrity.sha256(source.content());
        if (source.rowCount() != manifest.rowCount()
                || !sameDigest(sourceDigest, manifest.contentSha256())
                || (manifest.contentLength() != null && source.content().length != manifest.contentLength())) {
            throw new IllegalStateException("安全审计归档源范围、行数或摘要校验失败");
        }
        transactionTemplate.executeWithoutResult(status -> {
            int updated = "RESTORE_PENDING".equals(manifest.state())
                    ? manifestRepository.markRestored(manifest.manifestId(), owner, operatorId, clock.instant())
                    : manifestRepository.markVerified(manifest.manifestId(), owner, operatorId, clock.instant());
            if (updated != 1) {
                throw new DataAccessException("安全审计归档状态已变化，未确认校验结果") {
                };
            }
            auditTrail.append("RESTORE_PENDING".equals(manifest.state())
                    ? "SECURITY_AUDIT_ARCHIVE_RESTORED"
                    : "SECURITY_AUDIT_ARCHIVE_VERIFIED",
                    operatorId, manifest.partitionName(),
                    "manifestId=" + manifest.manifestId() + ";rows=" + source.rowCount());
        });
        if ("RESTORE_PENDING".equals(manifest.state())) {
            restoredCounter.increment();
        }
    }

    private void recordFailure(SecurityAuditArchiveManifestRepository.ArchiveManifest manifest,
                               String owner,
                               RuntimeException exception) {
        Instant now = clock.instant();
        String error = errorMessage(exception);
        Instant nextAvailableAt = now.plusSeconds(backoffSeconds(manifest.attempts()));
        try {
            transactionTemplate.executeWithoutResult(status -> {
                if (manifestRepository.markFailed(manifest.manifestId(), owner, operatorId(manifest), now,
                        nextAvailableAt, error) != 1) {
                    log.warn("安全审计归档失败状态推进未生效: manifestId={}, error={}", manifest.manifestId(), error);
                }
                auditTrail.append("SECURITY_AUDIT_ARCHIVE_FAILED", operatorId(manifest), manifest.partitionName(),
                        "manifestId=" + manifest.manifestId() + ";error=" + error);
            });
        } catch (RuntimeException markException) {
            // 状态推进失败时保留租约，待 lease_until 到期后重新领取；不删除源事实或对象。
            log.error("安全审计归档失败状态无法落库: manifestId={}", manifest.manifestId(), markException);
        }
    }

    private BatchResult metrics(int claimed, int processed, int failed) {
        long pending = manifestRepository.pendingCount();
        pendingGauge.set(pending);
        Instant oldest = manifestRepository.oldestPendingAt();
        oldestPendingAgeSeconds.set(oldest == null
                ? 0L
                : Math.max(0L, Duration.between(oldest, clock.instant()).toSeconds()));
        return new BatchResult(claimed, processed, failed, pending);
    }

    private static Instant retentionDeadline(Instant rangeEnd, Instant now, int totalRetentionYears) {
        Instant deadline = rangeEnd.atZone(ZoneOffset.UTC).plusYears(totalRetentionYears).toInstant();
        return deadline.isAfter(now) ? deadline : now.plus(Duration.ofDays(1));
    }

    private void assertBackendPolicy(SecurityAuditArchiveBackend backend) {
        String configuredBackend = manifestRepository.archiveBackend();
        if ("PENDING".equalsIgnoreCase(configuredBackend)) {
            throw new IllegalStateException("安全审计归档后端策略仍为 PENDING");
        }
        if (!configuredBackend.equals(backend.id())) {
            throw new IllegalStateException("安全审计归档 backend 与保留策略不一致");
        }
    }

    private static boolean isVerificationState(String state) {
        return "ARCHIVED".equals(state) || "RESTORE_PENDING".equals(state);
    }

    private static UUID operatorId(SecurityAuditArchiveManifestRepository.ArchiveManifest manifest) {
        return manifest.updatedBy() == null ? manifest.createdBy() : manifest.updatedBy();
    }

    private static long backoffSeconds(int attempts) {
        int exponent = Math.max(0, Math.min(attempts - 1, 10));
        long base = 1L << exponent;
        long jitter = ThreadLocalRandom.current().nextLong(0, Math.max(1L, base / 4));
        return Math.min(MAX_BACKOFF_SECONDS, base + jitter);
    }

    private static boolean sameDigest(String expected, String actual) {
        return actual != null
                && MessageDigest.isEqual(expected.getBytes(StandardCharsets.US_ASCII),
                        actual.getBytes(StandardCharsets.US_ASCII));
    }

    private static String errorMessage(Throwable exception) {
        String type = exception.getClass().getSimpleName();
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return type;
        }
        String normalized = message.replaceAll("[\\r\\n\\t]+", " ").trim();
        String combined = type + ": " + normalized;
        return combined.length() <= 2000 ? combined : combined.substring(0, 2000);
    }

    private static String owner(ScheduledLoopContext context) {
        if (context == null || context.instanceId() == null || context.runtimeId() == null) {
            return "security-audit-archive";
        }
        return context.instanceId() + ":" + context.runtimeId();
    }

    private static SecurityAuditArchiveOrchestrator.ManifestView toView(
                                                                        SecurityAuditArchiveManifestRepository.ArchiveManifest manifest) {
        return new SecurityAuditArchiveOrchestrator.ManifestView(manifest.manifestId(), manifest.partitionName(),
                manifest.rangeStart(), manifest.rangeEnd(),
                manifest.objectUri(), manifest.contentSha256(), manifest.contentLength(), manifest.rowCount(), manifest.state(),
                manifest.archivedAt(), manifest.verifiedAt(), manifest.lastError(), manifest.attempts(), manifest.availableAt());
    }

    /** 一批安全审计归档处理统计。 */
    public record BatchResult(int claimed, int processed, int failed, long pending) {

        public BatchResult {
            if (claimed < 0 || processed < 0 || failed < 0 || pending < 0) {
                throw new IllegalArgumentException("安全审计归档统计不能为负数");
            }
        }
    }

}
