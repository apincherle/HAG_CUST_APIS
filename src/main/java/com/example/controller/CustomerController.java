package com.example.controller;

import com.example.dto.*;
import com.example.model.Customer;
import com.example.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/customers")
@Tag(name = "Customers", description = "Customer management API")
public class CustomerController {
    
    @Autowired
    private CustomerService customerService;
    
    @PostMapping
    @Operation(summary = "Create customer", operationId = "createCustomer")
    public ResponseEntity<CustomerResponse> createCustomer(
            @Valid @RequestBody CustomerCreateRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) 
            @Parameter(description = "Optional key to safely retry create without duplicates") String idempotencyKey) {
        CustomerResponse customer = customerService.createCustomer(request, idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Location", "/v1/customers/" + customer.getCustomerId())
                .body(customer);
    }
    
    @GetMapping("/lookup")
    @Operation(summary = "Lookup customer by email", operationId = "lookupCustomerByEmail")
    public ResponseEntity<CustomerResponse> lookupCustomerByEmail(
            @Parameter(description = "Email address", required = true) @RequestParam(required = true) String email) {
        CustomerResponse customer = customerService.getCustomerByEmail(email);
        return ResponseEntity.ok(customer);
    }
    
    @GetMapping(value = "/{customer_id}", produces = "application/json")
    @Operation(summary = "Get customer by id", operationId = "getCustomer")
    public ResponseEntity<CustomerResponse> getCustomer(
            @Parameter(description = "Customer GUID", required = true, example = "95240174-43c0-4f75-a716-a2f701e7c9fd")
            @PathVariable("customer_id") String customerIdStr) {
        System.out.println("DEBUG: getCustomer called with customer_id: " + customerIdStr);
        try {
            UUID customerId = UUID.fromString(customerIdStr);
            CustomerResponse customer = customerService.getCustomerById(customerId);
            return ResponseEntity.ok(customer);
        } catch (IllegalArgumentException e) {
            System.err.println("ERROR: Invalid UUID format: " + customerIdStr + " - " + e.getMessage());
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST, 
                "Invalid customer ID format: " + customerIdStr);
        }
    }
    
    @GetMapping
    @Operation(summary = "List/search customers", operationId = "listCustomers")
    public ResponseEntity<CustomerListResponse> listCustomers(
            @Parameter(description = "Full-text search across name/email/phone") @RequestParam(required = false) String q,
            @Parameter(description = "Filter by exact email") @RequestParam(required = false) String email,
            @Parameter(description = "Filter by phone") @RequestParam(required = false) String phone,
            @Parameter(description = "Filter by customer status") @RequestParam(required = false) Customer.CustomerStatus status,
            @Parameter(description = "Page size (1-200)") @RequestParam(required = false, defaultValue = "50") Integer limit,
            @Parameter(description = "Pagination cursor") @RequestParam(required = false) String cursor,
            @Parameter(description = "Sort field") @RequestParam(required = false, defaultValue = "created_at") String sort,
            @Parameter(description = "Sort order") @RequestParam(required = false, defaultValue = "desc") String order) {
        CustomerListResponse response = customerService.listCustomers(q, email, phone, status, limit, cursor, sort, order);
        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/{customer_id}")
    @Operation(summary = "Update customer (partial)", operationId = "updateCustomer")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @Parameter(description = "Customer GUID", required = true) @PathVariable("customer_id") String customerIdStr,
            @Valid @RequestBody CustomerUpdateRequest request) {
        try {
            UUID customerId = UUID.fromString(customerIdStr);
            CustomerResponse customer = customerService.updateCustomer(customerId, request);
            return ResponseEntity.ok(customer);
        } catch (IllegalArgumentException e) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST, 
                "Invalid customer ID format: " + customerIdStr);
        }
    }
    
    @DeleteMapping("/{customer_id}")
    @Operation(summary = "Delete customer (soft delete)", operationId = "deleteCustomer")
    public ResponseEntity<Void> deleteCustomer(
            @Parameter(description = "Customer GUID", required = true) @PathVariable("customer_id") String customerIdStr) {
        try {
            UUID customerId = UUID.fromString(customerIdStr);
            customerService.deleteCustomer(customerId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST, 
                "Invalid customer ID format: " + customerIdStr);
        }
    }
    
    @PostMapping("/{customer_id}/restore")
    @Operation(summary = "Restore soft-deleted customer", operationId = "restoreCustomer")
    public ResponseEntity<CustomerResponse> restoreCustomer(
            @Parameter(description = "Customer GUID", required = true) @PathVariable("customer_id") String customerIdStr) {
        try {
            UUID customerId = UUID.fromString(customerIdStr);
            CustomerResponse customer = customerService.restoreCustomer(customerId);
            return ResponseEntity.ok(customer);
        } catch (IllegalArgumentException e) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST, 
                "Invalid customer ID format: " + customerIdStr);
        }
    }
}

