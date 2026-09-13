/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.devops00.spectra.core.system.service.impl;

import com.devops00.spectra.common.port.security.SecurityReplayNonceAdminPort;
import com.devops00.spectra.common.port.security.SecuritySessionQueryPort;
import com.devops00.spectra.common.port.security.SecuritySessionRevocationPort;
import com.devops00.spectra.common.port.security.SecurityVerificationAttemptStore;
import com.devops00.spectra.common.port.security.SecurityVerificationCodeStore;
import com.devops00.spectra.core.common.constant.RedisCacheKey;
import com.devops00.spectra.core.system.cache.CacheInvalidationCoordinator;
import com.devops00.spectra.core.system.cache.CacheManagementResult;
import com.devops00.spectra.core.system.cache.CacheRegionDescriptor;
import com.devops00.spectra.core.system.cache.CacheRegionRegistry;
import com.devops00.spectra.core.system.javabean.from.CacheBusinessClearFrom;
import com.devops00.spectra.core.system.javabean.from.CacheBusinessPreviewFrom;
import com.devops00.spectra.core.system.javabean.from.SecurityLoginFailureClearFrom;
import com.devops00.spectra.core.system.javabean.from.SecurityNonceGlobalInvalidateFrom;
import com.devops00.spectra.core.system.javabean.from.SecurityNonceInvalidateFrom;
import com.devops00.spectra.core.system.javabean.from.SecuritySessionRevokeAllFrom;
import com.devops00.spectra.core.system.javabean.from.SecuritySessionRevokeFrom;
import com.devops00.spectra.core.system.javabean.from.SecurityVerificationClearFrom;
import com.devops00.spectra.core.system.javabean.from.SecurityVerificationType;
import com.devops00.spectra.core.system.javabean.vo.CacheMonitorOverviewVO;
import com.devops00.spectra.core.system.javabean.vo.CacheOperationVO;
import com.devops00.spectra.core.system.javabean.vo.CacheRegionVO;
import com.devops00.spectra.core.system.javabean.vo.SecurityRuntimeVO;
import com.devops00.spectra.core.system.service.CacheManagementService;
import com.devops00.spectra.framework.security.session.lifecycle.SecurityLoginFailureTracker;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * 缓存管理应用服务实现；普通缓存和安全状态始终走不同的端口。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@Service
public class CacheManagementServiceImpl implements CacheManagementService {

    private static final String CACHE_CONFIRMATION = "CLEAR-CACHE";
    private static final String NONCE_CONFIRMATION = "INVALIDATE-NONCES";

    private final CacheRegionRegistry registry;
    private final CacheInvalidationCoordinator coordinator;
    private final SecuritySessionQueryPort sessionQuery;
    private final SecuritySessionRevocationPort sessionRevocation;
    private final SecurityVerificationCodeStore verificationCodes;
    private final SecurityVerificationAttemptStore verificationAttempts;
    private final SecurityLoginFailureTracker loginFailureTracker;
    private final SecurityReplayNonceAdminPort nonceAdmin;

    public CacheManagementServiceImpl(CacheRegionRegistry registry,
                                      CacheInvalidationCoordinator coordinator,
                                      SecuritySessionQueryPort sessionQuery,
                                      SecuritySessionRevocationPort sessionRevocation,
                                      SecurityVerificationCodeStore verificationCodes,
                                      SecurityVerificationAttemptStore verificationAttempts,
                                      SecurityLoginFailureTracker loginFailureTracker,
                                      SecurityReplayNonceAdminPort nonceAdmin) {
        this.registry = registry;
        this.coordinator = coordinator;
        this.sessionQuery = sessionQuery;
        this.sessionRevocation = sessionRevocation;
        this.verificationCodes = verificationCodes;
        this.verificationAttempts = verificationAttempts;
        this.loginFailureTracker = loginFailureTracker;
        this.nonceAdmin = nonceAdmin;
    }

    @Override
    public CacheMonitorOverviewVO getOverview() {
        List<CacheRegionDescriptor> regions = registry.list();
        SecurityRuntimeVO security = getSecurity();
        String status = "AVAILABLE".equals(security.status()) ? "AVAILABLE" : security.status();
        return new CacheMonitorOverviewVO(status, Instant.now(), regions.size(), security.onlineSessionCount(),
                "AVAILABLE".equals(security.status()) ? 0 : 1, security.status());
    }

    @Override
    public List<CacheRegionVO> getRegions() {
        return registry.list().stream().map(this::toRegion).toList();
    }

    @Override
    public SecurityRuntimeVO getSecurity() {
        try {
            Long onlineCount = (long) sessionQuery.listOnlineUsers().size();
            SecurityReplayNonceAdminPort.Summary nonce = nonceAdmin.summary();
            return new SecurityRuntimeVO("AVAILABLE", onlineCount, "AVAILABLE", "AVAILABLE", "AVAILABLE",
                    nonce.status(), nonce.cutoffEpochSecond(), "AVAILABLE");
        } catch (RuntimeException exception) {
            return new SecurityRuntimeVO("UNAVAILABLE", null, "UNAVAILABLE", "UNAVAILABLE", "UNAVAILABLE",
                    "UNAVAILABLE", null, "UNAVAILABLE");
        }
    }

    @Override
    public CacheOperationVO getOperation(UUID operationId) {
        requireOperationId(operationId);
        CacheManagementResult result = coordinator.status(operationId);
        if (result == null) {
            return new CacheOperationVO(operationId, "UNKNOWN", "UNKNOWN", 0L, false,
                    "当前实例没有该操作的完成回执", null);
        }
        return toOperation(result);
    }

    @Override
    public CacheOperationVO previewBusinessClear(CacheBusinessPreviewFrom from) {
        requireBusinessRequest(from);
        long count = from.allRegions() ? registry.list().size() : registry.validate(from.regionCodes()).size();
        return new CacheOperationVO(null, "BUSINESS_PREVIEW", "ACCEPTED", count, false,
                from.allInstances() ? "将清理登记区域并广播到其他实例" : "将清理当前实例的登记区域", Instant.now());
    }

    @Override
    public CacheOperationVO clearBusiness(CacheBusinessClearFrom from) {
        requireReason(from.reason());
        requireConfirmation(from.confirmation(), CACHE_CONFIRMATION);
        requireBusinessRequest(new CacheBusinessPreviewFrom(from.regionCodes(), from.allRegions(), from.allInstances()));
        UUID operationId = from.operationId() == null ? UUID.randomUUID() : from.operationId();
        CacheManagementResult result = coordinator.clear(from.regionCodes(), from.allRegions(), from.allInstances(),
                operationId);
        return toOperation(result);
    }

    @Override
    public CacheOperationVO revokeSession(SecuritySessionRevokeFrom from) {
        requireSecurityConfirmation(from.reason(), from.confirmed());
        if (from.userId() == null || from.clientType() == null) {
            throw new IllegalArgumentException("用户和客户端不能为空");
        }
        sessionRevocation.revokeUserClientSessions(from.userId(), from.clientType());
        return completed("SESSION_CLIENT", 1L, "已通过安全 Session 用例撤销目标客户端会话");
    }

    @Override
    public CacheOperationVO revokeAllSessions(SecuritySessionRevokeAllFrom from) {
        requireSecurityConfirmation(from.reason(), from.confirmed());
        if (from.userId() == null) {
            throw new IllegalArgumentException("用户不能为空");
        }
        sessionRevocation.revokeUserSessions(from.userId());
        return completed("SESSION_ALL", 1L, "已通过安全 Session 用例撤销用户全部会话");
    }

    @Override
    public CacheOperationVO clearVerification(SecurityVerificationClearFrom from) {
        requireSecurityConfirmation(from.reason(), from.confirmed());
        if (from.type() == null) {
            throw new IllegalArgumentException("验证码类型不能为空");
        }
        String target = normalizeTarget(from.target());
        String codeKey = verificationCodeKey(from.type(), target);
        verificationCodes.delete(codeKey);
        if (from.clearAttempts() && isAttemptType(from.type())) {
            verificationAttempts.delete(verificationAttemptKey(from.type(), target));
        }
        return completed("VERIFICATION", 1L, "已清理受控验证码状态");
    }

    @Override
    public CacheOperationVO clearLoginFailure(SecurityLoginFailureClearFrom from) {
        requireSecurityConfirmation(from.reason(), from.confirmed());
        String username = normalizeTarget(from.username());
        loginFailureTracker.clearLoginFail(username);
        return completed("LOGIN_FAILURE", 1L, "已清理登录失败计数；未改变用户生命周期状态");
    }

    @Override
    public CacheOperationVO invalidateNonce(SecurityNonceInvalidateFrom from) {
        requireSecurityConfirmation(from.reason(), from.confirmed());
        SecurityReplayNonceAdminPort.Result result = nonceAdmin.invalidate(from.nonce());
        return new CacheOperationVO(null, "NONCE_TARGET", result.status(), result.affectedCount(), false,
                "已处理定向 Web 加密 nonce", Instant.now());
    }

    @Override
    public CacheOperationVO invalidateAllNonces(SecurityNonceGlobalInvalidateFrom from) {
        requireReason(from.reason());
        requireConfirmation(from.confirmationPhrase(), NONCE_CONFIRMATION);
        SecurityReplayNonceAdminPort.Result result = nonceAdmin.invalidateAll();
        return new CacheOperationVO(null, "NONCE_ALL", result.status(), result.affectedCount(), false,
                "已推进 nonce 全局失效 cutoff；当前窗口内旧加密请求将被拒绝", Instant.now());
    }

    /**
     * 转换区域。
     */
    private CacheRegionVO toRegion(CacheRegionDescriptor descriptor) {
        return new CacheRegionVO(descriptor.code(), descriptor.displayName(), descriptor.provider(), descriptor.mode(),
                descriptor.ttlSeconds(), descriptor.supportsStats(), descriptor.supportsClear(),
                registry.statistics(descriptor));
    }

    /**
     * 转换操作。
     */
    private static CacheOperationVO toOperation(CacheManagementResult result) {
        return new CacheOperationVO(result.operationId(), "BUSINESS_CLEAR", result.status(),
                result.affectedRegions(), result.broadcastAccepted(), result.message(), Instant.now());
    }

    /**
     * 处理缓存相关数据。
     */
    private static CacheOperationVO completed(String type, long count, String message) {
        return new CacheOperationVO(UUID.randomUUID(), type, "SUCCEEDED", count, false, message, Instant.now());
    }

    /**
     * 校验业务请求。
     */
    private static void requireBusinessRequest(CacheBusinessPreviewFrom from) {
        if (from == null) {
            throw new IllegalArgumentException("普通缓存请求不能为空");
        }
    }

    /**
     * 校验操作标识。
     */
    private static void requireOperationId(UUID operationId) {
        if (operationId == null) {
            throw new IllegalArgumentException("操作 ID 不能为空");
        }
    }

    /**
     * 校验原因。
     */
    private static void requireReason(String reason) {
        if (reason == null || reason.isBlank() || reason.length() > 200) {
            throw new IllegalArgumentException("操作理由不能为空且长度不能超过 200");
        }
    }

    /**
     * 校验缓存。
     */
    private static void requireConfirmation(String confirmation, String expected) {
        if (!expected.equals(confirmation)) {
            throw new IllegalArgumentException("确认短语不匹配");
        }
    }

    /**
     * 校验安全。
     */
    private static void requireSecurityConfirmation(String reason, boolean confirmed) {
        requireReason(reason);
        if (!confirmed) {
            throw new IllegalArgumentException("安全操作需要二次确认");
        }
    }

    /**
     * 规范化目标。
     */
    private static String normalizeTarget(String target) {
        if (target == null
                || target.isBlank()
                || target.length() > 128
                || !target.matches("[A-Za-z0-9@._+:-]+")) {
            throw new IllegalArgumentException("安全目标格式无效");
        }
        return target;
    }

    /**
     * 处理编码键相关数据。
     */
    private static String verificationCodeKey(SecurityVerificationType type, String target) {
        return switch (type) {
            case KAPTCHA -> RedisCacheKey.KAPTCHA + target;
            case LOGIN_SMS -> RedisCacheKey.LOGIN_SMS_CODE + target;
            case LOGIN_EMAIL -> RedisCacheKey.LOGIN_EMAIL_CODE + target;
            case BIND_PHONE -> RedisCacheKey.BIND_PHONE_CODE + target;
            case BIND_EMAIL -> RedisCacheKey.BIND_EMAIL_CODE + target;
        };
    }

    /**
     * 处理尝试键相关数据。
     */
    private static String verificationAttemptKey(SecurityVerificationType type, String target) {
        return switch (type) {
            case LOGIN_SMS -> RedisCacheKey.LOGIN_SMS_CODE_ATTEMPTS + target;
            case LOGIN_EMAIL -> RedisCacheKey.LOGIN_EMAIL_CODE_ATTEMPTS + target;
            default -> throw new IllegalArgumentException("该验证码类型没有登录失败计数");
        };
    }

    /**
     * 判断尝试类型。
     */
    private static boolean isAttemptType(SecurityVerificationType type) {
        return type == SecurityVerificationType.LOGIN_SMS || type == SecurityVerificationType.LOGIN_EMAIL;
    }
}
