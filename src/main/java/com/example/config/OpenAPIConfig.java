package com.example.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("HAGS Customer API")
                        .version("1.0.0")
                        .description("Customer management API for HAGS")
                        .contact(new Contact()
                                .name("HAGS API Support")))
                .servers(List.of(
                        new Server().url("http://localhost:8001").description("Local development server"),
                        new Server().url("https://api.yourdomain.com").description("Production server")
                ));
    }
}

