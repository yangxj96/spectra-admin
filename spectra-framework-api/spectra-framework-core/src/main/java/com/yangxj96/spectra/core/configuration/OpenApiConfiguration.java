package com.yangxj96.spectra.core.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenApi配置
 *
 * @author 杨新杰
 * @since 2025/5/27 17:56
 */
@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI customOpenAPI() {
        // 定义一个名为 "bearerAuth" 的安全方案
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info().title("光谱后端API请求接口").version("1.0.0"))
                // 添加安全要求
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                // 定义安全方案
                .components(
                        new io.swagger.v3.oas.models.Components()
                                .addSecuritySchemes(securitySchemeName,
                                        new SecurityScheme()
                                                .name(securitySchemeName)
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")  // 如果你使用的是 JWT
                                )
                );
    }

}
