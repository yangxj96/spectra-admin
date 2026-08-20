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

package com.devops00.spectra.core.security.policy.repository;

import com.devops00.spectra.security.base.policy.PasswordPolicy;
import com.devops00.spectra.security.base.policy.SecurityPasswordPolicyProvider;
import com.devops00.spectra.security.base.policy.SecurityPolicyUnavailableException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/** PostgreSQL 系统密码策略读取适配器。 */
@Repository
@RequiredArgsConstructor
public class JdbcSecurityPasswordPolicyProvider implements SecurityPasswordPolicyProvider {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public PasswordPolicy current() {
        try {
            return jdbcTemplate.queryForObject("""
                    SELECT min_length, require_uppercase, require_lowercase,
                           require_digit, require_special, max_age_days
                    FROM spectra_security.sec_password_policy
                    WHERE policy_key = 'SYSTEM'
                    """,
                    (resultSet, _) -> new PasswordPolicy(
                            resultSet.getInt("min_length"),
                            resultSet.getBoolean("require_uppercase"),
                            resultSet.getBoolean("require_lowercase"),
                            resultSet.getBoolean("require_digit"),
                            resultSet.getBoolean("require_special"),
                            resultSet.getObject("max_age_days", Integer.class)));
        } catch (DataAccessException | IllegalArgumentException exception) {
            throw new SecurityPolicyUnavailableException("密码策略存储不可用，拒绝修改密码", exception);
        }
    }
}
