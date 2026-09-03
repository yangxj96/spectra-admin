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

package com.devops00.spectra.core.security.policy.service.impl;

import com.devops00.spectra.common.audit.RequestCorrelationContext;
import com.devops00.spectra.common.exception.DataException;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.common.exception.EntityUpdateException;
import com.devops00.spectra.core.security.policy.javabean.from.SecurityPasswordPolicyFrom;
import com.devops00.spectra.core.security.policy.javabean.from.SecuritySessionPolicyFrom;
import com.devops00.spectra.core.security.policy.javabean.vo.SecurityPasswordPolicyVO;
import com.devops00.spectra.core.security.policy.javabean.vo.SecuritySessionPolicyVO;
import com.devops00.spectra.core.security.policy.service.SecurityPolicyService;
import com.devops00.spectra.core.security.audit.AuditResult;
import com.devops00.spectra.core.security.audit.SecurityAuditEvent;
import com.devops00.spectra.core.security.change.SecurityChangeExecutor;
import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import com.devops00.spectra.common.security.policy.PasswordPolicy;
import com.devops00.spectra.common.security.policy.SessionConcurrencyMode;
import com.devops00.spectra.common.security.policy.SessionPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/** 安全策略查询与受审计修改实现。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JdbcSecurityPolicyService implements SecurityPolicyService {

    private static final String SESSION_TABLE = "spectra_security.sec_session_policy";

    private static final String PASSWORD_TABLE = "spectra_security.sec_password_policy";

    private final JdbcTemplate jdbcTemplate;

    private final SecurityChangeExecutor securityChangeExecutor;

    private final SecurityContextAccessor securityContextAccessor;

    @Override
    public List<SecuritySessionPolicyVO> sessionPolicies() {
        return jdbcTemplate.query("""
                SELECT client.id AS client_id,
                       client.code AS client_code,
                       client.name AS client_name,
                       policy.concurrency_mode,
                       policy.allow_concurrent,
                       policy.max_sessions,
                       policy.access_ttl_seconds,
                       policy.refresh_ttl_seconds,
                       policy.absolute_ttl_seconds,
                       policy.idle_ttl_seconds,
                       policy.version
                FROM spectra_security.sec_session_policy policy
                JOIN spectra_security.sec_security_client client ON client.id = policy.client_id
                WHERE client.state = 'ACTIVE'
                ORDER BY client.code
                """,
                (resultSet, rowNumber) -> mapSessionPolicy(resultSet, rowNumber));
    }

    @Override
    @Transactional
    public SecuritySessionPolicyVO modifySessionPolicy(UUID clientId, SecuritySessionPolicyFrom from) {
        if (clientId == null || from == null) {
            throw new DataException("会话策略参数不能为空");
        }
        SecuritySessionPolicyVO before = loadSessionPolicy(clientId, true);
        requireVersion(before.version(), from.getExpectedVersion());
        SessionPolicy requested = toSessionPolicy(from);
        SecuritySessionPolicyVO after = new SecuritySessionPolicyVO(clientId, before.clientCode(), before.clientName(),
                requested.concurrencyMode().name(), from.getAllowConcurrent(), from.getMaxSessions(),
                from.getAccessTtlSeconds(), from.getRefreshTtlSeconds(), from.getAbsoluteTtlSeconds(),
                from.getIdleTtlSeconds(), before.version() + 1);
        var event = auditEvent("SESSION_POLICY_CHANGED", clientId, snapshot(before), snapshot(after));
        return securityChangeExecutor.execute(event, () -> {
            int updated = jdbcTemplate.update("""
                    UPDATE spectra_security.sec_session_policy
                    SET concurrency_mode = ?, allow_concurrent = ?, max_sessions = ?,
                        access_ttl_seconds = ?, refresh_ttl_seconds = ?, absolute_ttl_seconds = ?,
                        idle_ttl_seconds = ?, version = version + 1
                    WHERE client_id = ? AND version = ?
                    """,
                    requested.concurrencyMode().name(), from.getAllowConcurrent(), from.getMaxSessions(),
                    from.getAccessTtlSeconds(), from.getRefreshTtlSeconds(), from.getAbsoluteTtlSeconds(),
                    from.getIdleTtlSeconds(), clientId, from.getExpectedVersion());
            if (updated != 1) {
                throw new EntityUpdateException("会话策略版本冲突，请刷新后重试");
            }
            log.info("会话策略已更新: clientId={}", clientId);
            return after;
        });
    }

    @Override
    public SecurityPasswordPolicyVO passwordPolicy() {
        return loadPasswordPolicy(false);
    }

    @Override
    @Transactional
    public SecurityPasswordPolicyVO modifyPasswordPolicy(SecurityPasswordPolicyFrom from) {
        if (from == null) {
            throw new DataException("密码策略参数不能为空");
        }
        SecurityPasswordPolicyVO before = loadPasswordPolicy(true);
        requireVersion(before.version(), from.getExpectedVersion());
        PasswordPolicy requested;
        try {
            requested = new PasswordPolicy(from.getMinLength(), from.getRequireUppercase(), from.getRequireLowercase(),
                    from.getRequireDigit(), from.getRequireSpecial(), from.getMaxAgeDays());
        } catch (IllegalArgumentException exception) {
            throw new DataException(exception.getMessage(), exception);
        }
        SecurityPasswordPolicyVO after = new SecurityPasswordPolicyVO("SYSTEM", requested.minLength(),
                requested.requireUppercase(), requested.requireLowercase(), requested.requireDigit(),
                requested.requireSpecial(), requested.maxAgeDays(), before.version() + 1);
        var event = auditEvent("PASSWORD_POLICY_CHANGED", null, snapshot(before), snapshot(after));
        return securityChangeExecutor.execute(event, () -> {
            int updated = jdbcTemplate.update("""
                    UPDATE spectra_security.sec_password_policy
                    SET min_length = ?, require_uppercase = ?, require_lowercase = ?, require_digit = ?,
                        require_special = ?, max_age_days = ?, version = version + 1
                    WHERE policy_key = 'SYSTEM' AND version = ?
                    """,
                    requested.minLength(), requested.requireUppercase(), requested.requireLowercase(),
                    requested.requireDigit(), requested.requireSpecial(), requested.maxAgeDays(), from.getExpectedVersion());
            if (updated != 1) {
                throw new EntityUpdateException("密码策略版本冲突，请刷新后重试");
            }
            log.info("系统密码策略已更新");
            return after;
        });
    }

    /**
     * 查询或获取目标数据（{@code loadSessionPolicy}）。
     */
    private SecuritySessionPolicyVO loadSessionPolicy(UUID clientId, boolean lock) {
        String suffix = lock ? " FOR UPDATE" : "";
        List<SecuritySessionPolicyVO> policies = jdbcTemplate.query("""
                SELECT client.id AS client_id,
                       client.code AS client_code,
                       client.name AS client_name,
                       policy.concurrency_mode,
                       policy.allow_concurrent,
                       policy.max_sessions,
                       policy.access_ttl_seconds,
                       policy.refresh_ttl_seconds,
                       policy.absolute_ttl_seconds,
                       policy.idle_ttl_seconds,
                       policy.version
                FROM spectra_security.sec_session_policy policy
                JOIN spectra_security.sec_security_client client ON client.id = policy.client_id
                WHERE client.id = ? AND client.state = 'ACTIVE'
                """ + suffix, (resultSet, rowNumber) -> mapSessionPolicy(resultSet, rowNumber), clientId);
        if (policies.isEmpty()) {
            throw new DataNotExistException("会话策略不存在或客户端未启用");
        }
        return policies.getFirst();
    }

    /**
     * 查询或获取目标数据（{@code loadPasswordPolicy}）。
     */
    private SecurityPasswordPolicyVO loadPasswordPolicy(boolean lock) {
        String suffix = lock ? " FOR UPDATE" : "";
        List<SecurityPasswordPolicyVO> policies = jdbcTemplate.query("""
                SELECT policy_key, min_length, require_uppercase, require_lowercase,
                       require_digit, require_special, max_age_days, version
                FROM spectra_security.sec_password_policy
                WHERE policy_key = 'SYSTEM'
                """ + suffix,
                (resultSet, _) -> new SecurityPasswordPolicyVO(
                        resultSet.getString("policy_key"),
                        resultSet.getInt("min_length"),
                        resultSet.getBoolean("require_uppercase"),
                        resultSet.getBoolean("require_lowercase"),
                        resultSet.getBoolean("require_digit"),
                        resultSet.getBoolean("require_special"),
                        resultSet.getObject("max_age_days", Integer.class),
                        resultSet.getLong("version")));
        if (policies.isEmpty()) {
            throw new DataNotExistException("系统密码策略不存在");
        }
        return policies.getFirst();
    }

    /**
     * 转换、解析或规范化数据（{@code mapSessionPolicy}）。
     */
    private static SecuritySessionPolicyVO mapSessionPolicy(ResultSet resultSet, int ignored)
            throws SQLException {
        return new SecuritySessionPolicyVO(
                resultSet.getObject("client_id", UUID.class),
                resultSet.getString("client_code"),
                resultSet.getString("client_name"),
                resultSet.getString("concurrency_mode"),
                resultSet.getBoolean("allow_concurrent"),
                resultSet.getInt("max_sessions"),
                resultSet.getInt("access_ttl_seconds"),
                resultSet.getInt("refresh_ttl_seconds"),
                resultSet.getObject("absolute_ttl_seconds", Integer.class),
                resultSet.getObject("idle_ttl_seconds", Integer.class),
                resultSet.getLong("version"));
    }

    /**
     * 转换、解析或规范化数据（{@code toSessionPolicy}）。
     */
    private static SessionPolicy toSessionPolicy(SecuritySessionPolicyFrom from) {
        try {
            return new SessionPolicy(SessionConcurrencyMode.valueOf(from.getConcurrencyMode().trim().toUpperCase(Locale.ROOT)),
                    from.getMaxSessions(), from.getAccessTtlSeconds(), from.getRefreshTtlSeconds(),
                    toLong(from.getAbsoluteTtlSeconds()), toLong(from.getIdleTtlSeconds()));
        } catch (IllegalArgumentException exception) {
            throw new DataException("会话策略参数无效", exception);
        }
    }

    /**
     * 转换、解析或规范化数据（{@code toLong}）。
     */
    private static Long toLong(Integer value) {
        return value == null ? null : value.longValue();
    }

    /**
     * 校验并确保数据满足当前约束（{@code requireVersion}）。
     */
    private static void requireVersion(Long current, Long expected) {
        if (expected == null || !expected.equals(current)) {
            throw new EntityUpdateException("安全策略版本冲突，请刷新后重试");
        }
    }

    /**
     * 处理内部业务逻辑（{@code auditEvent}）。
     */
    private SecurityAuditEvent auditEvent(String type, UUID targetId, Map<String, Object> before,
                                          Map<String, Object> after) {
        return new SecurityAuditEvent(null, type, securityContextAccessor.currentUserId(), targetId, null, null, null,
                before, after, "安全策略配置变更", null, AuditResult.STARTED,
                RequestCorrelationContext.current().correlationId());
    }

    /**
     * 处理内部业务逻辑（{@code snapshot}）。
     */
    private static Map<String, Object> snapshot(SecuritySessionPolicyVO value) {
        var snapshot = new LinkedHashMap<String, Object>();
        snapshot.put("clientId", value.clientId());
        snapshot.put("clientCode", value.clientCode());
        snapshot.put("concurrencyMode", value.concurrencyMode());
        snapshot.put("allowConcurrent", value.allowConcurrent());
        snapshot.put("maxSessions", value.maxSessions());
        snapshot.put("accessTtlSeconds", value.accessTtlSeconds());
        snapshot.put("refreshTtlSeconds", value.refreshTtlSeconds());
        snapshot.put("absoluteTtlSeconds", value.absoluteTtlSeconds());
        snapshot.put("idleTtlSeconds", value.idleTtlSeconds());
        snapshot.put("version", value.version());
        return snapshot;
    }

    /**
     * 处理内部业务逻辑（{@code snapshot}）。
     */
    private static Map<String, Object> snapshot(SecurityPasswordPolicyVO value) {
        var snapshot = new LinkedHashMap<String, Object>();
        snapshot.put("policyKey", value.policyKey());
        snapshot.put("minLength", value.minLength());
        snapshot.put("requireUppercase", value.requireUppercase());
        snapshot.put("requireLowercase", value.requireLowercase());
        snapshot.put("requireDigit", value.requireDigit());
        snapshot.put("requireSpecial", value.requireSpecial());
        snapshot.put("maxAgeDays", value.maxAgeDays());
        snapshot.put("version", value.version());
        return snapshot;
    }
}
