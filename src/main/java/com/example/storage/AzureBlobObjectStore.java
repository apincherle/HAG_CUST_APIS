package com.example.storage;

import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobHttpHeaders;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class AzureBlobObjectStore implements BlobObjectStore {

    private final AzureStorageProperties properties;
    private final BlobServiceClient serviceClient;
    private final Map<String, BlobContainerClient> containers = new ConcurrentHashMap<>();

    public AzureBlobObjectStore(AzureStorageProperties properties) {
        this.properties = properties;
        this.serviceClient = buildServiceClient();
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void put(String container, String blobKey, byte[] content, String contentType) throws IOException {
        try {
            BlobClient blob = containerClient(container).getBlobClient(blobKey);
            blob.upload(BinaryData.fromBytes(content), true);
            blob.setHttpHeaders(new BlobHttpHeaders().setContentType(contentType));
        } catch (RuntimeException e) {
            throw new IOException("Failed to upload blob " + container + "/" + blobKey, e);
        }
    }

    @Override
    public Optional<byte[]> get(String container, String blobKey) throws IOException {
        try {
            BlobClient blob = containerClient(container).getBlobClient(blobKey);
            if (!Boolean.TRUE.equals(blob.exists())) {
                return Optional.empty();
            }
            return Optional.of(blob.downloadContent().toBytes());
        } catch (RuntimeException e) {
            throw new IOException("Failed to download blob " + container + "/" + blobKey, e);
        }
    }

    @Override
    public boolean exists(String container, String blobKey) throws IOException {
        try {
            return Boolean.TRUE.equals(containerClient(container).getBlobClient(blobKey).exists());
        } catch (RuntimeException e) {
            throw new IOException("Failed to check blob " + container + "/" + blobKey, e);
        }
    }

    private BlobContainerClient containerClient(String containerName) {
        return containers.computeIfAbsent(containerName, name -> {
            BlobContainerClient client = serviceClient.getBlobContainerClient(name);
            if (!client.exists()) {
                client.create();
                log.info("Created blob container {}", name);
            }
            return client;
        });
    }

    private BlobServiceClient buildServiceClient() {
        BlobServiceClientBuilder builder = new BlobServiceClientBuilder();
        if (isNotBlank(properties.getConnectionString())) {
            builder.connectionString(properties.getConnectionString());
        } else if (isNotBlank(properties.getAccountName())) {
            builder.credential(new DefaultAzureCredentialBuilder().build());
            builder.endpoint("https://" + properties.getAccountName() + ".blob.core.windows.net");
        } else {
            throw new IllegalStateException("Blob storage requires STORAGE_CONNECTION_STRING or AZURE_STORAGE_ACCOUNT_NAME");
        }
        if (isNotBlank(properties.getBlobEndpoint())) {
            builder.endpoint(properties.getBlobEndpoint());
        }
        return builder.buildClient();
    }

    private static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }
}
