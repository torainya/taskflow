package com.taskflow.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 文档元信息 + 全局 Bearer 鉴权声明。
 * Swagger UI 地址：/swagger-ui.html（注册/登录接口已在 Security 层放行）。
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI taskflowOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TaskFlow API")
                        .description("任务待办管理 Demo —— 全生命周期示例项目后端接口文档")
                        .version("v0.1.0"))
                .components(new Components().addSecuritySchemes("bearerJwt",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearerJwt"));
    }
}
