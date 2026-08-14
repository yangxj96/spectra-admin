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

package com.devops00.spectra.core.security.policy;

import com.devops00.spectra.security.base.policy.SecurityPolicyUnavailableException;
import com.devops00.spectra.security.base.policy.SecuritySessionPolicyProvider;
import com.devops00.spectra.security.base.session.SessionConcurrencyMode;
import com.devops00.spectra.security.base.session.SessionPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/** PostgreSQL 会话策略读取适配器。 */
@Repository
@RequiredArgsConstructor
public class JdbcSecuritySessionPolicyProvider implements SecuritySessionPolicyProvider {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public SessionPolicy find(String clientCode) {
        if (clientCode == null || clientCode.isBlank()) {
            return null;
        }
        try {
            return jdbcTemplate.query("""
                            SELECT policy.concurrency_mode,
                                   policy.max_sessions,
                                   policy.access_ttl_seconds,
                                   policy.refresh_ttl_seconds,
                                   policy.absolute_ttl_seconds,
                                   policy.idle_ttl_seconds
                            FROM spectra_security.session_policy policy
                            JOIN spectra_security.security_client client ON client.id = policy.client_id
                            WHERE client.code = ? AND client.state = 'ACTIVE'
                            """,
                    (resultSet, _) -> new SessionPolicy(
                            SessionConcurrencyMode.valueOf(resultSet.getString("concurrency_mode")),
                            resultSet.getInt("max_sessions"),
                            resultSet.getLong("access_ttl_seconds"),
                            resultSet.getLong("refresh_ttl_seconds"),
                            resultSet.getObject("absolute_ttl_seconds", Long.class),
                            resultSet.getObject("idle_ttl_seconds", Long.class)),
                    clientCode.trim()).stream().findFirst().orElse(null);
        } catch (DataAccessException | IllegalArgumentException exception) {
            throw new SecurityPolicyUnavailableException("会话策略存储不可用，拒绝创建或刷新会话", exception);
        }
    }
}
