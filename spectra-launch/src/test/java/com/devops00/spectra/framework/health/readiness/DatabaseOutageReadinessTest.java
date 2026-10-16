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

package com.devops00.spectra.framework.health.readiness;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.quartz.Scheduler;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 使用真实隔离 PostgreSQL 与本地 TCP 代理验证数据库运行故障触发未就绪、恢复后回到就绪。
 *
 * <p>数据源指向本地代理，代理转发到隔离数据库；关闭代理即制造真实连接中断，无需停止共享实例，
 * 也不启动 Web/Quartz/Flyway，从而聚焦探针与状态切换。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/16
 */
@EnabledIfSystemProperty(named = "spectra.test.real-deps", matches = "true")
class DatabaseOutageReadinessTest {

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
            DataSource dataSource = new DriverManagerDataSource(
                    "jdbc:postgresql://127.0.0.1:" + proxyPort + "/" + database,
                    System.getenv("DB_USERNAME"), System.getenv("DB_PASSWORD"));
            Scheduler scheduler = mock(Scheduler.class);
            DependencyReadinessState state = new DependencyReadinessState();
            var probe = new DependencyReadinessProbe(provider(dataSource), redisTemplate(), provider(scheduler),
                    state, new ReadinessProperties(1000L, 2000L));

            probe.probe();
            assertTrue(state.isReady(), "数据库正常时探针应保持就绪");

            proxy.close();
            probe.probe();
            assertFalse(state.isReady(), "数据库故障时探针应转为未就绪");
            verify(scheduler).pauseAll();

            proxy = new TcpProxy(proxyPort, host, databasePort);
            probe.probe();
            assertTrue(state.isReady(), "数据库恢复后探针应回到就绪");
            verify(scheduler).resumeAll();
        } finally {
            proxy.close();
        }
    }

    private static RedisTemplate<String, Object> redisTemplate() {
        @SuppressWarnings("unchecked")
        RedisTemplate<String, Object> template = mock(RedisTemplate.class);
        RedisConnectionFactory factory = mock(RedisConnectionFactory.class);
        RedisConnection connection = mock(RedisConnection.class);
        when(template.getConnectionFactory()).thenReturn(factory);
        when(factory.getConnection()).thenReturn(connection);
        return template;
    }

    @SuppressWarnings("unchecked")
    private static <T> ObjectProvider<T> provider(T value) {
        ObjectProvider<T> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(value);
        return provider;
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
            Thread.ofVirtual().name("db-outage-proxy-accept").start(this::acceptLoop);
        }

        private void acceptLoop() {
            while (running) {
                try {
                    Socket client = server.accept();
                    sockets.add(client);
                    Thread.ofVirtual().name("db-outage-proxy-relay").start(() -> relay(client));
                } catch (IOException exception) {
                    return;
                }
            }
        }

        private void relay(Socket client) {
            try (client; Socket upstream = new Socket(targetHost, targetPort)) {
                sockets.add(upstream);
                Thread.ofVirtual().name("db-outage-proxy-pump").start(() -> pump(upstream, client));
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
