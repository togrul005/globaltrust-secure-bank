package com.globaltrust.bank.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Wires HTTP Basic into Swagger UI so the "Authorize" button works in the browser. */
@Configuration
public class OpenApiConfig {

    private static final String SCHEME = "basicAuth";

    @Bean
    public OpenAPI api() {
        return new OpenAPI()
                .info(new Info()
                        .title("GlobalTrust Secure Bank API")
                        .version("1.0.0")
                        .description("Clark-Wilson maker-checker + Chinese Wall ABAC over a banking workflow."))
                .addSecurityItem(new SecurityRequirement().addList(SCHEME))
                .components(new Components().addSecuritySchemes(SCHEME,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("basic")));
    }
}
