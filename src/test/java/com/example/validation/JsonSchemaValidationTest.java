package com.example.validation;

import com.example.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JsonSchemaValidationTest {
    private Schema schema;
    private ObjectMapper objectMapper;
    private static final String VALID_UUID = "0f8fad5b-d9cb-469f-a165-70867728950e";

    @BeforeEach
    void setUp() {
        try (InputStream inputStream = getClass().getResourceAsStream("/placements.json")) {
            JSONObject rawSchema = new JSONObject(new JSONTokener(inputStream));
            schema = SchemaLoader.load(rawSchema);
            objectMapper = new ObjectMapper();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load schema", e);
        }
    }

    private void validatePlacement(Placement placement) throws Exception {
        String jsonString = objectMapper.writeValueAsString(placement);
        JSONObject jsonObject = new JSONObject(jsonString);
        schema.validate(jsonObject);
    }

    private UnderwriterPool createDefaultUnderwriter() {
        UnderwriterPool underwriter = new UnderwriterPool();
        underwriter.setXid(VALID_UUID);
        underwriter.setFirstName("Tom");
        underwriter.setLastName("Jones");

        UnderwriterPool.Organisation org = new UnderwriterPool.Organisation();
        org.setXid(VALID_UUID);
        org.setName("Test Organisation");
        underwriter.setOrganisation(org);

        UnderwriterPool.Company company = new UnderwriterPool.Company();
        company.setXid(VALID_UUID);
        company.setName("Test Company");
        underwriter.setCompany(company);

        return underwriter;
    }

    private Placement createBasePlacement() {
        Placement placement = new Placement();
        placement.setId(VALID_UUID);
        
        // Required fields from schema
        placement.setClientName("Test Client");
        placement.setDescription("Test Description");
        placement.setEffectiveYear(2024);
        
        // Create and set broker team (required)
        BrokerTeam brokerTeam = new BrokerTeam();
        brokerTeam.setXid(VALID_UUID);
        brokerTeam.setName("Test Broker Team");
        placement.setBrokerTeam(brokerTeam);
        
        // Set placement read access (required)
        placement.setPlacementReadAccess(Arrays.asList(VALID_UUID));
        
        // Set inception date (required) - using simple date format
        placement.setInceptionDate("2024-03-14");
        
        // Set type (required)
        placement.setType("Non-Facility");
        
        // Create and set metadata (required)
        Metadata metadata = createValidMetadata();
        placement.setMetadata(metadata);
        
        // Create and set user (required)
        User user = new User();
        user.setXid(VALID_UUID);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setOrganisationName("Test Org");
        user.setCompanyName("Test Company");
        user.setCompanyXid(VALID_UUID);
        user.setOrganisationXid(VALID_UUID);
        placement.setUser(user);
        
        // Create and set branch (required)
        Branch branch = new Branch();
        branch.setXid(VALID_UUID);
        branch.setName("London Branch");
        placement.setBranch(branch);

        // Set underwriter pool (required)
        placement.setUnderwriterPool(Arrays.asList(createDefaultUnderwriter()));
        
        return placement;
    }

    private Metadata createValidMetadata() {
        Metadata metadata = new Metadata();
        metadata.setCreationDate("ISODate(\"2024-03-14T10:00:00Z\")");
        metadata.setCreationChannel("OutSystems");
        metadata.setCreationUser(VALID_UUID);
        metadata.setModifiedDate("ISODate(\"2024-03-14T10:00:00Z\")");
        metadata.setModifiedChannel("OutSystems");
        metadata.setModifiedUser(VALID_UUID);
        return metadata;
    }

    @Nested
    class MetadataValidationTests {
        @Test
        void validMetadataShouldPass() throws Exception {
            Metadata metadata = createValidMetadata();
            Placement placement = createBasePlacement();
            placement.setMetadata(metadata);
            validatePlacement(placement);
        }

        @Test
        void missingRequiredFieldsShouldFail() throws Exception {
            Metadata metadata = new Metadata();
            // Only set some fields, missing required ones
            metadata.setCreationDate("2024-03-14T10:00:00Z");
            metadata.setCreationChannel("OutSystems");
            // Missing creation_user and other required fields
            
            Placement placement = createBasePlacement();
            placement.setMetadata(metadata);
            assertThrows(ValidationException.class, () -> validatePlacement(placement));
        }
    }

    @Nested
    class UserValidationTests {
        @Test
        void validUserShouldPass() throws Exception {
            User user = new User();
            user.setXid(VALID_UUID);
            user.setFirstName("John");
            user.setLastName("Doe");
            user.setOrganisationName("Test Org");
            user.setCompanyName("Test Company");
            user.setCompanyXid(VALID_UUID);
            user.setOrganisationXid(VALID_UUID);

            Placement placement = createBasePlacement();
            placement.setUser(user);
            validatePlacement(placement);
        }

        @Test
        void invalidUserXidShouldFail() throws Exception {
            User user = new User();
            user.setXid("invalid-uuid");
            user.setFirstName("John");
            user.setLastName("Doe");

            Placement placement = createBasePlacement();
            placement.setUser(user);
            assertThrows(ValidationException.class, () -> validatePlacement(placement));
        }
    }

    @Nested
    class UnderwriterPoolValidationTests {
        @Test
        void validUnderwriterPoolShouldPass() throws Exception {
            UnderwriterPool underwriter = new UnderwriterPool();
            underwriter.setXid(VALID_UUID);
            underwriter.setFirstName("Tom");
            underwriter.setLastName("Jones");

            UnderwriterPool.Organisation org = new UnderwriterPool.Organisation();
            org.setXid(VALID_UUID);
            org.setName("Test Organisation");
            underwriter.setOrganisation(org);

            UnderwriterPool.Company company = new UnderwriterPool.Company();
            company.setXid(VALID_UUID);
            company.setName("Test Company");
            underwriter.setCompany(company);

            Placement placement = createBasePlacement();
            placement.setUnderwriterPool(Arrays.asList(underwriter));
            validatePlacement(placement);
        }

        @Test
        void invalidOrganisationShouldFail() throws Exception {
            UnderwriterPool underwriter = new UnderwriterPool();
            underwriter.setXid(VALID_UUID);
            underwriter.setFirstName("Tom");
            underwriter.setLastName("Jones");

            // Missing required organisation
            UnderwriterPool.Company company = new UnderwriterPool.Company();
            company.setXid(VALID_UUID);
            company.setName("Test Company");
            underwriter.setCompany(company);

            Placement placement = createBasePlacement();
            placement.setUnderwriterPool(Arrays.asList(underwriter));
            assertThrows(ValidationException.class, () -> validatePlacement(placement));
        }
    }

    @Nested
    class BranchValidationTests {
        @Test
        void validBranchShouldPass() throws Exception {
            Branch branch = new Branch();
            branch.setXid(VALID_UUID);
            branch.setName("London Branch");

            Placement placement = createBasePlacement();
            placement.setBranch(branch);
            validatePlacement(placement);
        }

        @Test
        void missingNameShouldFail() throws Exception {
            Branch branch = new Branch();
            branch.setXid(VALID_UUID);
            // Missing required name

            Placement placement = createBasePlacement();
            placement.setBranch(branch);
            assertThrows(ValidationException.class, () -> validatePlacement(placement));
        }
    }

    @Test
    void fullPlacementWithAllObjectsShouldPass() throws Exception {
        // Create metadata
        Metadata metadata = new Metadata();
        metadata.setCreationDate("2024-03-14T10:00:00Z");
        metadata.setCreationChannel("OutSystems");
        metadata.setCreationUser(VALID_UUID);
        metadata.setModifiedDate("2024-03-14T10:00:00Z");
        metadata.setModifiedChannel("OutSystems");
        metadata.setModifiedUser(VALID_UUID);

        // Create user
        User user = new User();
        user.setXid(VALID_UUID);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setOrganisationName("Test Org");
        user.setCompanyName("Test Company");
        user.setCompanyXid(VALID_UUID);
        user.setOrganisationXid(VALID_UUID);

        // Create underwriter pool
        UnderwriterPool underwriter = createDefaultUnderwriter();

        // Create branch
        Branch branch = new Branch();
        branch.setXid(VALID_UUID);
        branch.setName("London Branch");

        // Create broker team
        BrokerTeam brokerTeam = new BrokerTeam();
        brokerTeam.setXid(VALID_UUID);
        brokerTeam.setName("Test Broker Team");

        // Create placement with all objects
        Placement placement = new Placement();
        placement.setId(VALID_UUID);
        placement.setMetadata(metadata);
        placement.setUser(user);
        placement.setUnderwriterPool(Arrays.asList(underwriter));
        placement.setPlacementReadAccess(Arrays.asList(VALID_UUID));
        placement.setBranch(branch);
        placement.setBrokerTeam(brokerTeam);
        placement.setClientName("Test Client");
        placement.setDescription("Test Description");
        placement.setEffectiveYear(2024);
        placement.setInceptionDate("2024-03-14T00:00:00Z");
        placement.setType("Non-Facility");

        validatePlacement(placement);
    }
} 