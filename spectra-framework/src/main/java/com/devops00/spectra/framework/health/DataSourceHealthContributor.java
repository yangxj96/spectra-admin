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

package com.devops00.spectra.framework.health;

import com.devops00.spectra.common.health.DependencyHealthContributor;
import com.devops00.spectra.common.health.DependencyHealthResult;
import com.devops00.spectra.common.health.DependencyHealthStatus;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import javax.sql.DataSource;

/**
 * framework 提供的 PostgreSQL 技术依赖健康检查。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@Component("databaseHealthContributor")
@ConditionalOnBean(DataSource.class)
public class DataSourceHealthContributor implements DependencyHealthContributor {

    private static final Duration TIMEOUT = Duration.ofSeconds(3);

    private final DataSource dataSource;

    public DataSourceHealthContributor(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 读取 contributorName 对应的依赖健康状态。
     *
     * @return 返回固定名称 {@code PostgreSQL}，用于健康快照中标识数据库依赖；结果始终为非空字符串。
     */
    @Override
    public String contributorName() {
        return "PostgreSQL";
    }

    /**
     * 读取 moduleName 对应的依赖健康状态。
     *
     * @return 返回所属模块名称 {@code framework}；结果始终为非空字符串。
     */
    @Override
    public String moduleName() {
        return "framework";
    }

    /**
     * 读取 dependencyType 对应的依赖健康状态。
     *
     * @return 返回依赖类型 {@code DATABASE}；结果始终为非空字符串。
     */
    @Override
    public String dependencyType() {
        return "DATABASE";
    }

    /**
     * 读取 timeout 对应的依赖健康状态。
     *
     * @return 返回数据库健康检查超时时间 3 秒；结果始终为非 null 的固定 Duration。
     */
    @Override
    public Duration timeout() {
        return TIMEOUT;
    }

    /**
     * 读取 check 对应的依赖健康状态。
     *
     * @return 返回数据库连接检查结果；连接有效时状态为 UP，连接无效或检查异常时为 DOWN，结果对象始终非 null。
     */
    @Override
    public DependencyHealthResult check() {
        var start = System.nanoTime();
        try (var connection = dataSource.getConnection()) {
            var available = connection.isValid(2);
            return result(available ? DependencyHealthStatus.UP : DependencyHealthStatus.DOWN,
                    start, available ? null : "DATABASE_UNAVAILABLE",
                    available ? "数据库连接检查正常" : "数据库连接不可用");
        } catch (SQLException | RuntimeException exception) {
            return result(DependencyHealthStatus.DOWN, start, "DATABASE_CHECK_FAILED", "数据库连接检查失败");
        }
    }

    /**
     * 处理结果相关数据。
     */
    private DependencyHealthResult result(DependencyHealthStatus status, long start, String errorCode,
                                          String safeSummary) {
        return new DependencyHealthResult(contributorName(), moduleName(), dependencyType(), status,
                Duration.ofNanos(System.nanoTime() - start), Instant.now(), errorCode, safeSummary);
    }
}
