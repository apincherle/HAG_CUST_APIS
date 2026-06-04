package com.example.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("HAGS API")
                        .version("1.0.0")
                        .description("HAGS Customer Management and QR Certificate API")
                        .contact(new Contact()
                                .name("HAGS API Support")));
        // No servers[] — Swagger UI uses the same origin as the page (browser URL).
    }
}

