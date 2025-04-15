package com.example.validation;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class JsonSchemaValidationTest {

    private Schema schema;

    @BeforeEach
    void setUp() throws IOException {
        // Get the absolute path to the json_schemas directory
        String schemaPath = Paths.get("json_schemas", "placements.json").toString();
        File schemaFile = new File(schemaPath);
        
        if (!schemaFile.exists()) {
            throw new IOException("Schema file not found at: " + schemaFile.getAbsolutePath());
        }

        // Load schema using FileInputStream
        try (InputStream inputStream = new FileInputStream(schemaFile)) {
            JSONObject rawSchema = new JSONObject(new JSONTokener(inputStream));
            schema = SchemaLoader.load(rawSchema);
        }
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
                .put("organisation_xid", "0f8fad5b-d9cb-469f-a165-70867728950e"))
            .put("placement_read_access", new JSONArray())
            .put("branch", new JSONObject()
                .put("_xid", "0f8fad5b-d9cb-469f-a165-70867728950e")
                .put("name", "London Branch"))
            .put("client_name", "Test Client Ltd")
            .put("description", "Test placement description")
            .put("effective_year", 2024)
            .put("broker_team", new JSONObject()
                .put("_xid", "0f8fad5b-d9cb-469f-a165-70867728950e")
                .put("name", "Test Broker Team"))
            .put("inception_date", "2024-01-01")
            .put("type", "PLACEMENT");

        try {
            schema.validate(validPlacement);
        } catch (ValidationException e) {
            System.out.println("Validation errors:");
            e.getAllMessages().forEach(System.out::println);
            throw e;
        }
    }

    @Test
    void invalidPlacementShouldFail() {
        JSONObject invalidPlacement = new JSONObject()
            .put("_id", "invalid-id")  // Invalid UUID format
            .put("_metadata", new JSONObject());

        assertThrows(ValidationException.class, () -> schema.validate(invalidPlacement));
    }

    @Test
    void validDateFormatShouldPass() {
        JSONObject validPlacement = new JSONObject()
            .put("_id", "0f8fad5b-d9cb-469f-a165-70867728950e")
            .put("_metadata", new JSONObject()
                .put("creation_date", "2021-10-11T11:50:00Z")
                .put("creation_channel", "OutSystems")
                .put("creation_user", "0f8fad5b-d9cb-469f-a165-70867728950e")
                .put("modified_date", "2021-10-11T11:50:00Z")
                .put("modified_channel", "OutSystems")
                .put("modified_user", "0f8fad5b-d9cb-469f-a165-70867728950e"))
            .put("user", new JSONObject()
                .put("_xid", "0f8fad5b-d9cb-469f-a165-70867728950e")
                .put("first_name", "John")
                .put("last_name", "Doe")
                .put("organisation_name", "Test Org")
                .put("company_name", "Test Company")
                .put("company_xid", "0f8fad5b-d9cb-469f-a165-70867728950e")
                .put("organisation_xid", "0f8fad5b-d9cb-469f-a165-70867728950e"))
            .put("placement_read_access", new JSONArray())
            .put("branch", new JSONObject()
                .put("_xid", "0f8fad5b-d9cb-469f-a165-70867728950e")
                .put("name", "London Branch"))
            .put("client_name", "Test Client Ltd")
            .put("description", "Test placement description")
            .put("effective_year", 2024)
            .put("broker_team", new JSONObject()
                .put("_xid", "0f8fad5b-d9cb-469f-a165-70867728950e")
                .put("name", "Test Broker Team"))
            .put("inception_date", "2021-10-11T11:50:00Z")
            .put("type", "PLACEMENT");

        assertDoesNotThrow(() -> schema.validate(validPlacement));
    }
} 