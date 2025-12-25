package com.barofarm.auth.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "BaroFarm Auth API",
        version = "v1"
    )
)
@SecurityScheme(
    name = "bearerAuth",              // SecurityRequirement에서 쓰는 이름
    type = SecuritySchemeType.HTTP,   // enums 패키지의 enum
    scheme = "bearer",
    bearerFormat = "JWT"
)
public class OpenApiConfig {
}
