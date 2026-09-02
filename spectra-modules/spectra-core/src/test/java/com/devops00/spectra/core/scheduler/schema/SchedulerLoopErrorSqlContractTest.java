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

package com.devops00.spectra.core.scheduler.schema;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.plugins.InterceptorIgnoreHelper;
import com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor;
import com.devops00.spectra.core.scheduler.mapper.SchedulerLoopErrorMapper;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.RowBounds;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 调度循环错误聚合 SQL 与数据权限拦截器的结构契约测试。 */
class SchedulerLoopErrorSqlContractTest {

    @Test
    void shouldBypassDataPermissionParserForReturningUpsert() throws Exception {
        var method = SchedulerLoopErrorMapper.class.getMethod("upsertOccurrence",
                com.devops00.spectra.core.scheduler.javabean.entity.SchedulerLoopErrorEntity.class,
                long.class);
        var interceptorIgnore = method.getAnnotation(InterceptorIgnore.class);
        var mappedStatementId = SchedulerLoopErrorMapper.class.getName() + ".upsertOccurrence";
        var classIgnoreStrategy = InterceptorIgnoreHelper.initSqlParserInfoCache(SchedulerLoopErrorMapper.class);
        InterceptorIgnoreHelper.initSqlParserInfoCache(classIgnoreStrategy, SchedulerLoopErrorMapper.class.getName(), method);
        var ignoreStrategy = InterceptorIgnoreHelper.getIgnoreStrategy(mappedStatementId);

        assertNotNull(interceptorIgnore, "RETURNING 聚合写入必须跳过数据权限 SQL 重写");
        assertEquals("true", interceptorIgnore.dataPermission());
        assertNotNull(ignoreStrategy, "MyBatis-Plus 必须识别聚合写入方法的拦截器忽略策略");
        assertEquals(Boolean.TRUE, ignoreStrategy.getDataPermission());

        var originalSql = "INSERT INTO spectra_core.scheduler_loop_error (id) VALUES (?) RETURNING *";
        var configuration = new Configuration();
        var mappedStatement = new MappedStatement.Builder(
                configuration, mappedStatementId, new StaticSqlSource(configuration, originalSql), SqlCommandType.SELECT)
                .build();
        var boundSql = mappedStatement.getBoundSql(null);

        new DataPermissionInterceptor().beforeQuery(null, mappedStatement, null, RowBounds.DEFAULT, null, boundSql);

        assertEquals(originalSql, boundSql.getSql(), "RETURNING 写入不得进入数据权限 SQL 重写");
    }

    @Test
    void shouldMarkReturningUpsertAsAffectingData() throws IOException {
        var mapper = readResource();
        var statementStart = mapper.indexOf("<select id=\"upsertOccurrence\"");
        var statementEnd = mapper.indexOf('>', statementStart);

        assertTrue(statementStart >= 0, "错误聚合 RETURNING SQL 必须保留结果映射查询声明");
        assertTrue(statementEnd > statementStart, "错误聚合 Mapper 声明不完整");
        var statement = mapper.substring(statementStart, statementEnd);
        assertTrue(statement.contains("resultMap=\"BaseResultMap\""));
        assertTrue(statement.contains("affectData=\"true\""),
                "INSERT ... RETURNING 必须标记为会影响数据的查询");
    }

    private String readResource() throws IOException {
        try (var resource = getClass().getClassLoader()
                .getResourceAsStream("mapper/scheduler/SchedulerLoopErrorMapper.xml")) {
            if (resource == null) {
                throw new IOException("找不到调度循环错误 Mapper 资源");
            }
            return new String(resource.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
