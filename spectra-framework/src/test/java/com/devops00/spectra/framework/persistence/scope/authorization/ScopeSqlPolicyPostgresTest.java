/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.devops00.spectra.framework.persistence.scope.authorization;

import com.devops00.spectra.common.annotation.DataScope;
import com.devops00.spectra.common.security.authorization.AuthorizationScope;
import com.devops00.spectra.common.security.authorization.PermissionBoundary;
import com.devops00.spectra.common.security.authorization.ScopeMode;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.schema.Table;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 在专用 PostgreSQL 合成库验证关系范围 SQL 的外层表别名。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/11
 */
@EnabledIfSystemProperty(named = "spectra.test.real-deps", matches = "true")
class ScopeSqlPolicyPostgresTest {

    private static final DataScope DATA_SCOPE = RelationScopedRecord.class.getAnnotation(DataScope.class);

    private JdbcTemplate jdbc;

    @BeforeEach
    void connect() {
        String url = System.getenv("DB_URL");
        if (url == null || !url.matches("^jdbc:postgresql://[^/]+/devops00_spectra_db_test(?:\\?.*)?$")) {
            throw new IllegalStateException("真实依赖测试只允许 devops00_spectra_db_test");
        }
        String username = System.getenv("DB_USERNAME");
        if (username == null || username.isBlank()) {
            throw new IllegalStateException("真实依赖测试缺少 DB_USERNAME");
        }
        jdbc = new JdbcTemplate(new DriverManagerDataSource(url, username, System.getenv("DB_PASSWORD")));
    }

    @Test
    void relationScopeUsesOuterAliasWhenJoinedTablesShareTheMainColumn() {
        UUID subject = UUID.randomUUID();
        UUID other = UUID.randomUUID();
        Table table = new Table("record");
        table.setAlias(new Alias("m"));
        var boundary = new PermissionBoundary("record:read", new AuthorizationScope(ScopeMode.SELF, Set.of(), false));
        var expression = ScopeSqlPolicy.build(table, DATA_SCOPE, List.of(boundary), subject);

        assertNotNull(expression);
        assertTrue(expression.toString().contains("m.id IN ("));
        String sql = """
                WITH bc_base008_rel(id, record_id, user_id) AS (
                    VALUES (11, 1, '%s'), (12, 2, '%s')
                )
                SELECT m.id FROM (VALUES (1), (2)) AS m(id)
                JOIN (VALUES (1), (2)) AS t(id) ON t.id = m.id
                WHERE %s ORDER BY m.id
                """.formatted(subject, other, expression);

        assertEquals(List.of(1), jdbc.query(sql, (rs, row) -> rs.getInt(1)));
    }

    @DataScope(readPermission = "record:read", column = "", ownerColumn = "", relations = {
            @DataScope.Relation(table = "bc_base008_rel", joinColumn = "record_id")})
    private static class RelationScopedRecord {
    }
}
