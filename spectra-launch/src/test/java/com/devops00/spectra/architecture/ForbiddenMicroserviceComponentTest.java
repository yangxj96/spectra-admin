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

package com.devops00.spectra.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 微服务运行时组件禁止规则测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/08/31
 */
class ForbiddenMicroserviceComponentTest {

    private static final String AUTO_CONFIGURATION_IMPORTS = "META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports";

    private static final List<String> FORBIDDEN_RUNTIME_CLASSES = List.of(
            "com.alibaba.cloud.nacos.NacosDiscoveryProperties",
            "com.alibaba.nacos.api.NacosFactory",
            "org.springframework.cloud.client.discovery.DiscoveryClient",
            "org.springframework.cloud.config.client.ConfigClientProperties",
            "com.netflix.discovery.EurekaClient",
            "com.ecwid.consul.v1.ConsulClient",
            "org.apache.curator.framework.CuratorFramework",
            "org.apache.zookeeper.ZooKeeper");

    private static final List<String> FORBIDDEN_MARKERS = List.of(
            "nacos",
            "spring.cloud",
            "config-server",
            "eureka",
            "consul",
            "zookeeper",
            "service-registry",
            "discoveryclient");

    @Test
    void rootPomMustDeclareForbiddenDependencyRule() throws IOException {
        String rootPom = Files.readString(rootPomPath());

        assertThat(rootPom)
                .as("根 POM 必须声明微服务组件禁止依赖规则")
                .contains("<artifactId>maven-enforcer-plugin</artifactId>")
                .contains("<bannedDependencies>")
                .contains("com.alibaba.cloud")
                .contains("org.springframework.cloud")
                .contains("com.netflix.eureka")
                .contains("com.ecwid.consul")
                .contains("org.apache.curator");
    }

    @Test
    void launchRuntimeClasspathMustNotContainForbiddenMicroserviceClients() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        List<String> presentClasses = FORBIDDEN_RUNTIME_CLASSES.stream()
                .filter(className -> classLoader.getResource(classResourceName(className)) != null)
                .toList();

        assertThat(presentClasses)
                .as("launch 运行时 classpath 不得包含微服务客户端")
                .isEmpty();
    }

    @Test
    void applicationConfigMustNotEnableForbiddenMicroservicePrefixes() throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        List<String> violations = new ArrayList<>();
        for (String resourceName : List.of("application-dev.yml", "application-prod.yml")) {
            try (var stream = classLoader.getResourceAsStream(resourceName)) {
                assertThat(stream)
                        .as("必须能读取应用配置资源：" + resourceName)
                        .isNotNull();
                if (stream == null) {
                    continue;
                }
                String content = new String(stream.readAllBytes(), StandardCharsets.UTF_8).toLowerCase(Locale.ROOT);
                for (String marker : FORBIDDEN_MARKERS) {
                    if (content.contains(marker)) {
                        violations.add(resourceName + ": " + marker);
                    }
                }
            }
        }

        assertThat(violations)
                .as("application 配置不得启用微服务组件或远程配置前缀")
                .isEmpty();
    }

    @Test
    void autoConfigurationImportsMustNotRegisterForbiddenMicroserviceClients() throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        List<String> violations = new ArrayList<>();
        Enumeration<URL> resources = classLoader.getResources(AUTO_CONFIGURATION_IMPORTS);
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            String content;
            try (var stream = resource.openStream()) {
                content = new String(stream.readAllBytes(), StandardCharsets.UTF_8).toLowerCase(Locale.ROOT);
            }
            for (String marker : FORBIDDEN_MARKERS) {
                if (content.contains(marker)) {
                    violations.add(resource + ": " + marker);
                }
            }
        }

        assertThat(violations)
                .as("自动配置 imports 不得注册微服务客户端")
                .isEmpty();
    }

    private static String classResourceName(String className) {
        return className.replace('.', '/') + ".class";
    }

    private static Path rootPomPath() {
        Path start = Path.of(System.getProperty("maven.multiModuleProjectDirectory", "."))
                .toAbsolutePath()
                .normalize();
        for (Path directory = start; directory != null; directory = directory.getParent()) {
            Path candidate = directory.resolve("pom.xml");
            if (!Files.isRegularFile(candidate)) {
                continue;
            }
            try {
                String content = Files.readString(candidate);
                if (content.contains("<artifactId>spectra-admin</artifactId>")) {
                    return candidate;
                }
            } catch (IOException exception) {
                throw new IllegalStateException("无法读取 Maven 根 POM：" + candidate, exception);
            }
        }
        throw new IllegalStateException("无法从 Maven 项目目录定位 spectra-admin/pom.xml：" + start);
    }

}
