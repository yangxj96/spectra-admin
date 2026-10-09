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

package com.devops00.spectra.framework.persistence.pagination;

import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 在专用 PostgreSQL 合成库执行分页插件生成的排序 SQL。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/08
 */
@EnabledIfSystemProperty(named = "spectra.test.postgres.port", matches = "25432")
class PageFromSortPostgresTest {
    private JdbcTemplate jdbc;
    private final PaginationInnerInterceptor interceptor = new PaginationInnerInterceptor();
    private static final String QUERY = """
            SELECT d.id, t.channel FROM
            (VALUES (1, 2), (2, 1), (3, 1)) AS d(id, task_id)
            JOIN (VALUES (1, 'EMAIL'), (2, 'SMS')) AS t(id, channel) ON t.id = d.task_id
            ORDER BY d.id DESC
            """;

    @BeforeEach
    void connect() {
        jdbc = new JdbcTemplate(new DriverManagerDataSource(
                "jdbc:postgresql://127.0.0.1:25432/b02_test", "b02_test", ""));
    }

    @Test
    void joinAliasesAndDirectionsExecuteWithoutAmbiguousColumns() {
        var request = new PageFrom(15L, 1L, List.of(new PageOrderFrom("channel", true), new PageOrderFrom("id", true)));
        var page = request.toPage(Map.of("channel", "t.channel", "id", "d.id"));
        String sql = interceptor.concatOrderBy(QUERY, page.orders());
        assertEquals(List.of(2, 3, 1), jdbc.query(sql, (rs, row) -> rs.getInt("id")));
        request.setOrders(List.of(new PageOrderFrom("channel", false), new PageOrderFrom("id", false)));
        sql = interceptor.concatOrderBy(QUERY, request.toPage(Map.of("channel", "t.channel", "id", "d.id")).orders());
        assertEquals(List.of(1, 3, 2), jdbc.query(sql, (rs, row) -> rs.getInt("id")));
    }

    @Test
    void nullAndEmptyRetainDefaultAndInvalidRequestDoesNotAffectRecovery() {
        var request = new PageFrom();
        assertEquals(List.of(3, 2, 1), query(request));
        request.setOrders(List.of());
        assertEquals(List.of(3, 2, 1), query(request));
        request.setOrders(List.of(new PageOrderFrom("id;select 1", true)));
        assertThrows(IllegalArgumentException.class, () -> query(request));
        request.setOrders(List.of(new PageOrderFrom("id", true)));
        assertEquals(List.of(1, 2, 3), query(request));
    }

    private List<Integer> query(PageFrom request) {
        var page = request.toPage(Map.of("id", "d.id"));
        String sql = page.orders().isEmpty() ? QUERY : interceptor.concatOrderBy(QUERY, page.orders());
        return jdbc.query(sql, (rs, row) -> rs.getInt("id"));
    }
}
