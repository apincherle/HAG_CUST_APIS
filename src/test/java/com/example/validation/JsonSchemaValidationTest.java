package com.example.validation;

import com.example.model.Placement;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class JsonSchemaValidationTest {

    private Schema schema;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws IOException {
        String schemaPath = Paths.get("json_schemas", "placements.json").toString();
        File schemaFile = new File(schemaPath);
        
        if (!schemaFile.exists()) {
            throw new IOException("Schema file not found at: " + schemaFile.getAbsolutePath());
        }

        try (InputStream inputStream = new FileInputStream(schemaFile)) {
            JSONObject rawSchema = new JSONObject(new JSONTokener(inputStream));
            schema = SchemaLoader.load(rawSchema);
        }

        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    private Placement createBasePlacement(String dateFormat) {
        Placement placement = new Placement();
        placement.set_id("0f8fad5b-d9cb-469f-a165-70867728950e");

        // Set metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("creation_date", dateFormat);
        metadata.put("creation_channel", "OutSystems");
        metadata.put("creation_user", "0f8fad5b-d9cb-469f-a165-70867728950e");
        metadata.put("modified_date", dateFormat);
        metadata.put("modified_channel", "OutSystems");
        metadata.put("modified_user", "0f8fad5b-d9cb-469f-a165-70867728950e");
        placement.set_metadata(metadata);

        // Set user
        Placement.User user = new Placement.User();
        user.set_xid("0f8fad5b-d9cb-469f-a165-70867728950e");
        user.setFirst_name("John");
        user.setLast_name("Doe");
        user.setOrganisation_name("Test Org");
        user.setCompany_name("Test Company");
        user.setCompany_xid("0f8fad5b-d9cb-469f-a165-70867728950e");
        user.setOrganisation_xid("0f8fad5b-d9cb-469f-a165-70867728950e");
        placement.setUser(user);

        // Set placement_read_access
        placement.setPlacement_read_access(new ArrayList<>());

        // Set branch
        Placement.Branch branch = new Placement.Branch();
        branch.set_xid("0f8fad5b-d9cb-469f-a165-70867728950e");
        branch.setName("London Branch");
        placement.setBranch(branch);

        // Set other fields
        placement.setClient_name("Test Client Ltd");
        placement.setDescription("Test placement description");
        placement.setEffective_year(2024);

        // Set broker team
        Placement.BrokerTeam brokerTeam = new Placement.BrokerTeam();
        brokerTeam.set_xid("0f8fad5b-d9cb-469f-a165-70867728950e");
        brokerTeam.setName("Test Broker Team");
        placement.setBroker_team(brokerTeam);

        placement.setInception_date(dateFormat);
        placement.setType("PLACEMENT");

        return placement;
    }

    private void validatePlacement(Placement placement) throws Exception {
        String jsonString = objectMapper.writeValueAsString(placement);
        JSONObject jsonObject = new JSONObject(jsonString);

        try {
            schema.validate(jsonObject);
        } catch (ValidationException e) {
            System.out.println("Validation errors:");
            e.getAllMessages().forEach(System.out::println);
            throw e;
        }
    }

    @Test
    void validPlacementShouldPass() throws Exception {
        Placement placement = createBasePlacement("2024-03-14");
        validatePlacement(placement);
    }

    @Test
    void invalidPlacementShouldFail() throws Exception {
        Placement invalidPlacement = new Placement();
        invalidPlacement.set_id("invalid-id"); // Invalid UUID format

        String jsonString = objectMapper.writeValueAsString(invalidPlacement);
        JSONObject jsonObject = new JSONObject(jsonString);

        assertThrows(ValidationException.class, () -> schema.validate(jsonObject));
    }

    @Test
    void validDateFormatShouldPass() throws Exception {
        Placement placement = createBasePlacement("2021-10-11T11:50:00Z");
        validatePlacement(placement);
    }
} 