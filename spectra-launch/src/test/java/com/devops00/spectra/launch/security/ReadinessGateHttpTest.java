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

import com.devops00.spectra.framework.health.readiness.ReadinessVerifier;
import com.devops00.spectra.launch.LaunchApplication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.server.context.WebServerApplicationContext;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 验证运行期依赖故障下进程保留、业务未就绪返回 503、依赖恢复后重新服务。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/16
 */
@EnabledIfSystemProperty(named = "spectra.test.real-deps", matches = "true")
class ReadinessGateHttpTest {

    @Test
    void dependencyOutageMarksUnreadyAndRecovers() throws Exception {
        requireIsolatedDependencies();
        String redisServer = System.getenv("SPECTRA_TEST_REDIS_SERVER");
        if (redisServer == null || redisServer.isBlank()) {
            redisServer = "D:/Develop/Platform/redis/redis-server.exe";
        }
        Path redisHome = Files.createTempDirectory(Path.of("target"), "readiness-redis-");
        int redisPort = freePort();
        String redisUrl = "redis://127.0.0.1:" + redisPort + "/5";
        Process redis = startRedis(redisServer, redisHome, redisPort);
        try {
            awaitListening(redisPort, redis);
            try (var application = new SpringApplicationBuilder(LaunchApplication.class)
                    .run("--server.port=0", "--server.address=127.0.0.1", "--server.ssl.enabled=false",
                            "--spring.quartz.auto-startup=false", "--flowable.async-executor-activate=false",
                            "--spring.data.redis.url=" + redisUrl,
                            "--spectra.readiness.probe-interval-ms=1000",
                            "--spectra.readiness.max-interval-ms=2000")) {
                int port = ((WebServerApplicationContext) application).getWebServer().getPort();
                assertTrue(application.getBeansOfType(ReadinessVerifier.class)
                        .containsKey("cacheConsistencyVerifier"), "缓存一致性校验器必须参与恢复门");
                try (HttpClient client = HttpClient.newHttpClient()) {
                    assertEquals(401, businessStatus(client, port), "依赖正常时业务请求进入认证流程");

                    stop(redis);
                    assertEquals(503, awaitStatus(client, port, 503, 12), "依赖故障后业务请求应返回 503");

                    redis = startRedis(redisServer, redisHome, redisPort);
                    awaitListening(redisPort, redis);
                    assertEquals(401, awaitStatus(client, port, 401, 20), "依赖恢复后业务请求恢复认证流程");
                }
            }
        } finally {
            if (redis != null && redis.isAlive()) {
                stop(redis);
            }
        }
    }

    private static int awaitStatus(HttpClient client, int port, int expected, int timeoutSeconds) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(timeoutSeconds);
        int last = -1;
        while (System.nanoTime() < deadline) {
            try {
                last = businessStatus(client, port);
                if (last == expected) {
                    return last;
                }
            } catch (Exception ignored) {
                // 探针切换期间连接可能短暂失败。
            }
            Thread.sleep(300);
        }
        return last;
    }

    private static int businessStatus(HttpClient client, int port) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/api/security/context"))
                .header("Api-Version", "1.0.0")
                .GET()
                .build(), HttpResponse.BodyHandlers.discarding()).statusCode();
    }

    private static Process startRedis(String redisServer, Path home, int port) throws Exception {
        return new ProcessBuilder(redisServer, "--port", Integer.toString(port), "--bind", "127.0.0.1",
                "--save", "", "--appendonly", "no", "--dir", home.toAbsolutePath().toString())
                .redirectErrorStream(true)
                .redirectOutput(home.resolve("redis.out").toFile())
                .start();
    }

    private static int freePort() throws Exception {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    private static void awaitListening(int port, Process process) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(15);
        while (System.nanoTime() < deadline) {
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress("127.0.0.1", port), 200);
                return;
            } catch (Exception ignored) {
                if (process != null && !process.isAlive()) {
                    throw new IllegalStateException("隔离 Redis 未启动，退出码 " + process.exitValue());
                }
                Thread.sleep(100);
            }
        }
        throw new IllegalStateException("隔离 Redis 未在 15 秒内监听端口 " + port);
    }

    private static void stop(Process process) throws InterruptedException {
        process.destroy();
        if (!process.waitFor(10, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            process.waitFor(10, TimeUnit.SECONDS);
        }
    }

    private static void requireIsolatedDependencies() {
        String url = System.getenv("DB_URL");
        if (url == null || !url.matches("^jdbc:postgresql://[^/]+/devops00_spectra_db_test(?:\\?.*)?$")) {
            throw new IllegalStateException("真实依赖测试只允许 devops00_spectra_db_test");
        }
    }
}
