package com.example.qrcert.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "label.batch")
@Getter
@Setter
public class LabelBatchProperties {

    private String companyName = "Hags";
    private String templateVersion = "v1";
    private String logoClasspath = "labels/assets/hags_logo_gold.png";
    /** Azure container — same storage account as ximilar ({@code hagsimagestorage}). */
    private String blobContainer = "grading-exports";
    private String blobPrefix = "label-batches";
}
