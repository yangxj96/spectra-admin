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

package com.devops00.spectra.core;

import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 行政区划导入的目标数据库守卫。
 *
 * <p>导入会向目标库写入约 66.5 万行区域数据，因此在建立任何连接之前，必须确认实际数据源数据库名在
 * {@code SPECTRA_REGION_IMPORT_ALLOWED_DATABASES} 显式声明的隔离库允许列表中；未声明或不允许一律拒绝。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/16
 */
final class RegionImportDatabaseGuard {

    private final String database;

    RegionImportDatabaseGuard(Environment environment) {
        String url = environment.getProperty("spring.datasource.url");
        if (url == null || url.isBlank()) {
            url = System.getenv("DB_URL");
        }
        this.database = validate(url, System.getenv("SPECTRA_REGION_IMPORT_ALLOWED_DATABASES"));
    }

    String database() {
        return database;
    }

    static String validate(String url, String allowedConfigured) {
        if (url == null || url.isBlank()) {
            throw new IllegalStateException("行政区划导入缺少数据源 URL，拒绝导入。");
        }
        String database = extractDatabase(url);
        Set<String> allowed = allowedDatabases(allowedConfigured);
        if (allowed.isEmpty()) {
            throw new IllegalStateException("SPECTRA_REGION_IMPORT_ALLOWED_DATABASES 未配置，拒绝导入非隔离库。");
        }
        if (!allowed.contains(database)) {
            throw new IllegalStateException("拒绝导入未被允许的数据库: " + database);
        }
        return database;
    }

    static String extractDatabase(String url) {
        String withoutParameters = url.split("\\?", 2)[0];
        int slash = withoutParameters.lastIndexOf('/');
        if (slash < 0 || slash == withoutParameters.length() - 1) {
            throw new IllegalStateException("无法从数据源 URL 解析数据库名: " + url);
        }
        return withoutParameters.substring(slash + 1);
    }

    static Set<String> allowedDatabases(String configured) {
        if (configured == null || configured.isBlank()) {
            return Set.of();
        }
        Set<String> allowed = new LinkedHashSet<>();
        for (String item : Arrays.asList(configured.split(","))) {
            String trimmed = item.trim();
            if (!trimmed.isEmpty()) {
                allowed.add(trimmed);
            }
        }
        return allowed;
    }
}
