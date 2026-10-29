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

package com.devops00.spectra.launch.security;

import com.devops00.spectra.launch.LaunchApplication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.net.ServerSocket;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 验证必需数据库不可达时应用启动失败。
 */
@EnabledIfSystemProperty(named = "spectra.test.real-deps", matches = "true")
class DatabaseStartupFailClosedTest {

    @Test
    void unreachableDatabaseFailsStartup() throws Exception {
        requireIsolatedDependencies();
        int closedPort;
        try (ServerSocket socket = new ServerSocket(0)) {
            closedPort = socket.getLocalPort();
        }
        String databaseUrl = "jdbc:postgresql://127.0.0.1:" + closedPort + "/devops00_spectra_db_test";
        assertThrows(RuntimeException.class, () -> {
            try (var application = new SpringApplicationBuilder(LaunchApplication.class)
                    .run("--server.port=0", "--server.address=127.0.0.1", "--server.ssl.enabled=false",
                            "--spring.quartz.auto-startup=false", "--flowable.async-executor-activate=false",
                            "--spring.datasource.url=" + databaseUrl,
                            "--spring.datasource.hikari.connection-timeout=1000")) {
                application.getBean(LaunchApplication.class);
            }
        });
    }

    private static void requireIsolatedDependencies() {
        String url = System.getenv("DB_URL");
        if (url == null || !url.matches("^jdbc:postgresql://[^/]+/devops00_spectra_db_test(?:\\?.*)?$")) {
            throw new IllegalStateException("真实依赖测试只允许 devops00_spectra_db_test");
        }
    }
}
