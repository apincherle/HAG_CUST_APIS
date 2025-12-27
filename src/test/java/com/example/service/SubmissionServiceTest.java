package com.example.service;

import com.example.dto.SubmissionCreateRequestLite;
import com.example.dto.SubmissionCreateResponseLite;
import com.example.dto.SubmissionItemCreateLite;
import com.example.model.Submission;
import com.example.model.SubmissionItem;
import com.example.repository.CustomerRepository;
import com.example.repository.SubmissionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("dev")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:postgresql://localhost:5432/hags_customer",
    "spring.datasource.username=hags_user",
    "spring.datasource.password=hags_password",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class SubmissionServiceTest {

    @Autowired
    private SubmissionService submissionService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Test
    @Transactional
    public void testCreateSubmission_WithValidData() {
        // Given: Valid submission data matching the curl request
        UUID customerId = UUID.fromString("95240174-43c0-4f75-a716-a2f701e7c9fd");
        
        // Create test customer (using create-drop, so need to recreate)
        // In real tests with update mode, TestDataInitializer will load from CSV
        com.example.model.Customer customer = new com.example.model.Customer();
        customer.setCustomerId(customerId);
        customer.setEmail("a@b.com");
        customer.setPhone("07817700059");
        customer.setFullName("andfrew pincherle");
        customer.setMarketingOptIn(true);
        customer.setStatus(com.example.model.Customer.CustomerStatus.ACTIVE);
        customerRepository.save(customer);
        customerRepository.flush();
        
        SubmissionCreateRequestLite request = new SubmissionCreateRequestLite();
        request.setCustomerId(customerId);
        request.setServiceLevel(Submission.ServiceLevel.BRONZE);
        request.setNotesCustomer("first sub");
        
        List<SubmissionItemCreateLite> items = new ArrayList<>();
        SubmissionItemCreateLite item = new SubmissionItemCreateLite();
        item.setGame(SubmissionItem.GameType.POKEMON);
        item.setFreeTextLine("charzrd1");
        item.setCustomerNotes("big dragon 1");
        item.setRequestedPhotoSlots(2);
        item.setFrontPhotoId("pic1.jpg");
        item.setBackPhotoId("pic2.jpg");
        items.add(item);
        request.setItems(items);

        // When: Creating the submission
        SubmissionCreateResponseLite response = submissionService.createSubmission(request);

        // Then: Submission should be created successfully
        assertNotNull(response, "Response should not be null");
        assertNotNull(response.getSubmission(), "Submission should not be null");
        assertNotNull(response.getSubmission().getSubmissionId(), "Submission ID should not be null");
        assertEquals(customerId, response.getSubmission().getCustomerId(), 
            "Customer ID should match");
        assertEquals(Submission.ServiceLevel.BRONZE, response.getSubmission().getServiceLevel(), 
            "Service level should be BRONZE");
        assertEquals("first sub", response.getSubmission().getNotesCustomer(), 
            "Notes should match");
        assertEquals(Submission.SubmissionStatus.DRAFT, response.getSubmission().getStatus(), 
            "Status should be DRAFT");
        
        // Verify items
        assertNotNull(response.getItems(), "Items should not be null");
        assertEquals(1, response.getItems().size(), "Should have 1 item");
        com.example.dto.SubmissionItemLite responseItem = response.getItems().get(0);
        assertEquals(SubmissionItem.GameType.POKEMON, responseItem.getGame(), 
            "Game should be POKEMON");
        assertEquals("charzrd1", responseItem.getFreeTextLine(), 
            "Free text line should match");
        assertEquals("big dragon 1", responseItem.getCustomerNotes(), 
            "Customer notes should match");
        assertEquals(2, responseItem.getRequestedPhotoSlots(), 
            "Requested photo slots should be 2");
        assertEquals("pic1.jpg", responseItem.getFrontPhotoId(), 
            "Front photo ID should match");
        assertEquals("pic2.jpg", responseItem.getBackPhotoId(), 
            "Back photo ID should match");
        
        // Verify intake code
        assertNotNull(response.getIntakeCode(), "Intake code should not be null");
        assertNotNull(response.getIntakeCode().getValue(), "Intake code value should not be null");
    }
}
