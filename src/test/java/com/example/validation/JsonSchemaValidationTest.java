package com.example.validation;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

public class JsonSchemaValidationTest {

    private Schema schema;

    @BeforeEach
    void setUp() throws IOException {
        // Read the schema file
        File schemaFile = ResourceUtils.getFile("classpath:placements.json");
        String schemaContent = Files.readString(schemaFile.toPath());
        
        // Remove comments from the schema content
        schemaContent = schemaContent.replaceAll("//.*\\n", "\n");
        
        // Load the schema
        JSONObject rawSchema = new JSONObject(schemaContent);
        schema = SchemaLoader.load(rawSchema);
    }

    @Test
    void validPlacementShouldPass() {
        JSONObject validPlacement = new JSONObject()
            .put("_id", "0f8fad5b-d9cb-469f-a165-70867728950e")
            .put("_metadata", new JSONObject()
                .put("creation_date", "2024-03-14")
                .put("creation_channel", "OutSystems")
                .put("creation_user", "0f8fad5b-d9cb-469f-a165-70867728950e")
                .put("modified_date", "2024-03-14")
                .put("modified_channel", "OutSystems")
                .put("modified_user", "0f8fad5b-d9cb-469f-a165-70867728950e"))
            .put("user", new JSONObject()
                .put("_xid", "0f8fad5b-d9cb-469f-a165-70867728950e")
                .put("first_name", "John")
                .put("last_name", "Doe")
                .put("organisation_name", "Test Org")
                .put("company_name", "Test Company")
                .put("company_xid", "0f8fad5b-d9cb-469f-a165-70867728950e")
                .put("organisation_xid", "0f8fad5b-d9cb-469f-a165-70867728950e"));

        assertDoesNotThrow(() -> schema.validate(validPlacement));
    }

    @Test
    void invalidPlacementShouldFail() {
        JSONObject invalidPlacement = new JSONObject()
            .put("_id", "invalid-id")  // Invalid UUID format
            .put("_metadata", new JSONObject());

        assertThrows(ValidationException.class, () -> schema.validate(invalidPlacement));
    }
} 