package com.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final StringToSubmissionStatusConverter stringToSubmissionStatusConverter;

    public WebConfig(@NonNull StringToSubmissionStatusConverter stringToSubmissionStatusConverter) {
        this.stringToSubmissionStatusConverter = stringToSubmissionStatusConverter;
    }

    @Override
    public void addFormatters(@NonNull FormatterRegistry registry) {
        registry.addConverter(stringToSubmissionStatusConverter);
    }
}

