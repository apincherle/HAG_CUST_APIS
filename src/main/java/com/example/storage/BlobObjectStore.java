package com.example.storage;

import java.io.IOException;
import java.util.Optional;

public interface BlobObjectStore {

    boolean isAvailable();

    void put(String container, String blobKey, byte[] content, String contentType) throws IOException;

    Optional<byte[]> get(String container, String blobKey) throws IOException;

    boolean exists(String container, String blobKey) throws IOException;
}
