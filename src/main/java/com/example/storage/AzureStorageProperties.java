package com.example.storage;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Azure Blob auth — env names aligned with {@code HAGS_ximilar_ai_spring} Container App settings.
 */
@ConfigurationProperties(prefix = "app.storage")
@Getter
@Setter
public class AzureStorageProperties {

    private boolean enabled = true;
    private String accountName = "";
    private String connectionString = "";
    private String blobEndpoint = "";

    public boolean isConfigured() {
        if (!enabled) {
            return false;
        }
        return isNotBlank(connectionString) || isNotBlank(accountName);
    }

    private static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }
}
