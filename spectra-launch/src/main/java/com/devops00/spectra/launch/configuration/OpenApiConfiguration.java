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

package com.devops00.spectra.launch.configuration;

import com.devops00.spectra.framework.web.response.R;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.util.NamingStrategyImpls;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * 运行时 API 契约的版本和说明。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/28
 */
@Configuration(proxyBeanMethods = false)
public class OpenApiConfiguration {

    private static final Set<String> VERSIONED_EDIT_VIEWS = Set.of(
            "UserPageVO", "UserProfileVO", "MenuVO", "MenuTreeVO", "DictGroupTreeVO", "DictItemVO",
            "RegionVO", "ConfiguredVO", "QuartzJobVO");

    /** 显式 JSON null 表示清空的字段；缺失的字段由具体入参校验决定。 */
    private static final Map<String, Set<String>> CLEARABLE_INPUTS = Map.of(
            "UserProfileFrom", Set.of("language", "timezone"),
            "UserSaveFrom", Set.of("language", "timezone"),
            "MenuSaveFrom", Set.of("pid", "route_name"),
            "DictGroupFrom", Set.of("pid", "remark"),
            "DictItemFrom", Set.of("remark"),
            "RegionFrom", Set.of("pid", "full_name"),
            "ConfiguredBatchItemFrom", Set.of("remarks"),
            "QuartzTriggerFrom", Set.of("start_at"));

    /** 公开的 OpenAPI 版本固定为当前统一 API 契约版本。 */
    @Bean
    public OpenAPI spectraOpenApi() {
        return new OpenAPI().info(new Info().title("Spectra API").version("1.0.0"));
    }

    /** SpringDoc 的模型扫描不继承运行时 Jackson 3 命名策略，导出时与真实 JSON 对齐。 */
    @Bean
    public OpenApiCustomizer snakeCaseOpenApiProperties() {
        return openApi -> {
            if (openApi.getComponents() == null || openApi.getComponents().getSchemas() == null) {
                return;
            }
            ObjectSchema error = new ObjectSchema();
            error.addProperty("code", new IntegerSchema().format("int32"));
            error.addProperty("msg", new StringSchema());
            error.addProperty("error_code", new StringSchema());
            error.addRequiredItem("code");
            error.addRequiredItem("error_code");
            openApi.getComponents().addSchemas("ApiErrorEnvelope", error);
            for (var entry : openApi.getComponents().getSchemas().entrySet()) {
                Schema<?> schema = entry.getValue();
                if (schema.getProperties() == null) {
                    continue;
                }
                var properties = new LinkedHashMap<String, Schema>();
                schema.getProperties()
                        .forEach((name, property) -> properties.put(NamingStrategyImpls.SNAKE_CASE.translate(name), (Schema<?>) property));
                schema.setProperties(properties);
                if (schema.getRequired() != null) {
                    schema.setRequired(schema.getRequired()
                            .stream()
                            .map(NamingStrategyImpls.SNAKE_CASE::translate)
                            .toList());
                }
                if (VERSIONED_EDIT_VIEWS.contains(entry.getKey())) {
                    schema.addRequiredItem("version");
                }
                for (String field : CLEARABLE_INPUTS.getOrDefault(entry.getKey(), Set.of())) {
                    Schema<?> property = (Schema<?>) schema.getProperties().get(field);
                    if (property != null) {
                        var types = new LinkedHashSet<String>();
                        if (property.getTypes() != null) {
                            types.addAll(property.getTypes());
                        }
                        if (property.getType() != null) {
                            types.add(property.getType());
                        }
                        if (!types.isEmpty()) {
                            types.add("null");
                            property.setTypes(types);
                        }
                    }
                }
            }
        };
    }

    /** Controller 的普通返回值会由 ResponseModifyAdvice 包装；生成端点响应必须表达 HTTP 上的结构。 */
    @Bean
    public OperationCustomizer actualResponseEnvelope() {
        return (operation, handlerMethod) -> {
            if (operation.getResponses() != null) {
                for (String status : new String[]{"400", "401", "403", "409", "416", "500", "503"}) {
                    if ("416".equals(status)
                            && !"FileAssetStreamController".equals(handlerMethod.getBeanType().getSimpleName())) {
                        continue;
                    }
                    operation.getResponses()
                            .putIfAbsent(status, new ApiResponse()
                                    .description("统一错误响应")
                                    .content(new Content().addMediaType("application/json",
                                            new io.swagger.v3.oas.models.media.MediaType()
                                                    .schema(new Schema<>().$ref("#/components/schemas/ApiErrorEnvelope")))));
                }
            }
            Class<?> resultType = handlerMethod.getMethod().getReturnType();
            String packageName = handlerMethod.getBeanType().getPackageName();
            if (!packageName.startsWith("com.devops00.spectra.")
                    || !packageName.contains(".controller")
                    || R.class.isAssignableFrom(resultType)
                    || ResponseEntity.class.isAssignableFrom(resultType)
                    || Resource.class.isAssignableFrom(resultType)
                    || String.class.isAssignableFrom(resultType)
                    || resultType == byte[].class
                    || reactor.core.publisher.Flux.class.isAssignableFrom(resultType)) {
                return operation;
            }
            if (operation.getResponses() == null) {
                return operation;
            }
            operation.getResponses().forEach((status, response) -> {
                if (!status.startsWith("2")) {
                    return;
                }
                Content content = response.getContent();
                if (content == null || content.isEmpty()) {
                    content = new Content().addMediaType("application/json", new io.swagger.v3.oas.models.media.MediaType());
                    response.setContent(content);
                }
                content.values().forEach(media -> {
                    Schema<?> data = media.getSchema();
                    ObjectSchema envelope = new ObjectSchema();
                    envelope.addProperty("code", new IntegerSchema().format("int32"));
                    envelope.addProperty("msg", new StringSchema());
                    envelope.addRequiredItem("code");
                    envelope.addRequiredItem("msg");
                    if (data != null && resultType != void.class) {
                        envelope.addProperty("data", data);
                    }
                    media.setSchema(envelope);
                });
            });
            return operation;
        };
    }
}
