package com.example.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.UUID;

@Converter(autoApply = true)
public class UUIDAttributeConverter implements AttributeConverter<UUID, String> {

    @Override
    public String convertToDatabaseColumn(UUID uuid) {
        return (uuid == null) ? null : uuid.toString();
    }

    @Override
    public UUID convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        try {
            // Trim whitespace and ensure proper format
            String trimmed = dbData.trim();
            System.out.println("DEBUG UUIDConverter: Converting from DB string: '" + trimmed + "'");
            UUID result = UUID.fromString(trimmed);
            System.out.println("DEBUG UUIDConverter: Converted to UUID: " + result);
            return result;
        } catch (IllegalArgumentException e) {
            System.err.println("ERROR UUIDConverter: Failed to convert '" + dbData + "' to UUID: " + e.getMessage());
            throw new IllegalArgumentException("Invalid UUID format: " + dbData, e);
        }
    }
}

