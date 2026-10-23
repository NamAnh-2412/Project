package btl.vexemphim.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI cineveOpenApi() {
        String scheme = "bearer-jwt";
        return new OpenAPI()
                .info(new Info()
                        .title("CineVe API")
                        .version("2.0")
                        .description("Modular monolith: Auth, Catalog, Booking & POS. Đăng nhập ở /api/auth/login rồi dán JWT vào Authorize."))
                .addSecurityItem(new SecurityRequirement().addList(scheme))
                .components(new Components().addSecuritySchemes(scheme, new SecurityScheme()
                        .name(scheme)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
}
