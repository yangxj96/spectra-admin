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

package com.devops00.spectra.framework.health;

import com.devops00.spectra.common.health.DependencyHealthStatus;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** framework 技术依赖健康 contributor 回归。 */
class TechnicalHealthContributorTest {

    @Test
    void databaseContributorUsesCommonResult() throws Exception {
        var dataSource = mock(DataSource.class);
        var connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(2)).thenReturn(true);

        var result = new DataSourceHealthContributor(dataSource).check();

        assertEquals(DependencyHealthStatus.UP, result.status());
        assertEquals("DATABASE", result.dependencyType());
    }

    @Test
    void redisContributorUsesCommonResult() {
        var factory = mock(org.springframework.data.redis.connection.RedisConnectionFactory.class);
        var connection = mock(org.springframework.data.redis.connection.RedisConnection.class);
        when(factory.getConnection()).thenReturn(connection);
        when(connection.ping()).thenReturn("PONG");

        var result = new RedisHealthContributor(factory).check();

        assertEquals(DependencyHealthStatus.UP, result.status());
        assertEquals("REDIS", result.dependencyType());
    }
}
