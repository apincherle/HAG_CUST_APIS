package com.example.service;

import com.example.dto.*;
import com.example.dto.SubmissionIntakeCodeDto;
import com.example.model.Submission;
import com.example.model.SubmissionItem;
import com.example.model.SubmissionIntakeCode;
import com.example.repository.CustomerRepository;
import com.example.repository.SubmissionRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SubmissionService {
    
    @Autowired
    private SubmissionRepository submissionRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Transactional
    public SubmissionCreateResponseLite createSubmission(SubmissionCreateRequestLite request) {
        System.out.println("DEBUG SubmissionService.createSubmission: Starting");
        System.out.println("DEBUG SubmissionService.createSubmission: Request customerId: " + request.getCustomerId());
        
        // Verify customer exists using native query to avoid UUID conversion issues
        String customerIdString = request.getCustomerId().toString();
        System.out.println("DEBUG SubmissionService: Looking for customer with ID string: '" + customerIdString + "'");
        
        try {
            java.util.Optional<com.example.model.Customer> customerOpt = customerRepository.findByCustomerIdNative(customerIdString);
            if (!customerOpt.isPresent()) {
                System.out.println("DEBUG SubmissionService: Customer not found with ID: " + customerIdString);
                System.out.println("DEBUG SubmissionService: Total customers in DB: " + customerRepository.count());
                
                // Try to list all customer IDs for debugging - use native query to see actual stored values
                try {
                    Query debugQuery = entityManager.createNativeQuery("SELECT customer_id, email FROM customers LIMIT 10");
                    @SuppressWarnings("unchecked")
                    java.util.List<Object[]> results = debugQuery.getResultList();
                    System.out.println("DEBUG SubmissionService: Actual customers in DB:");
                    for (Object[] row : results) {
                        String storedId = row[0] != null ? String.valueOf(row[0]) : "null";
                        System.out.println("  - customer_id (as stored): '" + storedId + "' (type: " + (row[0] != null ? row[0].getClass().getName() : "null") + "), email: " + row[1]);
                        System.out.println("  - Searching for: '" + customerIdString + "'");
                        System.out.println("  - Exact match: " + customerIdString.equals(storedId));
                        System.out.println("  - Ignore case match: " + customerIdString.equalsIgnoreCase(storedId));
                        System.out.println("  - Trimmed stored: '" + storedId.trim() + "', match: " + customerIdString.equals(storedId.trim()));
                    }
                } catch (Exception e) {
                    System.err.println("DEBUG SubmissionService: Error listing customers: " + e.getMessage());
                    e.printStackTrace();
                }
                
                String errorMsg = "Customer not found with ID: " + customerIdString + ". Please ensure customer exists. Call POST /v1/admin/init-test-data to initialize test data.";
                System.err.println("DEBUG SubmissionService: Throwing ResponseStatusException: " + errorMsg);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMsg);
            }
            
            System.out.println("DEBUG SubmissionService: Customer found: " + customerOpt.get().getEmail());
            System.out.println("DEBUG SubmissionService: Customer ID verified: " + customerOpt.get().getCustomerId());
        } catch (ResponseStatusException e) {
            System.err.println("DEBUG SubmissionService: Re-throwing ResponseStatusException: " + e.getStatusCode() + " - " + e.getReason());
            throw e;
        } catch (Exception e) {
            System.err.println("DEBUG SubmissionService: Unexpected error checking customer: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Error checking customer: " + e.getMessage());
        }
        
        // Create submission
        Submission submission = new Submission();
        submission.setCustomerId(request.getCustomerId());
        submission.setServiceLevel(request.getServiceLevel() != null ? request.getServiceLevel() : Submission.ServiceLevel.BRONZE);
        // shippingAddressId removed - will use customer's shipping address when needed
        submission.setNotesCustomer(request.getNotesCustomer());
        submission.setStatus(Submission.SubmissionStatus.DRAFT);
        
        // Generate submission number
        String submissionNumber = "SUB-" + System.currentTimeMillis();
        submission.setSubmissionNumber(submissionNumber);
        
        // Create items
        List<SubmissionItem> items = new java.util.ArrayList<>();
        for (int i = 0; i < request.getItems().size(); i++) {
            SubmissionItemCreateLite itemRequest = request.getItems().get(i);
            SubmissionItem item = new SubmissionItem();
            item.setSubmission(submission);
            item.setLineNumber(i + 1);
            item.setGame(itemRequest.getGame());
            item.setFreeTextLine(itemRequest.getFreeTextLine());
            item.setCustomerNotes(itemRequest.getCustomerNotes());
            item.setRequestedPhotoSlots(itemRequest.getRequestedPhotoSlots() != null ? 
                    itemRequest.getRequestedPhotoSlots() : 2);
            item.setFrontPhotoId(itemRequest.getFrontPhotoId());
            item.setBackPhotoId(itemRequest.getBackPhotoId());
            item.setEnrichmentStatus(SubmissionItem.EnrichmentStatus.PENDING);
            items.add(item);
        }
        submission.setItems(items);
        
        // Create intake code
        SubmissionIntakeCode intakeCode = new SubmissionIntakeCode();
        intakeCode.setSubmission(submission);
        intakeCode.setValue(submissionNumber);
        intakeCode.setBarcodeFormat(SubmissionIntakeCode.BarcodeFormat.CODE_128);
        intakeCode.setQrValue("https://api.yourdomain.com/submissions/" + submission.getSubmissionId());
        submission.setIntakeCode(intakeCode);
        
        submission = submissionRepository.save(submission);
        
        // Build response
        List<SubmissionItemLite> itemLites = submission.getItems().stream()
                .map(SubmissionItemLite::fromEntity)
                .collect(Collectors.toList());
        
        return SubmissionCreateResponseLite.builder()
                .submission(SubmissionLite.fromEntity(submission))
                .intakeCode(SubmissionIntakeCodeDto.fromEntity(submission.getIntakeCode()))
                .items(itemLites)
                .build();
    }
    
    public SubmissionLite getSubmissionById(UUID submissionId) {
        Submission submission = submissionRepository.findBySubmissionIdAndStatusNot(
                submissionId, Submission.SubmissionStatus.CANCELLED)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Submission not found"));
        return SubmissionLite.fromEntity(submission);
    }
    
    public List<SubmissionLite> getSubmissionsByCustomerId(UUID customerId) {
        List<Submission> submissions = submissionRepository.findByCustomerIdAndStatusNot(
                customerId, Submission.SubmissionStatus.CANCELLED);
        return submissions.stream()
                .map(SubmissionLite::fromEntity)
                .collect(Collectors.toList());
    }
}

