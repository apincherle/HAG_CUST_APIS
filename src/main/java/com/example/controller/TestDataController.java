package com.example.controller;

import com.example.config.TestDataInitializer;
import com.example.model.Submission;
import com.example.model.SubmissionItem;
import com.example.repository.CustomerRepository;
import com.example.repository.SubmissionItemRepository;
import com.example.repository.SubmissionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/admin")
@Tag(name = "Admin", description = "Administrative endpoints")
public class TestDataController {

    @Autowired
    private TestDataInitializer testDataInitializer;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private SubmissionRepository submissionRepository;
    
    @Autowired
    private SubmissionItemRepository submissionItemRepository;

    @PostMapping("/init-test-data")
    @Operation(summary = "Initialize test data from CSV", description = "Loads customer data from customers.csv file and creates test submissions, items, and certificates. Safe to call multiple times - skips existing data.")
    public ResponseEntity<Map<String, Object>> initializeTestData() {
        try {
            int customersCreated = testDataInitializer.initializeTestData();
            
            // Also initialize submissions, items, and certificates
            testDataInitializer.initializeSubmissionsAndCertificates();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("customersCreated", customersCreated);
            response.put("message", "Test data initialization completed. " + customersCreated + " customer(s) created, and submissions/items/certificates initialized.");
            
            // Also verify the customer can be found
            if (customersCreated > 0) {
                java.util.UUID testCustomerId = java.util.UUID.fromString("95240174-43c0-4f75-a716-a2f701e7c9fd");
                boolean exists = customerRepository.findByCustomerIdNative(testCustomerId.toString()).isPresent();
                response.put("customerVerified", exists);
                response.put("customerId", testCustomerId.toString());
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            if (e.getCause() != null) {
                response.put("cause", e.getCause().getMessage());
            }
            e.printStackTrace();
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @GetMapping("/test-data-ids")
    @Operation(summary = "Get test data IDs", description = "Returns sample submission, customer, and item IDs from test data that can be used for testing QR certificate generation")
    public ResponseEntity<Map<String, Object>> getTestDataIds() {
        try {
            Map<String, Object> response = new HashMap<>();
            List<Map<String, Object>> samples = new ArrayList<>();
            
            // Find test data submissions (those with submission numbers starting with "SUB-")
            List<Submission> testSubmissions = submissionRepository.findAll().stream()
                .filter(s -> s.getSubmissionNumber() != null && s.getSubmissionNumber().startsWith("SUB-"))
                .limit(5)
                .toList();
            
            for (Submission submission : testSubmissions) {
                List<SubmissionItem> items = submissionItemRepository.findBySubmission_SubmissionId(submission.getSubmissionId());
                
                for (SubmissionItem item : items) {
                    Map<String, Object> sample = new HashMap<>();
                    sample.put("submissionId", submission.getSubmissionId().toString());
                    sample.put("customerId", submission.getCustomerId().toString());
                    sample.put("itemId", item.getItemId().toString());
                    sample.put("submissionNumber", submission.getSubmissionNumber());
                    sample.put("submissionStatus", submission.getStatus().getDisplayValue());
                    sample.put("itemFreeText", item.getFreeTextLine());
                    sample.put("itemGame", item.getGame().toString());
                    samples.add(sample);
                }
            }
            
            response.put("success", true);
            response.put("samples", samples);
            response.put("count", samples.size());
            response.put("message", "Use these IDs in the QR certificate generate endpoint");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            if (e.getCause() != null) {
                response.put("cause", e.getCause().getMessage());
            }
            return ResponseEntity.status(500).body(response);
        }
    }
}

