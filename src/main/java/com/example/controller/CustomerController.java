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
    
    @GetMapping("/lookup")
    @Operation(summary = "Lookup customer by email", operationId = "lookupCustomerByEmail")
    public ResponseEntity<CustomerResponse> lookupCustomerByEmail(
            @Parameter(description = "Email address", required = true) @RequestParam String email) {
        CustomerResponse customer = customerService.getCustomerByEmail(email);
        return ResponseEntity.ok(customer);
    }
    
    @GetMapping("/{customer_id}")
    @Operation(summary = "Get customer by id", operationId = "getCustomer")
    public ResponseEntity<CustomerResponse> getCustomer(
            @Parameter(description = "Customer GUID", required = true) @PathVariable("customer_id") UUID customerId) {
        CustomerResponse customer = customerService.getCustomerById(customerId);
        return ResponseEntity.ok(customer);
    }
    
    @PatchMapping("/{customer_id}")
    @Operation(summary = "Update customer (partial)", operationId = "updateCustomer")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @Parameter(description = "Customer GUID", required = true) @PathVariable("customer_id") UUID customerId,
            @Valid @RequestBody CustomerUpdateRequest request) {
        CustomerResponse customer = customerService.updateCustomer(customerId, request);
        return ResponseEntity.ok(customer);
    }
    
    @DeleteMapping("/{customer_id}")
    @Operation(summary = "Delete customer (soft delete)", operationId = "deleteCustomer")
    public ResponseEntity<Void> deleteCustomer(
            @Parameter(description = "Customer GUID", required = true) @PathVariable("customer_id") UUID customerId) {
        customerService.deleteCustomer(customerId);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{customer_id}/restore")
    @Operation(summary = "Restore soft-deleted customer", operationId = "restoreCustomer")
    public ResponseEntity<CustomerResponse> restoreCustomer(
            @Parameter(description = "Customer GUID", required = true) @PathVariable("customer_id") UUID customerId) {
        CustomerResponse customer = customerService.restoreCustomer(customerId);
        return ResponseEntity.ok(customer);
    }
}

