package com.example.validation;

import com.example.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Placement Schema Validation Tests")
class PlacementSchemaValidationTest {

    private ObjectMapper objectMapper;
    private String fullPlacementJson;
    private JsonNode placementJsonNode;
    private JsonNode schemaJsonNode;

    // UUID pattern for validation
    private static final Pattern UUID_PATTERN = Pattern.compile("^[0-9a-fA-F]{8}-([0-9a-fA-F]{4}-){3}[0-9a-fA-F]{12}$");

    @BeforeEach
    void setUp() throws IOException {
        objectMapper = new ObjectMapper();
        
        // Load the full placement JSON
        ClassPathResource resource = new ClassPathResource("fullplacement.json");
        fullPlacementJson = Files.readString(resource.getFile().toPath());
        placementJsonNode = objectMapper.readTree(fullPlacementJson);
        
        // Load the schema JSON
        ClassPathResource schemaResource = new ClassPathResource("placements.json");
        schemaJsonNode = objectMapper.readTree(schemaResource.getInputStream());
    }

    @Test
    @DisplayName("Should validate all required fields are present")
    void testRequiredFieldsPresent() {
        JsonNode requiredFields = schemaJsonNode.get("required");
        assertNotNull(requiredFields, "Schema should define required fields");
        
        for (JsonNode requiredField : requiredFields) {
            String fieldName = requiredField.asText();
            assertTrue(placementJsonNode.has(fieldName), 
                "Required field '" + fieldName + "' should be present in placement JSON");
        }
    }

    @Test
    @DisplayName("Should validate UUID format for ID fields")
    void testUuidFormatValidation() {
        // Test main _id field
        String mainId = placementJsonNode.get("_id").asText();
        assertTrue(UUID_PATTERN.matcher(mainId).matches(), 
            "Main _id should be a valid UUID format");

        // Test user _xid field
        String userXid = placementJsonNode.get("user").get("_xid").asText();
        assertTrue(UUID_PATTERN.matcher(userXid).matches(), 
            "User _xid should be a valid UUID format");

        // Test branch _xid field
        String branchXid = placementJsonNode.get("branch").get("_xid").asText();
        assertTrue(UUID_PATTERN.matcher(branchXid).matches(), 
            "Branch _xid should be a valid UUID format");

        // Test broker team _xid field
        String brokerTeamXid = placementJsonNode.get("broker_team").get("_xid").asText();
        assertTrue(UUID_PATTERN.matcher(brokerTeamXid).matches(), 
            "Broker team _xid should be a valid UUID format");

        // Test underwriter pool _xid fields
        ArrayNode underwriterPool = (ArrayNode) placementJsonNode.get("underwriter_pool");
        for (JsonNode underwriter : underwriterPool) {
            String underwriterXid = underwriter.get("_xid").asText();
            assertTrue(UUID_PATTERN.matcher(underwriterXid).matches(), 
                "Underwriter _xid should be a valid UUID format");
        }

        // Test programme _id fields
        ArrayNode programmes = (ArrayNode) placementJsonNode.get("programmes");
        for (JsonNode programme : programmes) {
            String programmeId = programme.get("_id").asText();
            assertTrue(UUID_PATTERN.matcher(programmeId).matches(), 
                "Programme _id should be a valid UUID format");
        }
    }

    @Test
    @DisplayName("Should validate metadata structure")
    void testMetadataStructure() {
        JsonNode metadata = placementJsonNode.get("_metadata");
        assertNotNull(metadata, "Metadata should be present");

        // Check required metadata fields
        String[] requiredMetadataFields = {
            "creation_date", "creation_channel", "creation_user",
            "modified_date", "modified_channel", "modified_user"
        };

        for (String field : requiredMetadataFields) {
            assertTrue(metadata.has(field), 
                "Metadata should have required field: " + field);
            assertFalse(metadata.get(field).asText().isEmpty(), 
                "Metadata field " + field + " should not be empty");
        }

        // Validate UUID format for user fields
        String creationUser = metadata.get("creation_user").asText();
        String modifiedUser = metadata.get("modified_user").asText();
        assertTrue(UUID_PATTERN.matcher(creationUser).matches(), 
            "Creation user should be valid UUID");
        assertTrue(UUID_PATTERN.matcher(modifiedUser).matches(), 
            "Modified user should be valid UUID");
    }

    @Test
    @DisplayName("Should validate user structure")
    void testUserStructure() {
        JsonNode user = placementJsonNode.get("user");
        assertNotNull(user, "User should be present");

        String[] requiredUserFields = {
            "_xid", "first_name", "last_name", "organisation_name",
            "company_name", "company_xid", "organisation_xid"
        };

        for (String field : requiredUserFields) {
            assertTrue(user.has(field), 
                "User should have required field: " + field);
            assertFalse(user.get(field).asText().isEmpty(), 
                "User field " + field + " should not be empty");
        }

        // Validate UUID format for ID fields
        String userXid = user.get("_xid").asText();
        String companyXid = user.get("company_xid").asText();
        String organisationXid = user.get("organisation_xid").asText();
        
        assertTrue(UUID_PATTERN.matcher(userXid).matches(), 
            "User _xid should be valid UUID");
        assertTrue(UUID_PATTERN.matcher(companyXid).matches(), 
            "Company _xid should be valid UUID");
        assertTrue(UUID_PATTERN.matcher(organisationXid).matches(), 
            "Organisation _xid should be valid UUID");
    }

    @Test
    @DisplayName("Should validate underwriter pool structure")
    void testUnderwriterPoolStructure() {
        ArrayNode underwriterPool = (ArrayNode) placementJsonNode.get("underwriter_pool");
        assertNotNull(underwriterPool, "Underwriter pool should be present");
        assertTrue(underwriterPool.size() > 0, "Underwriter pool should not be empty");

        for (JsonNode underwriter : underwriterPool) {
            // Check required fields
            String[] requiredFields = {"_xid", "first_name", "last_name", "organisation", "company"};
            for (String field : requiredFields) {
                assertTrue(underwriter.has(field), 
                    "Underwriter should have required field: " + field);
            }

            // Validate organisation structure
            JsonNode organisation = underwriter.get("organisation");
            assertTrue(organisation.has("_xid"), "Organisation should have _xid");
            assertTrue(organisation.has("name"), "Organisation should have name");
            assertTrue(UUID_PATTERN.matcher(organisation.get("_xid").asText()).matches(), 
                "Organisation _xid should be valid UUID");

            // Validate company structure
            JsonNode company = underwriter.get("company");
            assertTrue(company.has("_xid"), "Company should have _xid");
            assertTrue(company.has("name"), "Company should have name");
            assertTrue(UUID_PATTERN.matcher(company.get("_xid").asText()).matches(), 
                "Company _xid should be valid UUID");
        }
    }

    @Test
    @DisplayName("Should validate programme structure")
    void testProgrammeStructure() {
        ArrayNode programmes = (ArrayNode) placementJsonNode.get("programmes");
        assertNotNull(programmes, "Programmes should be present");
        assertTrue(programmes.size() > 0, "Programmes should not be empty");

        for (JsonNode programme : programmes) {
            // Check required fields
            String[] requiredFields = {
                "_id", "broker_team", "user", "description", "inception_date",
                "status_code", "sequence_number", "_metadata"
            };
            for (String field : requiredFields) {
                assertTrue(programme.has(field), 
                    "Programme should have required field: " + field);
            }

            // Validate programme metadata
            JsonNode programmeMetadata = programme.get("_metadata");
            assertNotNull(programmeMetadata, "Programme should have metadata");
            assertTrue(programmeMetadata.has("creation_date"), 
                "Programme metadata should have creation_date");
            assertTrue(programmeMetadata.has("creation_user"), 
                "Programme metadata should have creation_user");

            // Validate contracts if present
            if (programme.has("contracts")) {
                ArrayNode contracts = (ArrayNode) programme.get("contracts");
                for (JsonNode contract : contracts) {
                    validateContractStructure(contract);
                }
            }
        }
    }

    @Test
    @DisplayName("Should validate contract structure")
    void testContractStructure() {
        ArrayNode programmes = (ArrayNode) placementJsonNode.get("programmes");
        JsonNode programme = programmes.get(0);
        ArrayNode contracts = (ArrayNode) programme.get("contracts");
        
        assertNotNull(contracts, "Contracts should be present");
        assertTrue(contracts.size() > 0, "Contracts should not be empty");

        for (JsonNode contract : contracts) {
            validateContractStructure(contract);
        }
    }

    private void validateContractStructure(JsonNode contract) {
        // Check required contract fields
        String[] requiredFields = {
            "_id", "broker_team", "user", "broker_code", "broker_contract_ref",
            "contract_umr", "sequence_number", "description", "contract_type",
            "cover_type", "status", "version", "_metadata"
        };
        for (String field : requiredFields) {
            assertTrue(contract.has(field), 
                "Contract should have required field: " + field);
        }

        // Validate contract metadata
        JsonNode contractMetadata = contract.get("_metadata");
        assertNotNull(contractMetadata, "Contract should have metadata");

        // Validate sections if present
        if (contract.has("sections")) {
            ArrayNode sections = (ArrayNode) contract.get("sections");
            for (JsonNode section : sections) {
                validateSectionStructure(section);
            }
        }
    }

    @Test
    @DisplayName("Should validate section structure")
    void testSectionStructure() {
        ArrayNode programmes = (ArrayNode) placementJsonNode.get("programmes");
        JsonNode programme = programmes.get(0);
        ArrayNode contracts = (ArrayNode) programme.get("contracts");
        JsonNode contract = contracts.get(0);
        ArrayNode sections = (ArrayNode) contract.get("sections");
        
        assertNotNull(sections, "Sections should be present");
        assertTrue(sections.size() > 0, "Sections should not be empty");

        for (JsonNode section : sections) {
            validateSectionStructure(section);
        }
    }

    private void validateSectionStructure(JsonNode section) {
        // Check required section fields
        String[] requiredFields = {
            "_id", "sequence_number", "status", "period_type",
            "binding_information", "_metadata"
        };
        for (String field : requiredFields) {
            assertTrue(section.has(field), 
                "Section should have required field: " + field);
        }

        // Validate binding information
        JsonNode bindingInfo = section.get("binding_information");
        assertNotNull(bindingInfo, "Section should have binding_information");
        assertTrue(bindingInfo.has("written_line_type"), 
            "Binding information should have written_line_type");
        assertTrue(bindingInfo.has("currency_code"), 
            "Binding information should have currency_code");

        // Validate geographic coverage if present
        if (section.has("geographic_coverage")) {
            JsonNode geoCoverage = section.get("geographic_coverage");
            assertTrue(geoCoverage.has("type"), 
                "Geographic coverage should have type");
            assertTrue(geoCoverage.has("code"), 
                "Geographic coverage should have code");
        }

        // Validate risks if present
        if (section.has("risks")) {
            ArrayNode risks = (ArrayNode) section.get("risks");
            for (JsonNode risk : risks) {
                assertTrue(risk.has("_id"), "Risk should have _id");
                assertTrue(UUID_PATTERN.matcher(risk.get("_id").asText()).matches(), 
                    "Risk _id should be valid UUID");
            }
        }

        // Validate limits if present
        if (section.has("limits")) {
            ArrayNode limits = (ArrayNode) section.get("limits");
            for (JsonNode limit : limits) {
                assertTrue(limit.has("_id"), "Limit should have _id");
                assertTrue(UUID_PATTERN.matcher(limit.get("_id").asText()).matches(), 
                    "Limit _id should be valid UUID");
            }
        }

        // Validate premiums if present
        if (section.has("premiums")) {
            ArrayNode premiums = (ArrayNode) section.get("premiums");
            for (JsonNode premium : premiums) {
                assertTrue(premium.has("_id"), "Premium should have _id");
                assertTrue(UUID_PATTERN.matcher(premium.get("_id").asText()).matches(), 
                    "Premium _id should be valid UUID");
            }
        }
    }

    @Test
    @DisplayName("Should validate submission requests structure")
    void testSubmissionRequestsStructure() {
        ArrayNode submissionRequests = (ArrayNode) placementJsonNode.get("submission_requests");
        assertNotNull(submissionRequests, "Submission requests should be present");
        assertTrue(submissionRequests.size() > 0, "Submission requests should not be empty");

        for (JsonNode submissionRequest : submissionRequests) {
            // Check required fields
            String[] requiredFields = {
                "_id", "_metadata", "type", "status", "name",
                "created_date", "created_by", "broker_team", "total_submissions"
            };
            for (String field : requiredFields) {
                assertTrue(submissionRequest.has(field), 
                    "Submission request should have required field: " + field);
            }

            // Validate created_by user structure
            JsonNode createdBy = submissionRequest.get("created_by");
            assertTrue(createdBy.has("_xid"), "Created by should have _xid");
            assertTrue(createdBy.has("first_name"), "Created by should have first_name");
            assertTrue(createdBy.has("last_name"), "Created by should have last_name");

            // Validate broker team structure
            JsonNode brokerTeam = submissionRequest.get("broker_team");
            assertTrue(brokerTeam.has("_xid"), "Broker team should have _xid");
            assertTrue(brokerTeam.has("name"), "Broker team should have name");

            // Validate approval if present
            if (submissionRequest.has("approval")) {
                ArrayNode approval = (ArrayNode) submissionRequest.get("approval");
                for (JsonNode approvalItem : approval) {
                    assertTrue(approvalItem.has("_id"), "Approval should have _id");
                    assertTrue(approvalItem.has("user"), "Approval should have user");
                    assertTrue(approvalItem.has("sent_date"), "Approval should have sent_date");
                    assertTrue(approvalItem.has("status"), "Approval should have status");
                }
            }
        }
    }

    @Test
    @DisplayName("Should validate submission state structure")
    void testSubmissionStateStructure() {
        JsonNode submissionState = placementJsonNode.get("submission_state");
        assertNotNull(submissionState, "Submission state should be present");

        String[] submissionTypes = {"firm_order", "correction", "additional_information", "quote"};
        for (String type : submissionTypes) {
            if (submissionState.has(type)) {
                JsonNode typeState = submissionState.get(type);
                assertTrue(typeState.has("selected"), 
                    "Submission state " + type + " should have selected");
                assertTrue(typeState.has("active_locks"), 
                    "Submission state " + type + " should have active_locks");
                assertTrue(typeState.has("submission_requests"), 
                    "Submission state " + type + " should have submission_requests");
            }
        }
    }

    @Test
    @DisplayName("Should validate document structure")
    void testDocumentStructure() {
        ArrayNode documents = (ArrayNode) placementJsonNode.get("documents");
        assertNotNull(documents, "Documents should be present");

        for (JsonNode document : documents) {
            assertTrue(document.has("_xid"), "Document should have _xid");
            assertTrue(document.has("name"), "Document should have name");
            assertTrue(UUID_PATTERN.matcher(document.get("_xid").asText()).matches(), 
                "Document _xid should be valid UUID");

            // Validate submission state if present
            if (document.has("submission_state")) {
                JsonNode docSubmissionState = document.get("submission_state");
                String[] submissionTypes = {"firm_order", "correction", "additional_information", "quote"};
                for (String type : submissionTypes) {
                    if (docSubmissionState.has(type)) {
                        JsonNode typeState = docSubmissionState.get(type);
                        assertTrue(typeState.has("selected"), 
                            "Document submission state " + type + " should have selected");
                        assertTrue(typeState.has("locked"), 
                            "Document submission state " + type + " should have locked");
                    }
                }
            }
        }
    }

    @Test
    @DisplayName("Should validate data types match schema")
    void testDataTypesValidation() {
        // Test string fields
        assertTrue(placementJsonNode.get("_id").isTextual(), "_id should be string");
        assertTrue(placementJsonNode.get("client_name").isTextual(), "client_name should be string");
        assertTrue(placementJsonNode.get("description").isTextual(), "description should be string");
        assertTrue(placementJsonNode.get("status").isTextual(), "status should be string");
        assertTrue(placementJsonNode.get("type").isTextual(), "type should be string");

        // Test integer fields
        assertTrue(placementJsonNode.get("effective_year").isInt(), "effective_year should be integer");

        // Test array fields
        assertTrue(placementJsonNode.get("underwriter_pool").isArray(), "underwriter_pool should be array");
        assertTrue(placementJsonNode.get("programmes").isArray(), "programmes should be array");
        assertTrue(placementJsonNode.get("submission_requests").isArray(), "submission_requests should be array");
        assertTrue(placementJsonNode.get("documents").isArray(), "documents should be array");

        // Test object fields
        assertTrue(placementJsonNode.get("_metadata").isObject(), "_metadata should be object");
        assertTrue(placementJsonNode.get("user").isObject(), "user should be object");
        assertTrue(placementJsonNode.get("branch").isObject(), "branch should be object");
        assertTrue(placementJsonNode.get("broker_team").isObject(), "broker_team should be object");
        assertTrue(placementJsonNode.get("submission_state").isObject(), "submission_state should be object");
    }

    @Test
    @DisplayName("Should validate enum values match schema constraints")
    void testEnumValuesValidation() {
        // Test contract type enum
        ArrayNode programmes = (ArrayNode) placementJsonNode.get("programmes");
        JsonNode programme = programmes.get(0);
        ArrayNode contracts = (ArrayNode) programme.get("contracts");
        JsonNode contract = contracts.get(0);
        
        String contractType = contract.get("contract_type").asText();
        assertTrue(contractType.equals("direct_insurance_contract") || 
                  contractType.equals("reinsurance_contract") || 
                  contractType.equals("retrocession_contract"),
            "Contract type should be one of the allowed enum values");

        String coverType = contract.get("cover_type").asText();
        // This is a more complex enum, just check it's not empty
        assertFalse(coverType.isEmpty(), "Cover type should not be empty");

        // Test insured role enum
        ArrayNode sections = (ArrayNode) contract.get("sections");
        JsonNode section = sections.get(0);
        if (section.has("insureds")) {
            ArrayNode insureds = (ArrayNode) section.get("insureds");
            for (JsonNode insured : insureds) {
                String role = insured.get("role").asText();
                assertTrue(role.equals("insured") || 
                          role.equals("reinsured") || 
                          role.equals("retrocedent"),
                    "Insured role should be one of the allowed enum values");
            }
        }
    }
}
