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

package com.devops00.spectra.launch.contract;

import com.devops00.spectra.launch.LaunchApplication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * 从真实组合根和 MVC 映射导出 OpenAPI，要求隔离数据库与 Redis。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/28
 */
@EnabledIfSystemProperty(named = "spectra.test.real-deps", matches = "true")
class OpenApiExportTest {

    @Test
    void exportRuntimeOpenApi() throws Exception {
        requireIsolatedDependencies();
        try (var application = new SpringApplicationBuilder(LaunchApplication.class)
                .run("--server.port=0", "--server.address=127.0.0.1", "--server.ssl.enabled=false",
                        "--spring.quartz.auto-startup=false", "--flowable.async-executor-activate=false")) {
            var mvc = MockMvcBuilders.webAppContextSetup((WebApplicationContext) application).build();
            var response = mvc.perform(get("/api/v3/api-docs").contextPath("/api")).andReturn().getResponse();
            assertEquals(200, response.getStatus(), response.getContentAsString());
            var body = response.getContentAsString();
            var root = application.getBean(ObjectMapper.class).readTree(body);
            assertEquals("3.1.0", root.path("openapi").asString());
            assertEquals("1.0.0", root.path("info").path("version").asString());
            assertTrue(root.path("paths").size() > 0, body);
            var schemas = root.path("components").path("schemas");
            assertTrue(schemas.path("UserProfileVO").path("properties").has("employee_no"), "导出必须使用真实 snake_case");
            assertTrue(schemas.path("UserProfileVO").path("required").toString().contains("\"version\""),
                    "编辑视图必须声明读取版本");
            assertTrue(schemas.path("UserProfileFrom").path("properties").path("language").path("type").isArray(),
                    "可清空字段必须声明 JSON null");
            assertTrue(schemas.path("ApiErrorEnvelope").path("properties").has("error_code"),
                    "错误响应必须导出稳定错误码");
            var profileResponses = root.path("paths").path("/user/profile").path("get").path("responses");
            assertEquals("#/components/schemas/ApiErrorEnvelope",
                    profileResponses.path("409")
                            .path("content")
                            .path("application/json")
                            .path("schema")
                            .path("$ref")
                            .asString(),
                    "冲突响应必须表达统一错误结构");
            assertEquals("#/components/schemas/UserProfileVO", root.path("paths")
                    .path("/user/profile")
                    .path("get")
                    .path("responses")
                    .path("200")
                    .path("content")
                    .path("*/*")
                    .path("schema")
                    .path("properties")
                    .path("data")
                    .path("$ref")
                    .asString(),
                    "端点成功响应必须表达真实 data 包装");
            String output = System.getProperty("spectra.openapi.output");
            if (output != null && !output.isBlank()) {
                Path path = Path.of(output).toAbsolutePath();
                Files.createDirectories(path.getParent());
                Files.writeString(path, body);
            }
        }
    }

    private static void requireIsolatedDependencies() {
        String url = System.getenv("DB_URL");
        if (url == null || !url.matches("^jdbc:postgresql://[^/]+/devops00_spectra_db_test(?:\\?.*)?$")) {
            throw new IllegalStateException("OpenAPI 导出只允许 devops00_spectra_db_test");
        }
        if (!"5".equals(System.getenv("REDIS_DB"))) {
            throw new IllegalStateException("OpenAPI 导出只允许 Redis DB 5");
        }
    }
}
