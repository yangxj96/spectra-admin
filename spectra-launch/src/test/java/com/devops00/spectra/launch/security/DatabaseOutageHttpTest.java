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
import org.springframework.boot.web.server.context.WebServerApplicationContext;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 使用真实隔离 PostgreSQL、本地 TCP 代理和正式 HTTP 验证数据库运行故障的就绪门。
 *
 * <p>应用数据源指向本地代理，代理转发到隔离数据库；关闭代理即制造真实数据库连接中断，
 * 不停止共享数据库实例。验证依赖故障时业务返回 503、进程保留，恢复后重新进入认证流程。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/30
 */
@EnabledIfSystemProperty(named = "spectra.test.real-deps", matches = "true")
class DatabaseOutageHttpTest {

    private static final Pattern DB_URL = Pattern.compile("^jdbc:postgresql://([^/]+)/([^?]+)(?:\\?.*)?$");

    @Test
    void databaseOutageMarksUnreadyAndRecovers() throws Exception {
        requireIsolatedDependencies();
        Matcher matcher = DB_URL.matcher(System.getenv("DB_URL"));
        assertTrue(matcher.matches(), "DB_URL 解析失败");
        String hostPort = matcher.group(1);
        String database = matcher.group(2);
        String host = hostPort;
        int databasePort = 5432;
        int separator = hostPort.lastIndexOf(':');
        if (separator > 0) {
            host = hostPort.substring(0, separator);
            databasePort = Integer.parseInt(hostPort.substring(separator + 1));
        }

        int proxyPort = freePort();
        TcpProxy proxy = new TcpProxy(proxyPort, host, databasePort);
        try {
            String proxyUrl = "jdbc:postgresql://127.0.0.1:" + proxyPort + "/" + database;
            try (var application = new SpringApplicationBuilder(LaunchApplication.class)
                    .run("--server.port=0", "--server.address=127.0.0.1", "--server.ssl.enabled=false",
                            "--spring.quartz.auto-startup=false", "--flowable.async-executor-activate=false",
                            "--spring.datasource.url=" + proxyUrl,
                            "--spectra.readiness.probe-interval-ms=1000",
                            "--spectra.readiness.max-interval-ms=2000")) {
                int port = ((WebServerApplicationContext) application).getWebServer().getPort();
                try (HttpClient client = HttpClient.newHttpClient()) {
                    assertEquals(401, businessStatus(client, port), "数据库正常时业务请求进入认证流程");

                    proxy.close();
                    assertEquals(503, awaitStatus(client, port, 503, 20), "数据库故障后业务请求应返回 503");

                    proxy = new TcpProxy(proxyPort, host, databasePort);
                    assertEquals(401, awaitStatus(client, port, 401, 30), "数据库恢复后业务请求恢复认证流程");
                }
            }
        } finally {
            proxy.close();
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

    private static int freePort() throws Exception {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    private static void requireIsolatedDependencies() {
        String url = System.getenv("DB_URL");
        if (url == null || !url.matches("^jdbc:postgresql://[^/]+/devops00_spectra_db_test(?:\\?.*)?$")) {
            throw new IllegalStateException("真实依赖测试只允许 devops00_spectra_db_test");
        }
    }

    /**
     * 将本地端口转发到目标数据库的轻量 TCP 代理；关闭即断开全部转发连接以模拟数据库不可达。
     */
    private static final class TcpProxy implements AutoCloseable {

        private final ServerSocket server;
        private final String targetHost;
        private final int targetPort;
        private final Set<Socket> sockets = ConcurrentHashMap.newKeySet();
        private volatile boolean running = true;

        private TcpProxy(int listenPort, String targetHost, int targetPort) throws IOException {
            this.targetHost = targetHost;
            this.targetPort = targetPort;
            this.server = new ServerSocket();
            this.server.setReuseAddress(true);
            this.server.bind(new InetSocketAddress("127.0.0.1", listenPort));
            Thread.ofVirtual().name("db-http-outage-proxy-accept").start(this::acceptLoop);
        }

        private void acceptLoop() {
            while (running) {
                try {
                    Socket client = server.accept();
                    sockets.add(client);
                    Thread.ofVirtual().name("db-http-outage-proxy-relay").start(() -> relay(client));
                } catch (IOException exception) {
                    return;
                }
            }
        }

        private void relay(Socket client) {
            try (client; Socket upstream = new Socket(targetHost, targetPort)) {
                sockets.add(upstream);
                Thread.ofVirtual().name("db-http-outage-proxy-pump").start(() -> pump(upstream, client));
                pump(client, upstream);
            } catch (IOException ignored) {
                // 任一端断开即结束该转发。
            } finally {
                sockets.remove(client);
            }
        }

        private static void pump(Socket from, Socket to) {
            try {
                from.getInputStream().transferTo(to.getOutputStream());
                to.shutdownOutput();
            } catch (IOException ignored) {
                // 连接关闭即结束泵送。
            }
        }

        @Override
        public void close() {
            running = false;
            closeQuietly(server);
            for (Socket socket : sockets) {
                closeQuietly(socket);
            }
            sockets.clear();
        }

        private static void closeQuietly(AutoCloseable closeable) {
            try {
                closeable.close();
            } catch (Exception ignored) {
                // 忽略关闭异常。
            }
        }
    }
}
