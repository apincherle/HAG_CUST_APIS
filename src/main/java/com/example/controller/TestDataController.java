package com.example.controller;

import com.example.config.TestDataInitializer;
import com.example.repository.CustomerRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v1/admin")
@Tag(name = "Admin", description = "Administrative endpoints")
public class TestDataController {

    @Autowired
    private TestDataInitializer testDataInitializer;
    
    @Autowired
    private CustomerRepository customerRepository;

    @PostMapping("/init-test-data")
    @Operation(summary = "Initialize test data from CSV", description = "Loads customer data from customers.csv file. Safe to call multiple times - skips existing customers.")
    public ResponseEntity<Map<String, Object>> initializeTestData() {
        try {
            int created = testDataInitializer.initializeTestData();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("customersCreated", created);
            response.put("message", "Test data initialization completed. " + created + " customer(s) created.");
            
            // Also verify the customer can be found
            if (created > 0) {
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
            return ResponseEntity.status(500).body(response);
        }
    }
}

