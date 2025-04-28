package com.example.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {
    @Value("${server.port}")
    private String serverPort;

    @Bean
    public OpenAPI myOpenAPI() {
        Contact contact = new Contact();
        contact.setEmail("Andrew@rc.com");
        contact.setName("API Support");

        Info info = new Info()
                .title("PLacement API")
                .version("1.0")
                .description("This is a Placement API")
                .contact(contact);

        return new OpenAPI().info(info);
    }
} 