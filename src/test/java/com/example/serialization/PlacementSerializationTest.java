package com.example.serialization;

import com.example.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Placement Serialization and Deserialization Tests")
class PlacementSerializationTest {

    private ObjectMapper objectMapper;
    private String fullPlacementJson;
    private Placement expectedPlacement;

    @BeforeEach
    void setUp() throws IOException {
        objectMapper = new ObjectMapper();
        
        // Load the full placement JSON from test resources
        ClassPathResource resource = new ClassPathResource("fullplacement.json");
        fullPlacementJson = Files.readString(resource.getFile().toPath());
        
        // Create expected placement object for comparison
        expectedPlacement = createExpectedPlacement();
    }

    @Test
    @DisplayName("Should deserialize full placement JSON to Java object")
    void testDeserializeFullPlacement() throws Exception {
        // Deserialize JSON to Placement object
        Placement actualPlacement = objectMapper.readValue(fullPlacementJson, Placement.class);
        
        // Verify top-level fields
        assertNotNull(actualPlacement);
        assertEquals("411bb6ef-0db8-46b6-8dab-225b77bc660e", actualPlacement.getId());
        assertEquals("Client 1", actualPlacement.getClientName());
        assertEquals("Placement description", actualPlacement.getDescription());
        assertEquals(2024, actualPlacement.getEffectiveYear());
        assertEquals("draft", actualPlacement.getStatus());
        assertEquals("Non-Facility", actualPlacement.getType());
        assertEquals("2024-01-01T00:00:00Z", actualPlacement.getInceptionDate());
        
        // Verify metadata
        assertNotNull(actualPlacement.getMetadata());
        assertEquals("2025-09-15T15:38:48.455Z", actualPlacement.getMetadata().getCreationDate());
        assertEquals("OutSystems", actualPlacement.getMetadata().getCreationChannel());
        assertEquals("66992a66-8ac8-4b5c-b420-056f39c0435e", actualPlacement.getMetadata().getCreationUser());
        
        // Verify user
        assertNotNull(actualPlacement.getUser());
        assertEquals("66992a66-8ac8-4b5c-b420-056f39c0435e", actualPlacement.getUser().getXid());
        assertEquals("Jim", actualPlacement.getUser().getFirstName());
        assertEquals("Beard", actualPlacement.getUser().getLastName());
        assertEquals("Riskcare Ltd", actualPlacement.getUser().getOrganisationName());
        assertEquals("Riskcare Ltd", actualPlacement.getUser().getCompanyName());
        
        // Verify branch
        assertNotNull(actualPlacement.getBranch());
        assertEquals("4e1cf5d5-fdae-456c-b12b-3a59a2830124", actualPlacement.getBranch().getXid());
        assertEquals("London Branch", actualPlacement.getBranch().getName());
        
        // Verify broker team
        assertNotNull(actualPlacement.getBrokerTeam());
        assertEquals("8f3f208b-39d7-424e-bf9e-a8afea7f0e60", actualPlacement.getBrokerTeam().getXid());
        assertEquals("London Marine Team", actualPlacement.getBrokerTeam().getName());
        
        // Verify underwriter pool
        assertNotNull(actualPlacement.getUnderwriterPool());
        assertEquals(2, actualPlacement.getUnderwriterPool().size());
        
        UnderwriterPool firstUnderwriter = actualPlacement.getUnderwriterPool().get(0);
        assertEquals("66992a66-8ac8-4b5c-b420-056f39c0435e", firstUnderwriter.getXid());
        assertEquals("Jim", firstUnderwriter.getFirstName());
        assertEquals("Beard", firstUnderwriter.getLastName());
        assertNotNull(firstUnderwriter.getOrganisation());
        assertEquals("e460a816-4e2e-4cca-acbe-50c96f1c9839", firstUnderwriter.getOrganisation().getXid());
        assertEquals("Aegis Insurance Brokers Ltd", firstUnderwriter.getOrganisation().getName());
        
        // Verify placement read access
        assertNotNull(actualPlacement.getPlacementReadAccess());
        assertEquals(1, actualPlacement.getPlacementReadAccess().size());
        assertEquals("66992a66-8ac8-4b5c-b420-056f39c0435e", actualPlacement.getPlacementReadAccess().get(0));
        
        // Verify programmes
        assertNotNull(actualPlacement.getProgrammes());
        assertEquals(1, actualPlacement.getProgrammes().size());
        
        Programme programme = actualPlacement.getProgrammes().get(0);
        assertEquals("1fdd832b-41bb-460c-a814-371b1f85c3cc", programme.getId());
        assertEquals("SS Britannia - Hull Insurance", programme.getDescription());
        assertEquals("2024-05-18T16:00:00Z", programme.getInceptionDate());
        assertEquals("Firm Order", programme.getStatusCode());
        assertEquals(101, programme.getSequenceNumber());
        
        // Verify programme contracts
        assertNotNull(programme.getContracts());
        assertEquals(1, programme.getContracts().size());
        
        Contract contract = programme.getContracts().get(0);
        assertEquals("c4512d7c-7aa8-4625-9cf8-eda84afe7a13", contract.getId());
        assertEquals("55931e1dba3b45c8a640658023b93b66", contract.getBrokerCode());
        assertEquals("B0999ABC123456789v1", contract.getBrokerContractRef());
        assertEquals("B55931e1dba3b45c8a640658023b93b66B0999ABC123456789v1", contract.getContractUmr());
        assertEquals(1, contract.getSequenceNumber());
        assertEquals("This contract pertains to single participant aviation cover", contract.getDescription());
        assertEquals("direct_insurance_contract", contract.getContractType());
        assertEquals("master_cover", contract.getCoverType());
        assertEquals("draft", contract.getStatus());
        assertEquals("draft", contract.getSubStatus());
        assertEquals(1, contract.getVersion());
        assertFalse(contract.getUsesDigitalContract());
        assertFalse(contract.getCancelAndReplaceAllowed());
        
        // Verify contract insureds
        assertNotNull(contract.getInsureds());
        assertEquals(1, contract.getInsureds().size());
        
        Insured insured = contract.getInsureds().get(0);
        assertEquals("2be29166-0b73-432c-951b-2d9911acca26", insured.getId());
        assertEquals("British Airways PLC", insured.getName());
        assertEquals("insured", insured.getRole());
        
        // Verify contract sections
        assertNotNull(contract.getSections());
        assertEquals(1, contract.getSections().size());
        
        Section section = contract.getSections().get(0);
        assertEquals("d6a1ca07-5bad-41b1-8c65-3e917477d0d6", section.getId());
        assertEquals(1, section.getSequenceNumber());
        assertEquals("active", section.getStatus());
        assertEquals("dates", section.getPeriodType());
        assertEquals("I", section.getReference());
        assertEquals("All Risks of Physical Damage (including Machinery Breakdown and Boiler Explosion)", section.getDescription());
        assertEquals("direct_insurance_contract", section.getClassificationType());
        assertEquals("master_cover", section.getCoverType());
        assertEquals("001", section.getProductCode());
        assertEquals("2024-01-01T00:00:00Z", section.getInceptionDate());
        assertEquals("2024-12-31T23:59:59Z", section.getExpiryDate());
        assertEquals(12, section.getPeriodDurationNumber());
        assertEquals("months", section.getDurationUnitTypeCode());
        assertEquals("date_included", section.getPeriodQualifierTypeCode());
        assertEquals(100.0, section.getOrderPercentage());
        assertEquals("Property", section.getLineOfBusiness());
        assertEquals("Commercial Property", section.getClassOfBusiness());
        assertEquals("2024-06-01T12:00:00Z", section.getSettlementDueDate());
        assertEquals(30, section.getInstallmentPeriodOfCredit());
        assertEquals(60, section.getAdjustmentPeriodOfCredit());
        assertEquals("UK", section.getStampPermissionType());
        
        // Verify geographic coverage
        assertNotNull(section.getGeographicCoverage());
        assertEquals("country", section.getGeographicCoverage().getType());
        assertEquals("gb", section.getGeographicCoverage().getCode());
        assertEquals("Offices of the insured in the UK, Spain, New Zealand and Austria", section.getGeographicCoverageDescription());
        
        // Verify binding information
        assertNotNull(section.getBindingInformation());
        assertEquals("percentage", section.getBindingInformation().getWrittenLineType());
        assertEquals("GBP", section.getBindingInformation().getCurrencyCode());
        assertEquals("percentage_of_whole", section.getBindingInformation().getWrittenLineBasis());
        assertEquals("percentage_of_whole", section.getBindingInformation().getSignedLineBasis());
        assertEquals(2, section.getBindingInformation().getSignedDownDecimalPlaces());
        assertEquals(100.0, section.getBindingInformation().getSignedDownOrderPercentage());
        
        // Verify section risks
        assertNotNull(section.getRisks());
        assertEquals(1, section.getRisks().size());
        
        Risk risk = section.getRisks().get(0);
        assertEquals("ef05b43e-ae3e-4805-b654-dd95bee88e3d", risk.getId());
        assertEquals("6T", risk.getRiskCode());
        assertEquals("GBP", risk.getCurrencyCode());
        assertEquals(1000000.0, risk.getAmount());
        
        // Verify section limits
        assertNotNull(section.getLimits());
        assertEquals(1, section.getLimits().size());
        
        Limit limit = section.getLimits().get(0);
        assertEquals("92028c4a-d0d8-469d-b6df-ecd3c972b8ed", limit.getId());
        assertEquals("sum_insured", limit.getTypeRef());
        assertEquals("GBP", limit.getCurrencyCode());
        assertEquals(5000000.0, limit.getAmount());
        assertNotNull(limit.getBasisRefs());
        assertEquals(1, limit.getBasisRefs().size());
        assertEquals("any_one_occurrence", limit.getBasisRefs().get(0));
        assertEquals("Coverage for any one occurrence", limit.getSpecification());
        
        // Verify section excesses
        assertNotNull(section.getExcesses());
        assertEquals(1, section.getExcesses().size());
        
        Excess excess = section.getExcesses().get(0);
        assertEquals("a1155bd7-26fb-47a7-a8e5-b097f1a49b35", excess.getId());
        assertEquals("loss_deductible", excess.getType());
        assertEquals("GBP", excess.getCurrencyCode());
        assertEquals(10000.0, excess.getAmount());
        assertNotNull(excess.getBasisRefs());
        assertEquals(1, excess.getBasisRefs().size());
        assertEquals("any_one_claim", excess.getBasisRefs().get(0));
        assertEquals("Standard deductible per claim", excess.getSpecification());
        
        // Verify section deductibles
        assertNotNull(section.getDeductibles());
        assertEquals(1, section.getDeductibles().size());
        
        Deductible deductible = section.getDeductibles().get(0);
        assertEquals("3a4f7b38-5854-433d-941e-dd4ff9df4078", deductible.getId());
        assertEquals("loss_deductible", deductible.getTypeRef());
        assertEquals("GBP", deductible.getCurrencyCode());
        assertEquals(5000.0, deductible.getAmount());
        assertNotNull(deductible.getBasisRefs());
        assertEquals(1, deductible.getBasisRefs().size());
        assertEquals("any_one_occurrence", deductible.getBasisRefs().get(0));
        assertEquals("Standard deductible per occurrence", deductible.getSpecification());
        
        // Verify section premiums
        assertNotNull(section.getPremiums());
        assertEquals(1, section.getPremiums().size());
        
        Premium premium = section.getPremiums().get(0);
        assertEquals("6d099f04-3db0-43f5-9171-b81b10c3a3b5", premium.getId());
        assertEquals("premium", premium.getTypeRef());
        assertEquals("GBP", premium.getCurrencyCode());
        assertEquals(50000.0, premium.getAmount());
        assertEquals(1.0, premium.getRate());
        assertEquals("percentage", premium.getRateUnitCode());
        assertNotNull(premium.getBasisRefs());
        assertEquals(1, premium.getBasisRefs().size());
        assertEquals("gross_written_premium", premium.getBasisRefs().get(0));
        assertEquals("N", premium.getDiscountAppliedIndicator());
        
        // Verify section status flags
        assertNotNull(section.getStatusFlags());
        assertEquals(1, section.getStatusFlags().size());
        assertEquals("active", section.getStatusFlags().get(0));
        
        // Verify submission requests
        assertNotNull(actualPlacement.getSubmissionRequests());
        assertEquals(3, actualPlacement.getSubmissionRequests().size());
        
        SubmissionRequest submissionRequest = actualPlacement.getSubmissionRequests().get(0);
        assertEquals("f52ffe9d-c4a2-423f-adaf-fd4e8113f430", submissionRequest.getId());
        assertEquals("firm_order", submissionRequest.getType());
        assertEquals("draft", submissionRequest.getStatus());
        assertEquals("Firm Order Submission - Q1 2024", submissionRequest.getName());
        assertEquals("Please review the attached documents for firm order submission", submissionRequest.getGeneralMessage());
        assertEquals("2025-09-15T15:38:48.455Z", submissionRequest.getCreatedDate());
        assertEquals(5, submissionRequest.getTotalSubmissions());
        
        // Verify submission request approval
        assertNotNull(submissionRequest.getApproval());
        assertEquals(1, submissionRequest.getApproval().size());
        
        SubmissionRequest.Approval approval = submissionRequest.getApproval().get(0);
        assertEquals("6d7a24d2-4ad7-4735-9119-37ebd241e529", approval.getId());
        assertEquals("2025-09-15T15:38:48.455Z", approval.getSentDate());
        assertEquals("R", approval.getStatus());
        assertNotNull(approval.getUser());
        assertEquals("66992a66-8ac8-4b5c-b420-056f39c0435e", approval.getUser().getXid());
        assertEquals("Jim", approval.getUser().getFirstName());
        assertEquals("Beard", approval.getUser().getLastName());
        
        // Verify documents
        assertNotNull(actualPlacement.getDocuments());
        assertEquals(1, actualPlacement.getDocuments().size());
        
        Document document = actualPlacement.getDocuments().get(0);
        assertEquals("362c05fb-ed59-4679-8b90-b20925a00b68", document.getXid());
        assertEquals("ABC Corp Intl Casualty.docx", document.getName());
        
        // Verify document submission state
        assertNotNull(document.getSubmissionState());
        assertNotNull(document.getSubmissionState().getFirmOrder());
        assertTrue(document.getSubmissionState().getFirmOrder().getSelected());
        assertFalse(document.getSubmissionState().getFirmOrder().getLocked());
        assertNotNull(document.getSubmissionState().getFirmOrder().getSubmissionRequests());
        assertEquals(1, document.getSubmissionState().getFirmOrder().getSubmissionRequests().size());
        assertEquals("f52ffe9d-c4a2-423f-adaf-fd4e8113f430", document.getSubmissionState().getFirmOrder().getSubmissionRequests().get(0));
        
        // Verify placement submission state
        assertNotNull(actualPlacement.getSubmissionState());
        assertNotNull(actualPlacement.getSubmissionState().getFirmOrder());
        assertTrue(actualPlacement.getSubmissionState().getFirmOrder().getSelected());
        assertEquals(1, actualPlacement.getSubmissionState().getFirmOrder().getActiveLocks());
        assertNotNull(actualPlacement.getSubmissionState().getFirmOrder().getSubmissionRequests());
        assertEquals(1, actualPlacement.getSubmissionState().getFirmOrder().getSubmissionRequests().size());
        assertEquals("f52ffe9d-c4a2-423f-adaf-fd4e8113f430", actualPlacement.getSubmissionState().getFirmOrder().getSubmissionRequests().get(0));
    }

    @Test
    @DisplayName("Should serialize Java object to JSON matching original structure")
    void testSerializePlacementToJson() throws Exception {
        // Deserialize the original JSON to get a complete object
        Placement placement = objectMapper.readValue(fullPlacementJson, Placement.class);
        
        // Serialize back to JSON
        String serializedJson = objectMapper.writeValueAsString(placement);
        
        // Parse both JSONs to compare structure
        JsonNode originalJson = objectMapper.readTree(fullPlacementJson);
        JsonNode serializedJsonNode = objectMapper.readTree(serializedJson);
        
        // Verify all top-level fields are present
        assertTrue(serializedJsonNode.has("_id"));
        assertTrue(serializedJsonNode.has("_metadata"));
        assertTrue(serializedJsonNode.has("user"));
        assertTrue(serializedJsonNode.has("underwriter_pool"));
        assertTrue(serializedJsonNode.has("placement_read_access"));
        assertTrue(serializedJsonNode.has("branch"));
        assertTrue(serializedJsonNode.has("client_name"));
        assertTrue(serializedJsonNode.has("description"));
        assertTrue(serializedJsonNode.has("effective_year"));
        assertTrue(serializedJsonNode.has("broker_team"));
        assertTrue(serializedJsonNode.has("programmes"));
        assertTrue(serializedJsonNode.has("inception_date"));
        assertTrue(serializedJsonNode.has("status"));
        assertTrue(serializedJsonNode.has("submission_requests"));
        assertTrue(serializedJsonNode.has("documents"));
        assertTrue(serializedJsonNode.has("submission_state"));
        assertTrue(serializedJsonNode.has("type"));
        
        // Verify key values match
        assertEquals(originalJson.get("_id").asText(), serializedJsonNode.get("_id").asText());
        assertEquals(originalJson.get("client_name").asText(), serializedJsonNode.get("client_name").asText());
        assertEquals(originalJson.get("description").asText(), serializedJsonNode.get("description").asText());
        assertEquals(originalJson.get("effective_year").asInt(), serializedJsonNode.get("effective_year").asInt());
        assertEquals(originalJson.get("status").asText(), serializedJsonNode.get("status").asText());
        assertEquals(originalJson.get("type").asText(), serializedJsonNode.get("type").asText());
        
        // Verify nested structures are preserved
        assertTrue(serializedJsonNode.get("programmes").isArray());
        assertEquals(1, serializedJsonNode.get("programmes").size());
        
        JsonNode programme = serializedJsonNode.get("programmes").get(0);
        assertTrue(programme.has("contracts"));
        assertTrue(programme.get("contracts").isArray());
        assertEquals(1, programme.get("contracts").size());
        
        JsonNode contract = programme.get("contracts").get(0);
        assertTrue(contract.has("sections"));
        assertTrue(contract.get("sections").isArray());
        assertEquals(1, contract.get("sections").size());
        
        JsonNode section = contract.get("sections").get(0);
        assertTrue(section.has("risks"));
        assertTrue(section.has("limits"));
        assertTrue(section.has("excesses"));
        assertTrue(section.has("deductibles"));
        assertTrue(section.has("premiums"));
        assertTrue(section.has("binding_information"));
        assertTrue(section.has("geographic_coverage"));
    }

    @Test
    @DisplayName("Should handle null and empty collections gracefully")
    void testNullAndEmptyHandling() throws Exception {
        // Create a minimal placement with null/empty fields
        Placement minimalPlacement = new Placement();
        minimalPlacement.setId("test-id");
        minimalPlacement.setClientName("Test Client");
        minimalPlacement.setDescription("Test Description");
        minimalPlacement.setEffectiveYear(2024);
        minimalPlacement.setStatus("draft");
        minimalPlacement.setType("Non-Facility");
        
        // Serialize and deserialize
        String json = objectMapper.writeValueAsString(minimalPlacement);
        Placement deserialized = objectMapper.readValue(json, Placement.class);
        
        // Verify null/empty collections are handled
        assertNotNull(deserialized.getPlacementReadAccess());
        assertNotNull(deserialized.getUnderwriterPool());
        assertNotNull(deserialized.getDocuments());
        assertNotNull(deserialized.getProgrammes());
        assertNotNull(deserialized.getSubmissionRequests());
        
        // Verify basic fields are preserved
        assertEquals("test-id", deserialized.getId());
        assertEquals("Test Client", deserialized.getClientName());
        assertEquals("Test Description", deserialized.getDescription());
        assertEquals(2024, deserialized.getEffectiveYear());
        assertEquals("draft", deserialized.getStatus());
        assertEquals("Non-Facility", deserialized.getType());
    }

    @Test
    @DisplayName("Should validate all required schema fields are present")
    void testSchemaCompliance() throws Exception {
        Placement placement = objectMapper.readValue(fullPlacementJson, Placement.class);
        
        // Verify all required fields from schema are present
        assertNotNull(placement.getId(), "_id is required");
        assertNotNull(placement.getMetadata(), "_metadata is required");
        assertNotNull(placement.getUser(), "user is required");
        assertNotNull(placement.getPlacementReadAccess(), "placement_read_access is required");
        assertNotNull(placement.getBranch(), "branch is required");
        assertNotNull(placement.getClientName(), "client_name is required");
        assertNotNull(placement.getDescription(), "description is required");
        assertNotNull(placement.getEffectiveYear(), "effective_year is required");
        assertNotNull(placement.getBrokerTeam(), "broker_team is required");
        assertNotNull(placement.getInceptionDate(), "inception_date is required");
        assertNotNull(placement.getType(), "type is required");
        
        // Verify metadata has all required fields
        Metadata metadata = placement.getMetadata();
        assertNotNull(metadata.getCreationDate(), "creation_date is required");
        assertNotNull(metadata.getCreationChannel(), "creation_channel is required");
        assertNotNull(metadata.getCreationUser(), "creation_user is required");
        assertNotNull(metadata.getModifiedDate(), "modified_date is required");
        assertNotNull(metadata.getModifiedChannel(), "modified_channel is required");
        assertNotNull(metadata.getModifiedUser(), "modified_user is required");
        
        // Verify user has all required fields
        User user = placement.getUser();
        assertNotNull(user.getXid(), "user._xid is required");
        assertNotNull(user.getFirstName(), "user.first_name is required");
        assertNotNull(user.getLastName(), "user.last_name is required");
        assertNotNull(user.getOrganisationName(), "user.organisation_name is required");
        assertNotNull(user.getCompanyName(), "user.company_name is required");
        assertNotNull(user.getCompanyXid(), "user.company_xid is required");
        assertNotNull(user.getOrganisationXid(), "user.organisation_xid is required");
        
        // Verify branch has all required fields
        Branch branch = placement.getBranch();
        assertNotNull(branch.getXid(), "branch._xid is required");
        assertNotNull(branch.getName(), "branch.name is required");
        
        // Verify broker team has all required fields
        BrokerTeam brokerTeam = placement.getBrokerTeam();
        assertNotNull(brokerTeam.getXid(), "broker_team._xid is required");
        assertNotNull(brokerTeam.getName(), "broker_team.name is required");
    }

    @Test
    @DisplayName("Should handle complex nested structures correctly")
    void testComplexNestedStructures() throws Exception {
        Placement placement = objectMapper.readValue(fullPlacementJson, Placement.class);
        
        // Verify programme structure
        Programme programme = placement.getProgrammes().get(0);
        assertNotNull(programme.getBrokerTeam());
        assertNotNull(programme.getUser());
        assertNotNull(programme.getDocuments());
        assertNotNull(programme.getContracts());
        assertNotNull(programme.getSubmissionState());
        assertNotNull(programme.getMetadata());
        
        // Verify contract structure
        Contract contract = programme.getContracts().get(0);
        assertNotNull(contract.getBrokerTeam());
        assertNotNull(contract.getUser());
        assertNotNull(contract.getInsureds());
        assertNotNull(contract.getDocuments());
        assertNotNull(contract.getSections());
        assertNotNull(contract.getSubmissionState());
        assertNotNull(contract.getMetadata());
        assertNotNull(contract.getStatusFlags());
        
        // Verify section structure
        Section section = contract.getSections().get(0);
        assertNotNull(section.getGeographicCoverage());
        assertNotNull(section.getInsureds());
        assertNotNull(section.getRisks());
        assertNotNull(section.getLimits());
        assertNotNull(section.getExcesses());
        assertNotNull(section.getDeductibles());
        assertNotNull(section.getPremiums());
        assertNotNull(section.getBindingInformation());
        assertNotNull(section.getDocuments());
        assertNotNull(section.getSubmissionState());
        assertNotNull(section.getMetadata());
        assertNotNull(section.getStatusFlags());
        assertNotNull(section.getFacilityUsage());
        
        // Verify submission request structure
        SubmissionRequest submissionRequest = placement.getSubmissionRequests().get(0);
        assertNotNull(submissionRequest.getMetadata());
        assertNotNull(submissionRequest.getCreatedBy());
        assertNotNull(submissionRequest.getBrokerTeam());
        assertNotNull(submissionRequest.getApproval());
        
        // Verify approval structure
        SubmissionRequest.Approval approval = submissionRequest.getApproval().get(0);
        assertNotNull(approval.getUser());
    }

    private Placement createExpectedPlacement() {
        Placement placement = new Placement();
        placement.setId("411bb6ef-0db8-46b6-8dab-225b77bc660e");
        placement.setClientName("Client 1");
        placement.setDescription("Placement description");
        placement.setEffectiveYear(2024);
        placement.setStatus("draft");
        placement.setType("Non-Facility");
        placement.setInceptionDate("2024-01-01T00:00:00Z");
        
        // Set metadata
        Metadata metadata = new Metadata();
        metadata.setCreationDate("2025-09-15T15:38:48.455Z");
        metadata.setCreationChannel("OutSystems");
        metadata.setCreationUser("66992a66-8ac8-4b5c-b420-056f39c0435e");
        metadata.setModifiedDate("2025-09-15T15:38:48.455Z");
        metadata.setModifiedChannel("OutSystems");
        metadata.setModifiedUser("66992a66-8ac8-4b5c-b420-056f39c0435e");
        placement.setMetadata(metadata);
        
        // Set user
        User user = new User();
        user.setXid("66992a66-8ac8-4b5c-b420-056f39c0435e");
        user.setFirstName("Jim");
        user.setLastName("Beard");
        user.setOrganisationName("Riskcare Ltd");
        user.setCompanyName("Riskcare Ltd");
        user.setCompanyXid("46fcc299-13ea-4721-b049-d5a7e6124927");
        user.setOrganisationXid("46fcc299-13ea-4721-b049-d5a7e6124927");
        placement.setUser(user);
        
        // Set branch
        Branch branch = new Branch();
        branch.setXid("4e1cf5d5-fdae-456c-b12b-3a59a2830124");
        branch.setName("London Branch");
        placement.setBranch(branch);
        
        // Set broker team
        BrokerTeam brokerTeam = new BrokerTeam();
        brokerTeam.setXid("8f3f208b-39d7-424e-bf9e-a8afea7f0e60");
        brokerTeam.setName("London Marine Team");
        placement.setBrokerTeam(brokerTeam);
        
        // Initialize collections
        placement.setPlacementReadAccess(new ArrayList<>());
        placement.setUnderwriterPool(new ArrayList<>());
        placement.setDocuments(new ArrayList<>());
        placement.setProgrammes(new ArrayList<>());
        placement.setSubmissionRequests(new ArrayList<>());
        
        return placement;
    }
}
