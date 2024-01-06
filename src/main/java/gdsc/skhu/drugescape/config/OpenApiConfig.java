package gdsc.skhu.drugescape.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class OpenApiConfig {
    @Bean
    public OpenAPI openAPI() {
        // 정보
        Info info = new Info()
                .title("DrugEscape API Documentation")
                .description("API 문서화")
                .version("2.3.0");
        // SecuritySecheme명
        String JWTSchemeName = "JWT Authorization";
        // API 요청 헤더에 인증정보 포함
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(JWTSchemeName);
        // SecuritySchemes 등록
        Components components = new Components()
                .addSecuritySchemes(JWTSchemeName, new SecurityScheme()
                        .name(JWTSchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("Bearer")
                        .bearerFormat("JWT")); // 토큰 형식을 지정하는 임의의 문자(Optional)

        return new OpenAPI()
                .info(info)
                .addSecurityItem(securityRequirement)
                .components(components);
    }
}