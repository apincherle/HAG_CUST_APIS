package com.example.storage;

import java.io.IOException;
import java.util.Optional;

public final class DisabledBlobObjectStore implements BlobObjectStore {

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public void put(String container, String blobKey, byte[] content, String contentType) throws IOException {
        throw new IOException("Blob storage is not configured");
    }

    @Override
    public Optional<byte[]> get(String container, String blobKey) {
        return Optional.empty();
    }

    @Override
    public boolean exists(String container, String blobKey) {
        return false;
    }
}
