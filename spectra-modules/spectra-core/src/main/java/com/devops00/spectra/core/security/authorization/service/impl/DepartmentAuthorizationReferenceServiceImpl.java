/*
 * Copyright 2018-2026 yangxj96
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.devops00.spectra.core.security.authorization.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.devops00.spectra.common.exception.DataException;
import com.devops00.spectra.core.security.authorization.constant.SecurityAuthorizationState;
import com.devops00.spectra.core.security.authorization.javabean.entity.AssignmentGrantBoundary;
import com.devops00.spectra.core.security.authorization.javabean.entity.AssignmentPermissionBoundary;
import com.devops00.spectra.core.security.authorization.javabean.entity.AuthorizationProfile;
import com.devops00.spectra.core.security.authorization.javabean.entity.AuthorizationProfileAssignment;
import com.devops00.spectra.core.security.authorization.javabean.entity.AuthorizationProfileBoundary;
import com.devops00.spectra.core.security.authorization.javabean.entity.AuthorizationScope;
import com.devops00.spectra.core.security.authorization.javabean.entity.RoleAssignment;
import com.devops00.spectra.core.security.authorization.javabean.entity.ScopeRule;
import com.devops00.spectra.core.security.authorization.mapper.AssignmentGrantBoundaryMapper;
import com.devops00.spectra.core.security.authorization.mapper.AssignmentPermissionBoundaryMapper;
import com.devops00.spectra.core.security.authorization.mapper.AuthorizationProfileAssignmentMapper;
import com.devops00.spectra.core.security.authorization.mapper.AuthorizationProfileBoundaryMapper;
import com.devops00.spectra.core.security.authorization.mapper.AuthorizationProfileMapper;
import com.devops00.spectra.core.security.authorization.mapper.AuthorizationScopeMapper;
import com.devops00.spectra.core.security.authorization.mapper.RoleAssignmentMapper;
import com.devops00.spectra.core.security.authorization.mapper.ScopeRuleMapper;
import com.devops00.spectra.core.security.authorization.service.DepartmentAuthorizationReferenceImpact;
import com.devops00.spectra.core.security.authorization.service.DepartmentAuthorizationReferenceService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 结构化更新活动授权范围和授权方案 JSONB 部门引用。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/28
 */
@Service
public class DepartmentAuthorizationReferenceServiceImpl implements DepartmentAuthorizationReferenceService {

    private static final String DEPARTMENT_RULE = "DEPARTMENT";
    private static final String RULES_MODE = "RULES";

    private final RoleAssignmentMapper roleAssignmentMapper;
    private final AssignmentPermissionBoundaryMapper permissionBoundaryMapper;
    private final AssignmentGrantBoundaryMapper grantBoundaryMapper;
    private final AuthorizationScopeMapper scopeMapper;
    private final ScopeRuleMapper scopeRuleMapper;
    private final AuthorizationProfileMapper profileMapper;
    private final AuthorizationProfileAssignmentMapper profileAssignmentMapper;
    private final AuthorizationProfileBoundaryMapper profileBoundaryMapper;

    @SuppressWarnings("PMD.ExcessiveParameterList") // EX-B02-PMD-009: 授权引用服务的持久化协作者逐项注入。
    public DepartmentAuthorizationReferenceServiceImpl(RoleAssignmentMapper roleAssignmentMapper,
                                                       AssignmentPermissionBoundaryMapper permissionBoundaryMapper,
                                                       AssignmentGrantBoundaryMapper grantBoundaryMapper,
                                                       AuthorizationScopeMapper scopeMapper,
                                                       ScopeRuleMapper scopeRuleMapper,
                                                       AuthorizationProfileMapper profileMapper,
                                                       AuthorizationProfileAssignmentMapper profileAssignmentMapper,
                                                       AuthorizationProfileBoundaryMapper profileBoundaryMapper) {
        this.roleAssignmentMapper = roleAssignmentMapper;
        this.permissionBoundaryMapper = permissionBoundaryMapper;
        this.grantBoundaryMapper = grantBoundaryMapper;
        this.scopeMapper = scopeMapper;
        this.scopeRuleMapper = scopeRuleMapper;
        this.profileMapper = profileMapper;
        this.profileAssignmentMapper = profileAssignmentMapper;
        this.profileBoundaryMapper = profileBoundaryMapper;
    }

    @Override
    public DepartmentAuthorizationReferenceImpact previewMerge(Collection<UUID> sourceDepartmentIds,
                                                               Collection<String> sourceDepartmentCodes,
                                                               UUID targetDepartmentId,
                                                               String targetDepartmentCode) {
        var request = normalize(sourceDepartmentIds, sourceDepartmentCodes, targetDepartmentId,
                targetDepartmentCode, null);
        return impact(loadSnapshot(request), request);
    }

    @Override
    public DepartmentAuthorizationReferenceImpact applyMerge(Collection<UUID> sourceDepartmentIds,
                                                             Collection<String> sourceDepartmentCodes,
                                                             UUID targetDepartmentId,
                                                             String targetDepartmentCode,
                                                             UUID operatorId) {
        if (operatorId == null) {
            throw new DataException("无法识别当前安全主体");
        }
        var request = normalize(sourceDepartmentIds, sourceDepartmentCodes, targetDepartmentId,
                targetDepartmentCode, operatorId);
        var snapshot = loadSnapshot(request);
        var impact = impact(snapshot, request);
        rewriteActiveScopeRules(snapshot, request);
        rewriteProfileBoundaries(snapshot, request);
        return impact;
    }

    private Request normalize(Collection<UUID> sourceDepartmentIds,
                              Collection<String> sourceDepartmentCodes,
                              UUID targetDepartmentId,
                              String targetDepartmentCode,
                              UUID operatorId) {
        var ids = sourceDepartmentIds == null
                ? Set.<UUID>of()
                : sourceDepartmentIds.stream().filter(Objects::nonNull).collect(Collectors.toCollection(LinkedHashSet::new));
        var codes = sourceDepartmentCodes == null
                ? Set.<String>of()
                : sourceDepartmentCodes.stream()
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(code -> !code.isEmpty())
                        .collect(Collectors.toCollection(LinkedHashSet::new));
        var targetCode = targetDepartmentCode == null ? null : targetDepartmentCode.trim();
        if (ids.size() < 2
                || codes.size() != ids.size()
                || targetDepartmentId == null
                || targetCode == null
                || targetCode.isEmpty()
                || codes.contains(targetCode)) {
            throw new DataException("授权引用重写参数无效");
        }
        return new Request(Set.copyOf(ids), Set.copyOf(codes), targetDepartmentId, targetCode, operatorId);
    }

    private Snapshot loadSnapshot(Request request) {
        var activeAssignments = roleAssignmentMapper.selectList(new LambdaQueryWrapper<RoleAssignment>()
                .eq(RoleAssignment::getState, SecurityAuthorizationState.ACTIVE.name())
                .isNull(RoleAssignment::getDeleted))
                .stream()
                .filter(row -> SecurityAuthorizationState.ACTIVE.name().equals(row.getState()) && row.getDeleted() == null)
                .toList();
        var activeAssignmentIds = activeAssignments.stream()
                .map(RoleAssignment::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        var accessBoundaries = activeAssignmentIds.isEmpty()
                ? List.<AssignmentPermissionBoundary>of()
                : permissionBoundaryMapper.selectList(new LambdaQueryWrapper<AssignmentPermissionBoundary>()
                        .in(AssignmentPermissionBoundary::getAssignmentId, activeAssignmentIds)
                        .isNull(AssignmentPermissionBoundary::getDeleted))
                        .stream()
                        .filter(row -> activeAssignmentIds.contains(row.getAssignmentId()) && row.getDeleted() == null)
                        .toList();
        var grantBoundaries = activeAssignmentIds.isEmpty()
                ? List.<AssignmentGrantBoundary>of()
                : grantBoundaryMapper.selectList(new LambdaQueryWrapper<AssignmentGrantBoundary>()
                        .in(AssignmentGrantBoundary::getAssignmentId, activeAssignmentIds)
                        .isNull(AssignmentGrantBoundary::getDeleted))
                        .stream()
                        .filter(row -> activeAssignmentIds.contains(row.getAssignmentId()) && row.getDeleted() == null)
                        .toList();
        var scopeIds = new LinkedHashSet<UUID>();
        accessBoundaries.stream().map(AssignmentPermissionBoundary::getScopeId).filter(Objects::nonNull).forEach(scopeIds::add);
        grantBoundaries.stream().map(AssignmentGrantBoundary::getScopeId).filter(Objects::nonNull).forEach(scopeIds::add);
        var scopes = scopeIds.isEmpty()
                ? List.<AuthorizationScope>of()
                : scopeMapper.selectBatchIds(scopeIds)
                        .stream()
                        .filter(scope -> scope.getDeleted() == null)
                        .toList();
        var rulesScopeIds = scopes.stream()
                .filter(scope -> RULES_MODE.equals(scope.getScopeMode()))
                .map(AuthorizationScope::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        var scopeRules = rulesScopeIds.isEmpty()
                ? List.<ScopeRule>of()
                : scopeRuleMapper.selectList(new LambdaQueryWrapper<ScopeRule>()
                        .in(ScopeRule::getScopeId, rulesScopeIds)
                        .isNull(ScopeRule::getDeleted))
                        .stream()
                        .filter(rule -> rule.getDeleted() == null && rulesScopeIds.contains(rule.getScopeId()))
                        .toList();

        var profiles = profileMapper.selectList(new LambdaQueryWrapper<AuthorizationProfile>()
                .isNull(AuthorizationProfile::getDeleted)).stream().filter(profile -> profile.getDeleted() == null).toList();
        var profileIds = profiles.stream().map(AuthorizationProfile::getId).filter(Objects::nonNull).collect(Collectors.toSet());
        var profileAssignments = profileIds.isEmpty()
                ? List.<AuthorizationProfileAssignment>of()
                : profileAssignmentMapper.selectList(new LambdaQueryWrapper<AuthorizationProfileAssignment>()
                        .in(AuthorizationProfileAssignment::getProfileId, profileIds)
                        .isNull(AuthorizationProfileAssignment::getDeleted))
                        .stream()
                        .filter(row -> profileIds.contains(row.getProfileId()) && row.getDeleted() == null)
                        .toList();
        var profileAssignmentIds = profileAssignments.stream()
                .map(AuthorizationProfileAssignment::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        var profileBoundaries = profileAssignmentIds.isEmpty()
                ? List.<AuthorizationProfileBoundary>of()
                : profileBoundaryMapper.selectList(new LambdaQueryWrapper<AuthorizationProfileBoundary>()
                        .in(AuthorizationProfileBoundary::getProfileAssignmentId, profileAssignmentIds)
                        .isNull(AuthorizationProfileBoundary::getDeleted))
                        .stream()
                        .filter(row -> profileAssignmentIds.contains(row.getProfileAssignmentId()) && row.getDeleted() == null)
                        .toList();
        var profileIdByAssignmentId = profileAssignments.stream()
                .filter(row -> row.getId() != null)
                .collect(Collectors.toMap(AuthorizationProfileAssignment::getId,
                        AuthorizationProfileAssignment::getProfileId, (first, ignored) -> first));
        return new Snapshot(activeAssignments, accessBoundaries, grantBoundaries, scopes, scopeRules,
                profiles, profileBoundaries, profileIdByAssignmentId);
    }

    private DepartmentAuthorizationReferenceImpact impact(Snapshot snapshot, Request request) {
        var rulesByScope = snapshot.scopeRules()
                .stream()
                .filter(this::isDepartmentRule)
                .collect(Collectors.groupingBy(ScopeRule::getScopeId));
        var accessScopeIds = relevantAccessScopeIds(snapshot.accessBoundaries(), rulesByScope, request.sourceIds());
        var grantScopeIds = relevantGrantScopeIds(snapshot.grantBoundaries(), rulesByScope, request.sourceIds());
        var accessRules = matchingRules(accessScopeIds, rulesByScope, request.sourceIds());
        var grantRules = matchingRules(grantScopeIds, rulesByScope, request.sourceIds());
        var impactedAssignmentIds = new HashSet<UUID>();
        snapshot.accessBoundaries()
                .stream()
                .filter(row -> accessScopeIds.contains(row.getScopeId()))
                .map(AssignmentPermissionBoundary::getAssignmentId)
                .filter(Objects::nonNull)
                .forEach(impactedAssignmentIds::add);
        snapshot.grantBoundaries()
                .stream()
                .filter(row -> grantScopeIds.contains(row.getScopeId()))
                .map(AssignmentGrantBoundary::getAssignmentId)
                .filter(Objects::nonNull)
                .forEach(impactedAssignmentIds::add);
        var affectedUserIds = snapshot.activeAssignments()
                .stream()
                .filter(assignment -> impactedAssignmentIds.contains(assignment.getId()))
                .map(RoleAssignment::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        var profileIdsByAssignment = snapshot.profileIdByAssignmentId();
        var impactedProfileIds = new HashSet<UUID>();
        int profileAccessScopeCount = 0;
        int profileGrantScopeCount = 0;
        boolean expands = expandsRuleScopes(accessScopeIds, rulesByScope, request.sourceIds())
                || expandsRuleScopes(grantScopeIds, rulesByScope, request.sourceIds());
        var fingerprintParts = new ArrayList<String>();
        snapshot.activeAssignments()
                .stream()
                .filter(assignment -> impactedAssignmentIds.contains(assignment.getId()))
                .sorted(java.util.Comparator.comparing(RoleAssignment::getId))
                .forEach(assignment -> fingerprintParts.add("ASSIGNMENT:" + assignment.getId() + ":"
                        + assignment.getVersion() + ":" + assignment.getState() + ":" + assignment.getUserId()));
        snapshot.accessBoundaries()
                .stream()
                .filter(boundary -> accessScopeIds.contains(boundary.getScopeId()))
                .sorted(java.util.Comparator.comparing(AssignmentPermissionBoundary::getId))
                .forEach(boundary -> fingerprintParts.add("ACCESS:" + boundary.getAssignmentId() + ":" + boundary.getScopeId()));
        snapshot.grantBoundaries()
                .stream()
                .filter(boundary -> grantScopeIds.contains(boundary.getScopeId()))
                .sorted(java.util.Comparator.comparing(AssignmentGrantBoundary::getId))
                .forEach(boundary -> fingerprintParts.add("GRANT:" + boundary.getAssignmentId() + ":" + boundary.getScopeId()));
        accessRules.stream().map(this::canonicalRule).sorted().forEach(fingerprintParts::add);
        grantRules.stream().map(this::canonicalRule).sorted().forEach(fingerprintParts::add);
        for (var boundary : snapshot.profileBoundaries()) {
            var profileId = profileIdsByAssignment.get(boundary.getProfileAssignmentId());
            if (profileId == null) {
                continue;
            }
            var accessMatch = scopeReferencesCodes(boundary.getAccessScope(), request.sourceCodes());
            var grantMatch = scopeReferencesCodes(boundary.getGrantScope(), request.sourceCodes());
            if (accessMatch) {
                profileAccessScopeCount++;
                impactedProfileIds.add(profileId);
                expands |= scopeMayExpand(boundary.getAccessScope(), request.sourceCodes());
            }
            if (grantMatch) {
                profileGrantScopeCount++;
                impactedProfileIds.add(profileId);
                expands |= scopeMayExpand(boundary.getGrantScope(), request.sourceCodes());
            }
            if (accessMatch || grantMatch) {
                var profile = snapshot.profiles().stream().filter(row -> profileId.equals(row.getId())).findFirst().orElse(null);
                fingerprintParts.add("PROFILE:" + profileId + ":" + (profile == null ? "-" : profile.getVersion())
                        + ":" + (profile == null ? "-" : profile.getState()) + ":BOUNDARY:" + boundary.getId()
                        + ":" + boundary.getVersion() + ":A:" + canonicalScope(boundary.getAccessScope())
                        + ":G:" + canonicalScope(boundary.getGrantScope()));
            }
        }
        return new DepartmentAuthorizationReferenceImpact(impactedAssignmentIds.size(), impactedProfileIds.size(),
                accessRules.size(), grantRules.size(), profileAccessScopeCount, profileGrantScopeCount, expands,
                affectedUserIds, fingerprint(fingerprintParts));
    }

    private String canonicalRule(ScopeRule rule) {
        return "RULE:" + rule.getId() + ":" + rule.getVersion() + ":" + rule.getScopeId() + ":"
                + rule.getRuleType() + ":" + rule.getDepartmentId() + ":" + rule.getIncludeDescendants()
                + ":" + canonicalValue(rule.getRulePayload());
    }

    private String canonicalScope(Map<String, Object> scope) {
        if (scope == null) {
            return "-";
        }
        return scope.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "=" + canonicalValue(entry.getValue()))
                .collect(Collectors.joining(",", "{", "}"));
    }

    private String canonicalValue(Object value) {
        if (value instanceof Map<?, ?> map) {
            return map.entrySet()
                    .stream()
                    .filter(entry -> entry.getKey() != null)
                    .sorted(java.util.Comparator.comparing(entry -> String.valueOf(entry.getKey())))
                    .map(entry -> entry.getKey() + "=" + canonicalValue(entry.getValue()))
                    .collect(Collectors.joining(",", "{", "}"));
        }
        if (value instanceof Collection<?> collection) {
            return collection.stream().map(this::canonicalValue).collect(Collectors.joining(",", "[", "]"));
        }
        return String.valueOf(value);
    }

    private String fingerprint(List<String> parts) {
        try {
            var digest = MessageDigest.getInstance("SHA-256")
                    .digest(
                            String.join("|", parts).getBytes(StandardCharsets.UTF_8));
            var result = new StringBuilder(digest.length * 2);
            for (byte value : digest) {
                result.append(String.format("%02x", value));
            }
            return result.toString();
        } catch (Exception exception) {
            throw new IllegalStateException("计算授权引用预览摘要失败", exception);
        }
    }

    private void rewriteActiveScopeRules(Snapshot snapshot, Request request) {
        var rulesByScope = snapshot.scopeRules()
                .stream()
                .filter(this::isDepartmentRule)
                .collect(Collectors.groupingBy(ScopeRule::getScopeId));
        var accessScopeIds = relevantAccessScopeIds(snapshot.accessBoundaries(), rulesByScope, request.sourceIds());
        var grantScopeIds = relevantGrantScopeIds(snapshot.grantBoundaries(), rulesByScope, request.sourceIds());
        var affectedScopeIds = new HashSet<>(accessScopeIds);
        affectedScopeIds.addAll(grantScopeIds);
        for (var scopeId : affectedScopeIds) {
            var seen = new HashSet<DepartmentRuleKey>();
            for (var rule : rulesByScope.getOrDefault(scopeId, List.of())) {
                var sourceReference = request.sourceIds().contains(rule.getDepartmentId());
                var departmentId = sourceReference ? request.targetId() : rule.getDepartmentId();
                var key = new DepartmentRuleKey(departmentId, Boolean.TRUE.equals(rule.getIncludeDescendants()));
                if (!sourceReference) {
                    seen.add(key);
                    continue;
                }
                if (!seen.add(key)) {
                    if (scopeRuleMapper.deleteDuplicateDepartmentRule(rule.getId(), rule.getVersion()) != 1) {
                        throw new DataException("删除重复授权部门规则失败");
                    }
                    continue;
                }
                var now = Instant.now();
                var nextVersion = rule.getVersion() == null ? 1L : rule.getVersion() + 1;
                var updated = scopeRuleMapper.update(null, new UpdateWrapper<ScopeRule>()
                        .eq("id", rule.getId())
                        .eq("version", rule.getVersion())
                        .isNull("deleted")
                        .set("department_id", request.targetId())
                        .set("updated_by", request.operatorId())
                        .set("updated_at", now)
                        .set("version", nextVersion));
                if (updated != 1) {
                    throw new DataException("授权部门规则并发变化，部门合并已拒绝");
                }
                rule.setDepartmentId(request.targetId());
                rule.setUpdatedBy(request.operatorId());
                rule.setUpdatedAt(now);
                rule.setVersion(nextVersion);
            }
        }
    }

    private void rewriteProfileBoundaries(Snapshot snapshot, Request request) {
        var profileById = snapshot.profiles()
                .stream()
                .filter(profile -> profile.getId() != null)
                .collect(Collectors.toMap(AuthorizationProfile::getId, Function.identity(), (first, ignored) -> first));
        var changedProfiles = new HashSet<UUID>();
        for (var boundary : snapshot.profileBoundaries()) {
            var profileId = snapshot.profileIdByAssignmentId().get(boundary.getProfileAssignmentId());
            if (profileId == null || !profileById.containsKey(profileId)) {
                continue;
            }
            boolean changed = false;
            var newAccess = remapScope(boundary.getAccessScope(), request.sourceCodes(), request.targetCode());
            if (newAccess != null) {
                boundary.setAccessScope(newAccess);
                changed = true;
            }
            var newGrant = remapScope(boundary.getGrantScope(), request.sourceCodes(), request.targetCode());
            if (newGrant != null) {
                boundary.setGrantScope(newGrant);
                changed = true;
            }
            if (!changed) {
                continue;
            }
            if (profileBoundaryMapper.updateById(boundary) != 1) {
                throw new DataException("更新授权方案边界失败");
            }
            changedProfiles.add(profileId);
        }
        for (var profileId : changedProfiles) {
            var profile = profileById.get(profileId);
            var version = profile.getVersion() == null ? 0L : profile.getVersion();
            var now = Instant.now();
            var updated = profileMapper.update(null, new UpdateWrapper<AuthorizationProfile>()
                    .eq("id", profileId)
                    .eq("version", version)
                    .isNull("deleted")
                    .set("version", version + 1)
                    .set("updated_by", request.operatorId())
                    .set("updated_at", now));
            if (updated != 1) {
                throw new DataException("授权方案版本并发变化，部门合并已拒绝");
            }
            profile.setVersion(version + 1);
            profile.setUpdatedBy(request.operatorId());
            profile.setUpdatedAt(now);
        }
    }

    private Set<UUID> relevantAccessScopeIds(List<AssignmentPermissionBoundary> boundaries,
                                             Map<UUID, List<ScopeRule>> rulesByScope,
                                             Set<UUID> sourceIds) {
        return boundaries.stream()
                .map(AssignmentPermissionBoundary::getScopeId)
                .filter(Objects::nonNull)
                .filter(scopeId -> hasSourceRule(rulesByScope.getOrDefault(scopeId, List.of()), sourceIds))
                .collect(Collectors.toSet());
    }

    private Set<UUID> relevantGrantScopeIds(List<AssignmentGrantBoundary> boundaries,
                                            Map<UUID, List<ScopeRule>> rulesByScope,
                                            Set<UUID> sourceIds) {
        return boundaries.stream()
                .map(AssignmentGrantBoundary::getScopeId)
                .filter(Objects::nonNull)
                .filter(scopeId -> hasSourceRule(rulesByScope.getOrDefault(scopeId, List.of()), sourceIds))
                .collect(Collectors.toSet());
    }

    private List<ScopeRule> matchingRules(Set<UUID> scopeIds,
                                          Map<UUID, List<ScopeRule>> rulesByScope,
                                          Set<UUID> sourceIds) {
        return scopeIds.stream()
                .flatMap(scopeId -> rulesByScope.getOrDefault(scopeId, List.of()).stream())
                .filter(rule -> sourceIds.contains(rule.getDepartmentId()))
                .toList();
    }

    private boolean expandsRuleScopes(Set<UUID> scopeIds,
                                      Map<UUID, List<ScopeRule>> rulesByScope,
                                      Set<UUID> sourceIds) {
        return scopeIds.stream()
                .map(rulesByScope::get)
                .filter(Objects::nonNull)
                .anyMatch(rules -> rules.stream()
                        .filter(rule -> sourceIds.contains(rule.getDepartmentId()))
                        .anyMatch(rule -> Boolean.TRUE.equals(rule.getIncludeDescendants()))
                        && rules.stream()
                                .filter(rule -> sourceIds.contains(rule.getDepartmentId()))
                                .map(ScopeRule::getDepartmentId)
                                .collect(Collectors.toSet())
                                .size() < sourceIds.size());
    }

    private boolean hasSourceRule(List<ScopeRule> rules, Set<UUID> sourceIds) {
        return rules.stream().anyMatch(rule -> isDepartmentRule(rule) && sourceIds.contains(rule.getDepartmentId()));
    }

    private boolean isDepartmentRule(ScopeRule rule) {
        return DEPARTMENT_RULE.equals(rule.getRuleType()) && rule.getDepartmentId() != null;
    }

    private boolean scopeReferencesCodes(Map<String, Object> scope, Set<String> sourceCodes) {
        return RULES_MODE.equals(scopeMode(scope)) && departmentCodes(scope).stream().anyMatch(sourceCodes::contains);
    }

    private boolean scopeMayExpand(Map<String, Object> scope, Set<String> sourceCodes) {
        if (!Boolean.TRUE.equals(scope.get("include_descendants"))) {
            return false;
        }
        var matchingCodes = departmentCodes(scope).stream().filter(sourceCodes::contains).collect(Collectors.toSet());
        return !matchingCodes.isEmpty() && matchingCodes.size() < sourceCodes.size();
    }

    private Map<String, Object> remapScope(Map<String, Object> scope, Set<String> sourceCodes, String targetCode) {
        if (!scopeReferencesCodes(scope, sourceCodes)) {
            return null;
        }
        var remapped = new LinkedHashMap<>(scope);
        var codes = new LinkedHashSet<String>();
        for (var code : departmentCodes(scope)) {
            codes.add(sourceCodes.contains(code) ? targetCode : code);
        }
        remapped.put("department_codes", new ArrayList<>(codes));
        return remapped;
    }

    private String scopeMode(Map<String, Object> scope) {
        return scope == null || scope.get("mode") == null ? null : String.valueOf(scope.get("mode"));
    }

    private List<String> departmentCodes(Map<String, Object> scope) {
        if (scope == null || !(scope.get("department_codes") instanceof Collection<?> codes)) {
            return List.of();
        }
        return codes.stream().filter(Objects::nonNull).map(String::valueOf).toList();
    }

    private record Request(Set<UUID> sourceIds, Set<String> sourceCodes, UUID targetId,
                           String targetCode, UUID operatorId) {
    }

    private record Snapshot(List<RoleAssignment> activeAssignments,
                            List<AssignmentPermissionBoundary> accessBoundaries,
                            List<AssignmentGrantBoundary> grantBoundaries,
                            List<AuthorizationScope> scopes,
                            List<ScopeRule> scopeRules,
                            List<AuthorizationProfile> profiles,
                            List<AuthorizationProfileBoundary> profileBoundaries,
                            Map<UUID, UUID> profileIdByAssignmentId) {
    }

    private record DepartmentRuleKey(UUID departmentId, boolean includeDescendants) {
    }
}
