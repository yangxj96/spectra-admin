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

package com.devops00.spectra.core.user.imports.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.devops00.spectra.common.exception.DataException;
import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import com.devops00.spectra.common.utils.SHA256Utils;
import com.devops00.spectra.core.security.authorization.constant.SecurityAuthorizationState;
import com.devops00.spectra.core.security.authorization.javabean.vo.AuthorizationProfileVO;
import com.devops00.spectra.core.security.authorization.service.AuthorizationProfileService;
import com.devops00.spectra.core.security.authentication.service.AuthenticationIdentityService;
import com.devops00.spectra.core.system.javabean.entity.Department;
import com.devops00.spectra.core.system.service.DepartmentService;
import com.devops00.spectra.core.system.service.DictService;
import com.devops00.spectra.core.user.imports.entity.UserImportRow;
import com.devops00.spectra.core.user.imports.entity.UserImportTask;
import com.devops00.spectra.core.user.imports.javabean.enums.UserImportRowState;
import com.devops00.spectra.core.user.imports.javabean.enums.UserImportTaskStatus;
import com.devops00.spectra.core.user.imports.javabean.from.UserImportApplyFrom;
import com.devops00.spectra.core.user.imports.javabean.from.UserImportPreviewFrom;
import com.devops00.spectra.core.user.imports.javabean.from.UserImportRowFrom;
import com.devops00.spectra.core.user.imports.javabean.vo.UserImportTaskVO;
import com.devops00.spectra.core.user.imports.mapper.UserImportRowMapper;
import com.devops00.spectra.core.user.imports.mapper.UserImportTaskMapper;
import com.devops00.spectra.core.user.javabean.entity.User;
import com.devops00.spectra.core.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/** 用户导入 Preview 用例，负责规范化、引用数据校验和幂等 Preview 状态。 */
@Service
@RequiredArgsConstructor
public class UserImportPreviewService {

    private static final String STATUS_PREVIEWED = UserImportTaskStatus.PREVIEWED.name();

    private static final String STATUS_VALIDATING = UserImportTaskStatus.VALIDATING.name();

    private static final String STATUS_EXPIRED = UserImportTaskStatus.EXPIRED.name();

    private static final String STATE_VALID = UserImportRowState.VALID.name();

    private static final String STATE_ERROR = UserImportRowState.ERROR.name();

    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9][0-9 .()\\-]{5,38}$");

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final UserImportTaskMapper taskMapper;

    private final UserImportRowMapper rowMapper;

    private final UserMapper userMapper;

    private final AuthenticationIdentityService authenticationIdentityService;

    private final DepartmentService departmentService;

    private final DictService dictService;

    private final AuthorizationProfileService profileService;

    private final SecurityContextAccessor securityContextAccessor;

    private final UserImportResultService resultService;

    private final SecureRandom secureRandom = new SecureRandom();

    /** 创建或复用一项用户导入 Preview。 */
    @Transactional
    public UserImportTaskVO preview(UserImportPreviewFrom params) {
        var operatorId = currentOperatorId();
        var rows = normalizeRows(params.getRows(), params.getIdempotencyKey());
        var requestHash = requestHash(params.getFileHash(), params.isSkipExisting(), rows);
        var existing = taskMapper.selectOne(new LambdaQueryWrapper<UserImportTask>()
                .eq(UserImportTask::getOperatorId, operatorId)
                .eq(UserImportTask::getIdempotencyKey, params.getIdempotencyKey().trim()));
        if (existing != null) {
            if (!MessageDigest.isEqual(existing.getRequestHash().getBytes(StandardCharsets.UTF_8),
                    requestHash.getBytes(StandardCharsets.UTF_8))) {
                throw new DataException("相同幂等键对应的导入请求已变化");
            }
            return replayPreview(existing);
        }

        var referenceData = loadReferenceData();
        var task = new UserImportTask();
        task.setOperatorId(operatorId);
        task.setIdempotencyKey(params.getIdempotencyKey().trim());
        task.setFileName(params.getFileName().trim());
        task.setFileHash(params.getFileHash().trim());
        task.setSkipExisting(params.isSkipExisting());
        task.setStatus(STATUS_VALIDATING);
        task.setRequestHash(requestHash);
        task.setProfileVersionHash(profileVersionHash(rows, referenceData.profiles()));
        task.setExpiresAt(Instant.now().plusSeconds(24 * 60 * 60));
        task.setTotalRows(rows.size());
        if (taskMapper.insert(task) != 1) {
            throw new DataException("创建用户导入任务失败");
        }

        var usernames = new HashSet<String>();
        var emails = new HashSet<String>();
        var phones = new HashSet<String>();
        var validRows = 0;
        var errorRows = 0;
        var skippedRows = 0;
        var assignmentCount = 0;
        var accessBoundaryCount = 0;
        var grantBoundaryCount = 0;
        for (int index = 0; index < rows.size(); index++) {
            var normalized = rows.get(index);
            var row = new UserImportRow();
            row.setTaskId(task.getId());
            row.setRowNumber(index + 1);
            row.setRowKey((index + 1) + ":" + normalized.source().getEmployeeNo());
            row.setRawData(normalized.rawData());
            row.setNormalizedData(normalized.normalizedData());
            var errors = validate(normalized.source(), referenceData, params.isSkipExisting(), usernames, emails, phones);
            if (errors.isEmpty()) {
                row.setState(STATE_VALID);
                validRows++;
                var existingUser = findExisting(normalized.source());
                if (existingUser != null && params.isSkipExisting()) {
                    row.setUserId(existingUser.getId());
                    skippedRows++;
                } else {
                    var profile = referenceData.profiles().get(normalized.source().getAuthorizationProfileCode());
                    if (profile != null) {
                        assignmentCount += profile.getAssignments().size();
                        accessBoundaryCount += profile.getAssignments()
                                .stream()
                                .mapToInt(assignment -> assignment.getBoundaries().size())
                                .sum();
                        grantBoundaryCount += profile.getAssignments()
                                .stream()
                                .flatMap(assignment -> assignment.getBoundaries().stream())
                                .filter(boundary -> boundary.getGrant() != null)
                                .mapToInt(boundary -> 1)
                                .sum();
                    }
                }
            } else {
                row.setState(STATE_ERROR);
                row.setErrors(Map.of("validation", errors));
                errorRows++;
            }
            if (rowMapper.insert(row) != 1) {
                throw new DataException("保存用户导入暂存行失败: " + row.getRowNumber());
            }
        }
        task.setStatus(STATUS_PREVIEWED);
        task.setValidRows(validRows);
        task.setErrorRows(errorRows);
        task.setSkippedRows(skippedRows);
        task.setAssignmentCount(assignmentCount);
        task.setAccessBoundaryCount(accessBoundaryCount);
        task.setGrantBoundaryCount(grantBoundaryCount);
        var token = issuePreviewToken();
        task.setPreviewTokenHash(SHA256Utils.hash(token));
        task.setPreviewExpiresAt(Instant.now().plusSeconds(10 * 60));
        if (taskMapper.updateById(task) != 1) {
            throw new DataException("更新用户导入 Preview 状态失败");
        }
        return resultService.toVO(task, token);
    }

    /** 在 Apply 前重新校验 Preview token、引用数据版本和请求摘要。 */
    public void validateApply(UserImportTask task, UserImportApplyFrom params) {
        var now = Instant.now();
        if (task.getExpiresAt() == null || now.isAfter(task.getExpiresAt())) {
            expire(task);
            throw new DataException("用户导入任务已过期");
        }
        if (task.getPreviewExpiresAt() == null
                || now.isAfter(task.getPreviewExpiresAt())
                || task.getPreviewConsumedAt() != null
                || !MessageDigest.isEqual(SHA256Utils.hash(params.getPreviewToken()).getBytes(StandardCharsets.UTF_8),
                        task.getPreviewTokenHash().getBytes(StandardCharsets.UTF_8))) {
            throw new DataException("用户导入 Preview token 无效或已过期");
        }
        var rows = rowMapper.selectList(new LambdaQueryWrapper<UserImportRow>()
                .eq(UserImportRow::getTaskId, task.getId())
                .orderByAsc(UserImportRow::getRowNumber));
        var normalizedRows = normalizeRows(rows.stream()
                .map(UserImportRow::getNormalizedData)
                .map(this::toSource)
                .toList(), task.getIdempotencyKey());
        var referenceData = loadReferenceData();
        if (!task.getProfileVersionHash().equals(profileVersionHash(normalizedRows, referenceData.profiles()))) {
            throw new DataException("授权方案版本已变化，请重新生成导入 Preview");
        }
        if (!task.getRequestHash().equals(requestHash(task.getFileHash(), task.isSkipExisting(), normalizedRows))) {
            throw new DataException("导入请求已变化，请重新生成导入 Preview");
        }
    }

    /** 提供给异步执行块的当前引用数据快照。 */
    public ReferenceData loadReferenceData() {
        var departmentIds = departmentService.list()
                .stream()
                .filter(department -> department.getCode() != null)
                .collect(Collectors.toMap(Department::getCode, Department::getId, (left, right) -> left));
        var languages = dictService.listDictDataByGroupCode("sys_language")
                .stream()
                .map(item -> item.getValue())
                .filter(value -> value != null)
                .collect(Collectors.toUnmodifiableSet());
        var timezones = dictService.listDictDataByGroupCode("sys_timezone")
                .stream()
                .map(item -> item.getValue())
                .filter(value -> value != null)
                .collect(Collectors.toUnmodifiableSet());
        var profiles = profileService.all()
                .stream()
                .filter(profile -> profile.getCode() != null)
                .collect(Collectors.toMap(AuthorizationProfileVO::getCode, Function.identity(), (left, right) -> left,
                        LinkedHashMap::new));
        return new ReferenceData(departmentIds, languages, timezones, profiles);
    }

    /** 将 Preview 任务标记为过期并清理一次性 token。 */
    public void expire(UserImportTask task) {
        task.setStatus(STATUS_EXPIRED);
        task.setPreviewTokenHash(null);
        task.setPreviewExpiresAt(null);
        taskMapper.updateById(task);
    }

    private UserImportTaskVO replayPreview(UserImportTask task) {
        if (!STATUS_PREVIEWED.equals(task.getStatus())) {
            return resultService.toVO(task);
        }
        if (task.getExpiresAt() == null || Instant.now().isAfter(task.getExpiresAt())) {
            expire(task);
            throw new DataException("用户导入任务已过期，请使用新的幂等键");
        }
        var token = issuePreviewToken();
        task.setPreviewTokenHash(SHA256Utils.hash(token));
        task.setPreviewExpiresAt(Instant.now().plusSeconds(10 * 60));
        task.setPreviewConsumedAt(null);
        taskMapper.updateById(task);
        return resultService.toVO(task, token);
    }

    private List<String> validate(UserImportRowFrom source, ReferenceData referenceData, boolean skipExisting,
                                  Set<String> usernames, Set<String> emails, Set<String> phones) {
        var errors = new ArrayList<String>();
        if (blank(source.getRealName())) {
            errors.add("姓名不能为空");
        }
        if (blank(source.getUsername())) {
            errors.add("登录用户名不能为空");
        } else if (!usernames.add(source.getUsername().toLowerCase(Locale.ROOT))) {
            errors.add("登录用户名在导入文件中重复");
        }
        if (blank(source.getPhone())) {
            errors.add("手机号码不能为空");
        } else if (!PHONE_PATTERN.matcher(source.getPhone()).matches()) {
            errors.add("手机号码格式不正确");
        } else if (!phones.add(source.getPhone())) {
            errors.add("手机号码在导入文件中重复");
        }
        if (blank(source.getEmail())) {
            errors.add("邮箱不能为空");
        } else if (!EMAIL_PATTERN.matcher(source.getEmail()).matches()) {
            errors.add("邮箱格式不正确");
        } else if (!emails.add(source.getEmail().toLowerCase(Locale.ROOT))) {
            errors.add("邮箱在导入文件中重复");
        }
        if (!referenceData.departmentIds().containsKey(source.getDepartmentCode())) {
            errors.add("部门编码不存在");
        }
        if (!referenceData.languages().contains(source.getLanguage())) {
            errors.add("语言不在当前系统字典中");
        }
        if (!referenceData.timezones().contains(source.getTimezone())) {
            errors.add("时区不在当前系统字典中");
        }
        var profile = referenceData.profiles().get(source.getAuthorizationProfileCode());
        if (profile == null || !SecurityAuthorizationState.ACTIVE.name().equals(profile.getState())) {
            errors.add("授权方案不存在或已停用");
        }
        var existing = findExisting(source);
        if (existing != null && !skipExisting) {
            errors.add("登录用户名、邮箱或手机号码已存在");
        }
        return errors;
    }

    private List<NormalizedRow> normalizeRows(List<UserImportRowFrom> sources, String generationSeed) {
        var result = new ArrayList<NormalizedRow>(sources.size());
        for (int index = 0; index < sources.size(); index++) {
            result.add(normalize(sources.get(index), generationSeed, index));
        }
        return result;
    }

    private NormalizedRow normalize(UserImportRowFrom source, String generationSeed, int rowIndex) {
        var sourceValues = toMap(source);
        var raw = new LinkedHashMap<>(sourceValues);
        raw.remove("employee_no");
        Map<String, Object> normalized = new LinkedHashMap<>();
        for (var entry : sourceValues.entrySet()) {
            normalized.put(entry.getKey(), entry.getValue() == null ? "" : trim(String.valueOf(entry.getValue())));
        }
        var normalizedSource = toSource(normalized);
        if (blank(normalizedSource.getEmployeeNo())) {
            normalizedSource.setEmployeeNo(generateEmployeeNo(generationSeed, rowIndex));
        }
        normalized = toMap(normalizedSource);
        return new NormalizedRow(normalizedSource, raw, normalized);
    }

    private String generateEmployeeNo(String generationSeed, int rowIndex) {
        return "EMP-" + SHA256Utils.hash(trim(generationSeed) + '\u001f' + rowIndex)
                .substring(0, 32)
                .toUpperCase(Locale.ROOT);
    }

    private Map<String, Object> toMap(UserImportRowFrom source) {
        var result = new LinkedHashMap<String, Object>();
        result.put("employee_no", source.getEmployeeNo());
        result.put("real_name", source.getRealName());
        result.put("username", source.getUsername());
        result.put("phone", source.getPhone());
        result.put("email", source.getEmail());
        result.put("department_code", source.getDepartmentCode());
        result.put("language", source.getLanguage());
        result.put("timezone", source.getTimezone());
        result.put("authorization_profile_code", source.getAuthorizationProfileCode());
        return result;
    }

    private UserImportRowFrom toSource(Map<String, Object> values) {
        var source = new UserImportRowFrom();
        source.setEmployeeNo(value(values, "employee_no"));
        source.setRealName(value(values, "real_name"));
        source.setUsername(value(values, "username"));
        source.setPhone(value(values, "phone"));
        source.setEmail(value(values, "email"));
        source.setDepartmentCode(value(values, "department_code"));
        source.setLanguage(value(values, "language"));
        source.setTimezone(value(values, "timezone"));
        source.setAuthorizationProfileCode(value(values, "authorization_profile_code"));
        return source;
    }

    private String value(Map<String, Object> values, String key) {
        return values == null || values.get(key) == null ? "" : String.valueOf(values.get(key));
    }

    private String requestHash(String fileHash, boolean skipExisting, List<NormalizedRow> rows) {
        var canonical = new StringBuilder(trim(fileHash)).append('\u001f').append(skipExisting);
        for (var row : rows) {
            canonical.append('\u001e');
            toMap(row.source()).values().forEach(value -> canonical.append('\u001f').append(value == null ? "" : value));
        }
        return SHA256Utils.hash(canonical.toString());
    }

    private String profileVersionHash(List<NormalizedRow> rows, Map<String, AuthorizationProfileVO> profiles) {
        var codes = rows.stream().map(row -> row.source().getAuthorizationProfileCode()).distinct().sorted().toList();
        var canonical = codes.stream().map(code -> {
            var profile = profiles.get(code);
            if (profile == null) {
                return code + "|MISSING";
            }
            var assignments = profile.getAssignments()
                    .stream()
                    .sorted(Comparator.comparing(item -> item.getRoleCode() == null ? "" : item.getRoleCode()))
                    .map(item -> item.getRoleCode() + "@" + item.getRoleVersion())
                    .collect(Collectors.joining(","));
            return code + "|" + profile.getState() + "|" + profile.getVersion() + "|" + assignments;
        }).collect(Collectors.joining("\u001f"));
        return SHA256Utils.hash(canonical);
    }

    private String issuePreviewToken() {
        var bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private User findExisting(UserImportRowFrom source) {
        var byEmployeeNo = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmployeeNo, source.getEmployeeNo()));
        if (byEmployeeNo != null) {
            return byEmployeeNo;
        }
        var byUsername = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .apply("lower(btrim(username)) = lower({0})", source.getUsername())
                .last("LIMIT 1"));
        if (byUsername != null) {
            return byUsername;
        }
        var byEmail = source.getEmail() == null || source.getEmail().isBlank()
                ? null
                : authenticationIdentityService.findIdentity("EMAIL", source.getEmail());
        if (byEmail != null) {
            return userMapper.selectById(byEmail.getUserId());
        }
        var byPhone = source.getPhone() == null || source.getPhone().isBlank()
                ? null
                : authenticationIdentityService.findIdentity("SMS", source.getPhone());
        return byPhone == null ? null : userMapper.selectById(byPhone.getUserId());
    }

    private UUID currentOperatorId() {
        var operatorId = securityContextAccessor.currentUserId();
        if (operatorId == null) {
            throw new DataException("无法识别当前安全主体");
        }
        return operatorId;
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    /** Apply 用例共享的不可变引用数据快照。 */
    public record ReferenceData(Map<String, UUID> departmentIds, Set<String> languages, Set<String> timezones,
                                Map<String, AuthorizationProfileVO> profiles) {

        public ReferenceData {
            departmentIds = Map.copyOf(departmentIds);
            languages = Set.copyOf(languages);
            timezones = Set.copyOf(timezones);
            profiles = Map.copyOf(profiles);
        }
    }

    private record NormalizedRow(UserImportRowFrom source, Map<String, Object> rawData,
                                 Map<String, Object> normalizedData) {
    }
}
