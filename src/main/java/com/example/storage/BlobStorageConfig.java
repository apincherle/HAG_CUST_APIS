package com.example.storage;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AzureStorageProperties.class)
public class BlobStorageConfig {

    @Bean
    public BlobObjectStore blobObjectStore(AzureStorageProperties properties) {
        if (properties.isConfigured()) {
            return new AzureBlobObjectStore(properties);
        }
        return new DisabledBlobObjectStore();
    }
}
