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

package com.devops00.spectra.core.security.root;

import com.devops00.spectra.core.security.root.repository.JdbcRootPolicyRepository;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Root 策略读取无法确认时必须拒绝人数变更。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/11
 */
class JdbcRootPolicyRepositoryTest {

    @Test
    void unknownEffectiveCountCannotBecomeZero() {
        JdbcTemplate jdbc = mock();
        when(jdbc.queryForObject(anyString(), eq(Long.class))).thenReturn(1L, null);

        assertThrows(RootGovernanceException.class,
                () -> new JdbcRootPolicyRepository(jdbc).countEffectiveDevOpsUsers());
    }

    @Test
    void unknownLockedPolicyCannotBecomeASnapshot() {
        JdbcTemplate jdbc = mock();
        doReturn(null).when(jdbc).queryForObject(anyString(), any(RowMapper.class), eq("SYSTEM"));

        assertThrows(RootGovernanceException.class, () -> new JdbcRootPolicyRepository(jdbc).lock());
    }
}
