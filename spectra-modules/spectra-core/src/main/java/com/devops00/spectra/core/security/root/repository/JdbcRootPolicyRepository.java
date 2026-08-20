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

package com.devops00.spectra.core.security.root.repository;

import com.devops00.spectra.security.base.root.RootGovernanceException;
import com.devops00.spectra.security.base.root.RootPolicy;
import com.devops00.spectra.security.base.root.RootPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Root 策略 PostgreSQL 持久化实现。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/14
 */
@Repository
@RequiredArgsConstructor
public class JdbcRootPolicyRepository implements RootPolicyRepository {

    private static final String TABLE = "spectra_security.sec_root_policy";
    private static final String SINGLETON_KEY = "SYSTEM";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public RootPolicy lock() {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT min_effective_dev_ops_users, max_dev_ops_users, version FROM " + TABLE
                            + " WHERE policy_key = ? FOR UPDATE",
                    (resultSet, _) -> new RootPolicy(resultSet.getInt(1), resultSet.getInt(2), resultSet.getLong(3)),
                    SINGLETON_KEY);
        } catch (DataAccessException exception) {
            throw new RootGovernanceException("Root 策略不可用，拒绝执行 Root 治理操作", exception);
        }
    }

    @Override
    public void update(RootPolicy policy, long expectedVersion) {
        try {
            int updated = jdbcTemplate.update(
                    "UPDATE " + TABLE + " SET min_effective_dev_ops_users = ?, max_dev_ops_users = ?, version = version + 1"
                            + " WHERE policy_key = ? AND version = ?",
                    policy.minEffectiveDevOpsUsers(), policy.maxDevOpsUsers(), SINGLETON_KEY, expectedVersion);
            if (updated != 1) {
                throw new RootGovernanceException("Root 策略版本冲突，拒绝覆盖并发修改");
            }
        } catch (DataAccessException exception) {
            throw new RootGovernanceException("Root 策略更新失败", exception);
        }
    }

    @Override
    public long countEffectiveDevOpsUsers() {
        try {
            Long count = jdbcTemplate.queryForObject("""
                    SELECT COUNT(DISTINCT assignment.user_id)
                    FROM spectra_security.sec_role_assignment assignment
                    JOIN spectra_security.sec_role role ON role.id = assignment.role_id
                    JOIN spectra_core.sys_user user_account ON user_account.id = assignment.user_id
                    WHERE role.code = 'ROLE_DEV_OPS'
                      AND role.state = 'ACTIVE'
                      AND assignment.state = 'ACTIVE'
                      AND user_account.status = 'ACTIVE'
                      AND EXISTS (
                          SELECT 1
                          FROM spectra_security.sec_authentication_identity identity
                          WHERE identity.user_id = assignment.user_id
                            AND identity.state = 'ACTIVE'
                      )
                    """, Long.class);
            return count == null ? 0L : count;
        } catch (DataAccessException exception) {
            throw new RootGovernanceException("无法核验有效 DEV_OPS 数量，拒绝执行 Root 治理操作", exception);
        }
    }
}
