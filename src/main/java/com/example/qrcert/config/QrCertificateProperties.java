package com.example.qrcert.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "qr.certificate")
@Getter
@Setter
public class QrCertificateProperties {
    private String baseUrl = "https://www.hags-grading.co.uk";
    private String secret = "change-me-please";
    private String qrStoragePath = "./static/qrs";
    private String serialPrefix = "HAGS";
}

