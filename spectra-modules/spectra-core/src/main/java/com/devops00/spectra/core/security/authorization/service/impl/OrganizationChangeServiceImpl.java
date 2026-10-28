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

package com.devops00.spectra.core.security.authorization.service.impl;

import com.devops00.spectra.common.audit.RequestCorrelationContext;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.devops00.spectra.common.exception.DataException;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.core.security.authorization.constant.SecurityAuthorizationState;
import com.devops00.spectra.core.security.authorization.domain.OrganizationChangeImpact;
import com.devops00.spectra.core.security.authorization.javabean.entity.RoleAssignment;
import com.devops00.spectra.core.security.authorization.javabean.from.OrganizationChangeApplyFrom;
import com.devops00.spectra.core.security.authorization.javabean.from.OrganizationChangeFrom;
import com.devops00.spectra.core.security.authorization.javabean.from.OrganizationCreateApplyFrom;
import com.devops00.spectra.core.security.authorization.javabean.vo.OrganizationChangePreviewVO;
import com.devops00.spectra.core.security.authorization.mapper.RoleAssignmentMapper;
import com.devops00.spectra.core.security.authorization.service.OrganizationChangeAuthorization;
import com.devops00.spectra.core.security.authorization.service.OrganizationChangeService;
import com.devops00.spectra.core.security.authorization.service.OrganizationImpactAnalyzer;
import com.devops00.spectra.core.system.service.DepartmentMutation;
import com.devops00.spectra.core.system.service.DepartmentOrganizationService;
import com.devops00.spectra.core.system.service.DepartmentService;
import com.devops00.spectra.core.user.service.UserSecurityStateService;
import com.devops00.spectra.framework.serialization.mapper.TimeMapper;
import com.devops00.spectra.common.audit.AuditRecord;
import com.devops00.spectra.common.audit.AuditService;
import com.devops00.spectra.core.audit.AuditRecordFactory;
import com.devops00.spectra.core.security.change.AuthorizationChangeToken;
import com.devops00.spectra.core.security.change.AuthorizationChangeTokenService;
import com.devops00.spectra.core.security.change.AuthorizationEpochGuard;
import com.devops00.spectra.core.security.change.HighRiskApprovalGate;
import com.devops00.spectra.core.security.change.SecurityChangeExecutor;
import com.devops00.spectra.common.port.security.SecuritySessionRevocationPort;
import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 部门新增、编辑和移动的组织版本与授权影响门禁。
 * <p>
 * 所有改变组织树或组织节点属性的写操作都必须经过同一套 Preview/Apply 流程：
 * Preview 固化请求摘要和 organizationVersion，Apply 再次校验并在事务中完成数据库变更、闭包重建、
 * organizationVersion 递增和受影响会话撤销。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationChangeServiceImpl implements OrganizationChangeService {

    private final DepartmentOrganizationService departmentOrganizationService;
    private final RoleAssignmentMapper roleAssignmentMapper;
    private final UserSecurityStateService userSecurityStateService;
    private final DepartmentService departmentService;
    private final OrganizationChangeAuthorization organizationChangeAuthorization;
    private final OrganizationImpactAnalyzer impactAnalyzer;
    private final AuthorizationChangeTokenService tokenService;
    private final AuthorizationEpochGuard epochGuard;
    private final SecuritySessionRevocationPort sessionRevocationPort;

    private final SecurityContextAccessor securityContextAccessor;
    private final SecurityChangeExecutor securityChangeExecutor;
    private final ObjectProvider<HighRiskApprovalGate> approvalGateProvider;

    private final AuditService auditService;

    private final AuditRecordFactory auditRecordFactory;

    private final TimeMapper timeMapper;

    @Override
    public long currentOrganizationVersion() {
        return readCurrentOrganizationVersion();
    }

    @Override
    public OrganizationChangePreviewVO preview(UUID departmentId, OrganizationChangeFrom from) {
        return preview(departmentId, from, ChangeType.UPDATE);
    }

    @Override
    public OrganizationChangePreviewVO previewCreate(OrganizationChangeFrom from) {
        return preview(null, from, ChangeType.CREATE);
    }

    /**
     * 处理内部业务逻辑（{@code preview}）。
     */
    private OrganizationChangePreviewVO preview(UUID departmentId, OrganizationChangeFrom from, ChangeType changeType) {
        var prepared = prepare(departmentId, from, changeType);
        var expiresAt = Instant.now().plusSeconds(300);
        var token = new AuthorizationChangeToken(UUID.randomUUID(), prepared.operatorId(), prepared.operatorId(),
                departmentId, UUID.randomUUID(), prepared.impact().beforeVersion(), prepared.requestHash(), expiresAt);
        var result = new OrganizationChangePreviewVO();
        result.setDepartmentId(departmentId);
        result.setNewParentId(prepared.requestedDepartment().pid());
        result.setPreviewToken(tokenService.issue(token));
        result.setExpectedOrganizationVersion(prepared.impact().beforeVersion());
        result.setAfterOrganizationVersion(prepared.impact().afterVersion());
        result.setExpiresAt(timeMapper.toOffsetDateTime(expiresAt));
        result.setAffectedAssignmentCount(prepared.impact().affectedAssignmentCount());
        result.setAffectedUserCount(prepared.impact().affectedUserCount());
        result.setExpandsEffectiveAuthority(prepared.impact().expandsEffectiveAuthority());
        appendAudit(new AuditInput("AUTHORIZATION_IMPACT_PREVIEWED", prepared.operatorId(), departmentId,
                Map.of("organizationVersion", prepared.impact().beforeVersion(), "operation", changeType.name()),
                Map.of(), "组织变更预览"));
        return result;
    }

    @Override
    @Transactional
    public void apply(UUID departmentId, OrganizationChangeApplyFrom from) {
        var prepared = verifyAndPrepare(departmentId, from, ChangeType.UPDATE);
        execute(prepared);
    }

    @Override
    @Transactional
    public void applyCreate(OrganizationCreateApplyFrom from) {
        var prepared = verifyAndPrepare(null, from, ChangeType.CREATE);
        execute(prepared);
    }

    /**
     * 处理内部业务逻辑（{@code verifyAndPrepare}）。
     */
    private PreparedChange verifyAndPrepare(UUID departmentId, OrganizationChangeApplyFrom from,
                                            ChangeType changeType) {
        if (from == null || from.getPreviewToken() == null || from.getPreviewToken().isBlank()) {
            throw new DataException("组织变更 Preview token 不能为空");
        }
        if (from.getExpectedOrganizationVersion() == null) {
            throw new DataException("组织变更 organizationVersion 不能为空");
        }
        var token = tokenService.verify(from.getPreviewToken());
        var operatorId = currentOperatorId();
        if (!operatorId.equals(token.operatorId())
                || !operatorId.equals(token.targetUserId())
                || !Objects.equals(departmentId, token.roleId())) {
            throw new DataException("组织变更 token 与当前操作者或目标部门不匹配");
        }
        if (!from.getExpectedOrganizationVersion().equals(token.expectedVersion())) {
            throw new DataException("组织变更 token 与 organizationVersion 不匹配");
        }
        var prepared = prepare(departmentId, from, changeType);
        if (!prepared.requestHash().equals(token.requestHash())) {
            throw new DataException("组织变更请求已被修改，请重新生成预览");
        }
        return prepared;
    }

    /**
     * 执行内部处理逻辑（{@code execute}）。
     */
    private void execute(PreparedChange prepared) {
        UUID operatorId = prepared.operatorId();
        UUID departmentId = prepared.departmentId();
        var event = auditRecordFactory.create(UUID.randomUUID(), "AUTHORIZATION_IMPACT_APPLIED", operatorId, departmentId,
                null, null, null, Map.of("organizationVersion", prepared.impact().beforeVersion()),
                Map.of("organizationVersion", prepared.impact().afterVersion(), "operation",
                        prepared.changeType().name()),
                "通过组织变更 Preview/Apply 提交", null, AuditRecord.Result.STARTED,
                RequestCorrelationContext.current().correlationId());
        securityChangeExecutor.execute(event, () -> {
            UUID persistedDepartmentId = persist(prepared);
            String eventType = prepared.changeType() == ChangeType.CREATE
                    ? "ORGANIZATION_NODE_CREATED"
                    : "ORGANIZATION_NODE_UPDATED";
            appendAudit(new AuditInput(eventType, operatorId, persistedDepartmentId,
                    Map.of("organizationVersion", prepared.impact().beforeVersion()),
                    Map.of("organizationVersion", prepared.impact().afterVersion(), "parentId",
                            String.valueOf(prepared.requestedDepartment().pid()), "name",
                            prepared.requestedDepartment().name()),
                    null));
            return Boolean.TRUE;
        });
    }

    /**
     * 创建或构建目标数据（{@code prepare}）。
     */
    private PreparedChange prepare(UUID departmentId, OrganizationChangeFrom from, ChangeType changeType) {
        validateInput(departmentId, from, changeType);
        var existing = departmentOrganizationService.find(departmentId);
        if (changeType == ChangeType.UPDATE && existing == null) {
            throw new DataNotExistException("目标部门不存在");
        }

        var currentVersion = readCurrentOrganizationVersion();
        if (currentVersion != from.getExpectedOrganizationVersion()) {
            throw new DataException("organizationVersion 已变化，请重新生成预览");
        }
        validateParent(departmentId, from.getNewParentId(), existing);

        var requestedDepartment = requestedDepartment(departmentId, from);
        var assignments = roleAssignmentMapper.selectList(new LambdaQueryWrapper<RoleAssignment>()
                .eq(RoleAssignment::getState, SecurityAuthorizationState.ACTIVE.name()));
        var userIds = assignments.stream().map(RoleAssignment::getUserId).collect(Collectors.toSet());
        var impact = impactAnalyzer.analyze(currentVersion, assignments.size(), userIds.size(), true);
        var operatorId = currentOperatorId();
        evaluatePermission(operatorId, changeType, departmentId, from.getNewParentId());
        var requestHash = requestHash(departmentId, changeType, requestedDepartment, currentVersion);
        assertApproval(changeType, requestHash);
        return new PreparedChange(operatorId, departmentId, changeType, requestedDepartment, impact, requestHash,
                userIds);
    }

    private void validateInput(UUID departmentId, OrganizationChangeFrom from, ChangeType changeType) {
        if ((changeType == ChangeType.UPDATE && departmentId == null)
                || from == null
                || from.getExpectedOrganizationVersion() == null
                || from.getName() == null
                || from.getName().isBlank()
                || from.getType() == null
                || from.getRegionId() == null) {
            throw new DataException("组织变更参数不能为空");
        }
    }

    private void evaluatePermission(UUID operatorId, ChangeType changeType, UUID departmentId, UUID newParentId) {
        String permission = changeType == ChangeType.CREATE ? "department:create" : "department:update";
        var scopeIds = new ArrayList<UUID>();
        if (departmentId != null) {
            scopeIds.add(departmentId);
        }
        if (newParentId != null) {
            scopeIds.add(newParentId);
        }
        organizationChangeAuthorization.evaluate(operatorId, List.of(permission), scopeIds);
    }

    private void assertApproval(ChangeType changeType, String requestHash) {
        var approvalGate = approvalGateProvider.getIfAvailable();
        if (approvalGate != null) {
            approvalGate.assertAllowed(changeType == ChangeType.CREATE ? "ORGANIZATION_CREATE" : "ORGANIZATION_CHANGE",
                    requestHash);
        }
    }

    /**
     * 校验并确保数据满足当前约束（{@code validateParent}）。
     */
    private void validateParent(UUID departmentId, UUID newParentId,
                                DepartmentOrganizationService.DepartmentSnapshot existing) {
        if (existing != null && Objects.equals(departmentId, newParentId)) {
            throw new DataException("部门不能移动到自身");
        }
        if (newParentId == null) {
            return;
        }
        if (departmentOrganizationService.find(newParentId) == null) {
            throw new DataNotExistException("新的上级部门不存在");
        }
        if (existing != null && departmentService.getSelfAndDescendantIds(departmentId).contains(newParentId)) {
            throw new DataException("部门不能移动到自己的下级节点");
        }
    }

    /**
     * 处理内部业务逻辑（{@code requestedDepartment}）。
     */
    private DepartmentOrganizationService.DepartmentSnapshot requestedDepartment(UUID departmentId,
                                                                                 OrganizationChangeFrom from) {
        return new DepartmentOrganizationService.DepartmentSnapshot(departmentId, from.getNewParentId(), null,
                from.getName().trim(), from.getType().toString(), from.getRegionId(), from.getSort(),
                from.getRemark());
    }

    /**
     * 查询或获取目标数据（{@code readCurrentOrganizationVersion}）。
     */
    private long readCurrentOrganizationVersion() {
        return departmentOrganizationService.readVersion();
    }

    /**
     * 处理内部业务逻辑（{@code persist}）。
     */
    private UUID persist(PreparedChange prepared) {
        var department = prepared.requestedDepartment();
        var mutation = new DepartmentMutation();
        mutation.setId(prepared.departmentId());
        mutation.setCode(IdWorker.get32UUID().toUpperCase());
        mutation.setPid(department.pid());
        mutation.setName(department.name());
        mutation.setType(department.type());
        mutation.setRegionId(department.regionId());
        mutation.setSort(department.sort());
        mutation.setRemark(department.remark());
        UUID persistedDepartmentId;
        if (prepared.changeType() == ChangeType.CREATE) {
            persistedDepartmentId = departmentOrganizationService.create(mutation);
        } else {
            departmentOrganizationService.update(mutation);
            persistedDepartmentId = prepared.departmentId();
        }
        departmentOrganizationService.rebuildClosure();
        departmentOrganizationService.advanceVersion(prepared.impact().beforeVersion());
        String currentAccessToken = securityContextAccessor.currentToken();
        for (var userId : prepared.affectedUserIds()) {
            var user = userSecurityStateService.load(userId);
            var version = user.securityVersion();
            epochGuard.assertCurrent(userId, version);
            epochGuard.advance(userId, version);
            if (Objects.equals(userId, prepared.operatorId())) {
                sessionRevocationPort.revokeUserSessionsExceptToken(userId, currentAccessToken);
            } else {
                sessionRevocationPort.revokeUserSessions(userId);
            }
        }
        return persistedDepartmentId;
    }

    /**
     * 处理内部业务逻辑（{@code requestHash}）。
     */
    private String requestHash(UUID departmentId, ChangeType changeType,
                               DepartmentOrganizationService.DepartmentSnapshot department, long version) {
        String canonical = List.of(changeType.name(), canonicalValue(departmentId), Long.toString(version),
                canonicalValue(department.pid()), canonicalValue(department.name()),
                canonicalValue(department.type()), canonicalValue(department.regionId()),
                canonicalValue(department.sort()), canonicalValue(department.remark()))
                .stream()
                .map(this::canonicalValue)
                .collect(Collectors.joining("|"));
        try {
            var digest = MessageDigest.getInstance("SHA-256").digest(canonical.getBytes(StandardCharsets.UTF_8));
            var result = new StringBuilder();
            for (byte value : digest) {
                result.append(String.format("%02x", value));
            }
            return result.toString();
        } catch (Exception exception) {
            throw new IllegalStateException("计算组织变更摘要失败", exception);
        }
    }

    /**
     * 判断条件是否满足（{@code canonicalValue}）。
     */
    private String canonicalValue(Object value) {
        if (value == null) {
            return "-";
        }
        String text = value.toString();
        return text.length() + ":" + text;
    }

    /**
     * 查询或获取目标数据（{@code currentOperatorId}）。
     */
    private UUID currentOperatorId() {
        var operatorId = securityContextAccessor.currentUserId();
        if (operatorId == null) {
            throw new DataException("无法识别当前安全主体");
        }
        return operatorId;
    }

    /**
     * 更新或推进目标状态（{@code appendAudit}）。
     */
    private void appendAudit(AuditInput input) {
        var event = auditRecordFactory.create(null, input.eventType(), input.operatorId(), input.targetId(), null, null, null,
                input.before(), input.after(), input.reason(), null, AuditRecord.Result.SUCCEEDED,
                RequestCorrelationContext.current().correlationId());
        auditService.record(event);
    }

    private record AuditInput(String eventType, UUID operatorId, UUID targetId, Map<String, Object> before,
                              Map<String, Object> after, String reason) {
    }

    /**
     * 定义 {@code ChangeType} 支持的业务类型及取值。
     *
     * @author yangxj96
     * @version 1.0
     * @since 2026/09/13
     */
    private enum ChangeType {
        CREATE,
        UPDATE
    }

    /**
     * 实现相关数据相关的应用服务逻辑。
     *
     * @param operatorId          操作人标识
     * @param departmentId        部门标识
     * @param changeType          本次组织或授权变更的类型
     * @param requestedDepartment 请求调整到的目标部门
     * @param impact              组织调整对权限和用户的影响明细
     * @param requestHash         请求哈希值
     * @param affectedUserIds     用户标识
     * @author yangxj96
     * @version 1.0
     * @since 2026/09/13
     */
    private record PreparedChange(UUID operatorId,
                                  UUID departmentId,
                                  ChangeType changeType,
                                  DepartmentOrganizationService.DepartmentSnapshot requestedDepartment,
                                  OrganizationChangeImpact impact,
                                  String requestHash,
                                  Set<UUID> affectedUserIds) {
    }
}
