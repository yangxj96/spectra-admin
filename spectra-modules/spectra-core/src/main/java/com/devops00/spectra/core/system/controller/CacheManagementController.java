/* Copyright 2018-2026 yangxj96 */

package com.devops00.spectra.core.system.controller;

import com.devops00.spectra.common.audit.Audit;
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
import com.devops00.spectra.core.system.javabean.vo.SecurityUserCandidateVO;
import com.devops00.spectra.core.system.javabean.vo.SecurityVerificationCandidateVO;
import com.devops00.spectra.core.system.service.CacheManagementService;
import com.devops00.spectra.core.system.service.SecurityTargetCandidateService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** 缓存监控和系统维护管理接口。 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/cache")
public class CacheManagementController {

    private final CacheManagementService service;

    private final SecurityTargetCandidateService candidateService;

    @Audit("'获取缓存监控总览'")
    @GetMapping(value = "/monitor/overview", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'system:cache:read')")
    public CacheMonitorOverviewVO getOverview() {
        return service.getOverview();
    }

    @Audit("'获取缓存区域监控'")
    @GetMapping(value = "/monitor/regions", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'system:cache:read')")
    public List<CacheRegionVO> getRegions() {
        return service.getRegions();
    }

    @Audit("'获取安全运行态摘要'")
    @GetMapping(value = "/monitor/security", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'system:cache:read')")
    public SecurityRuntimeVO getSecurity() {
        return service.getSecurity();
    }

    @Audit("'查询缓存维护操作状态'")
    @GetMapping(value = "/admin/operations/{operationId}", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'system:cache:read')")
    public CacheOperationVO getOperation(@PathVariable UUID operationId) {
        return service.getOperation(operationId);
    }

    @Audit("'预览普通缓存清理'")
    @PostMapping(value = "/admin/business/clear/preview", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'system:cache:clear')")
    public CacheOperationVO previewBusinessClear(@Validated @RequestBody CacheBusinessPreviewFrom from) {
        return service.previewBusinessClear(from);
    }

    @Audit("'执行普通缓存清理'")
    @PostMapping(value = "/admin/business/clear", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'system:cache:clear')")
    public CacheOperationVO clearBusiness(@Validated @RequestBody CacheBusinessClearFrom from) {
        return service.clearBusiness(from);
    }

    @Audit("'撤销指定客户端安全会话'")
    @PostMapping(value = "/admin/security/session/revoke", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'session:revoke')")
    public CacheOperationVO revokeSession(@Validated @RequestBody SecuritySessionRevokeFrom from) {
        return service.revokeSession(from);
    }

    @Audit("'撤销用户全部安全会话'")
    @PostMapping(value = "/admin/security/session/revoke-all", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'session:revoke')")
    public CacheOperationVO revokeAllSessions(@Validated @RequestBody SecuritySessionRevokeAllFrom from) {
        return service.revokeAllSessions(from);
    }

    @Audit("'查询会话维护用户候选'")
    @GetMapping(value = "/admin/security/session/candidates", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'session:revoke')")
    public List<SecurityUserCandidateVO> getSessionCandidates(@RequestParam("keyword") String keyword) {
        return candidateService.searchUserCandidates(keyword);
    }

    @Audit("'清理安全验证码状态'")
    @PostMapping(value = "/admin/security/verification/clear", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'security:verification:manage')")
    public CacheOperationVO clearVerification(@Validated @RequestBody SecurityVerificationClearFrom from) {
        return service.clearVerification(from);
    }

    @Audit("'查询验证码维护目标候选'")
    @GetMapping(value = "/admin/security/verification/candidates", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'security:verification:manage')")
    public List<SecurityVerificationCandidateVO> getVerificationCandidates(
            @RequestParam("type") SecurityVerificationType type, @RequestParam("keyword") String keyword) {
        return candidateService.searchVerificationCandidates(type, keyword);
    }

    @Audit("'清理登录失败锁定计数'")
    @PostMapping(value = "/admin/security/login-failure/clear", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'security:login-failure:manage')")
    public CacheOperationVO clearLoginFailure(@Validated @RequestBody SecurityLoginFailureClearFrom from) {
        return service.clearLoginFailure(from);
    }

    @Audit("'查询登录失败锁定账号候选'")
    @GetMapping(value = "/admin/security/login-failure/candidates", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'security:login-failure:manage')")
    public List<SecurityUserCandidateVO> getLoginFailureCandidates(@RequestParam("keyword") String keyword) {
        return candidateService.searchUserCandidates(keyword);
    }

    @Audit("'定向失效 Web 加密 nonce'")
    @PostMapping(value = "/admin/security/nonce/invalidate", version = "1.0.0")
    @PreAuthorize("hasRole('ROLE_DEV_OPS')")
    public CacheOperationVO invalidateNonce(@Validated @RequestBody SecurityNonceInvalidateFrom from) {
        return service.invalidateNonce(from);
    }

    @Audit("'全局失效 Web 加密 nonce 窗口'")
    @PostMapping(value = "/admin/security/nonce/invalidate-all", version = "1.0.0")
    @PreAuthorize("hasRole('ROLE_DEV_OPS')")
    public CacheOperationVO invalidateAllNonces(
            @Validated @RequestBody SecurityNonceGlobalInvalidateFrom from) {
        return service.invalidateAllNonces(from);
    }
}
