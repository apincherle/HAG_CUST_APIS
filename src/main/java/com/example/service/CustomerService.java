package com.example.service;

import com.example.dto.*;
import com.example.model.Customer;
import com.example.repository.CustomerRepository;
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
public class CustomerService {
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Transactional
    public CustomerResponse createCustomer(CustomerCreateRequest request, String idempotencyKey) {
        // Normalize email to lowercase
        String normalizedEmail = (request.getEmail() != null) ? request.getEmail().trim().toLowerCase() : null;
        if (normalizedEmail == null || normalizedEmail.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }
        
        // Check for duplicate email
        if (customerRepository.existsByEmail(normalizedEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, 
                "Customer with email already exists");
        }
        
        Customer customer = new Customer();
        customer.setCustomerId(UUID.randomUUID());
        customer.setEmail(normalizedEmail);
        customer.setPhone(request.getPhone());
        customer.setFullName(request.getFullName());
        customer.setBillingAddress(request.getBillingAddress());
        customer.setShippingAddress(request.getShippingAddress());
        customer.setMarketingOptIn(request.getMarketingOptIn() != null ? request.getMarketingOptIn() : false);
        customer.setStatus(Customer.CustomerStatus.ACTIVE);
        
        customer = customerRepository.save(customer);
        return CustomerResponse.fromEntity(customer);
    }
    
    public CustomerListResponse listCustomers(
            String q, String email, String phone, Customer.CustomerStatus status,
            Integer limit, String cursor, String sort, String order) {
        
        // Default values
        int pageSize = (limit != null && limit > 0 && limit <= 200) ? limit : 50;
        String sortField = (sort != null) ? sort : "createdAt";
        Sort.Direction direction = ("asc".equalsIgnoreCase(order)) ? Sort.Direction.ASC : Sort.Direction.DESC;
        
        // Handle cursor-based pagination
        int pageNumber = 0;
        if (cursor != null && !cursor.isEmpty()) {
            try {
                String decoded = new String(Base64.getDecoder().decode(cursor));
                pageNumber = Integer.parseInt(decoded);
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid cursor");
            }
        }
        
        // Map sort field names for JPA (camelCase) and database (snake_case)
        String jpaSortField = sortField;
        String dbSortField = sortField;
        if ("created_at".equals(sortField) || "createdAt".equals(sortField)) {
            jpaSortField = "createdAt";
            dbSortField = "created_at";
        } else if ("full_name".equals(sortField) || "fullName".equals(sortField)) {
            jpaSortField = "fullName";
            dbSortField = "full_name";
        } else if ("updated_at".equals(sortField) || "updatedAt".equals(sortField)) {
            jpaSortField = "updatedAt";
            dbSortField = "updated_at";
        }
        
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(direction, jpaSortField));
        
        // Normalize email filter to lowercase
        String normalizedEmail = (email != null) ? email.trim().toLowerCase() : null;
        
        Page<Customer> page;
        // Use native queries for pagination and search
        if (q == null && normalizedEmail == null && phone == null && status == null) {
            page = customerRepository.findAllNative(pageable);
        } else {
            String searchTerm = (q != null) ? q.trim() : null;
            page = customerRepository.searchCustomersNative(searchTerm, normalizedEmail, phone, status, pageable);
        }
        
        List<CustomerResponse> items = page.getContent().stream()
                .map(CustomerResponse::fromEntity)
                .collect(Collectors.toList());
        
        String nextCursor = null;
        if (page.hasNext()) {
            String nextPage = String.valueOf(pageNumber + 1);
            nextCursor = Base64.getEncoder().encodeToString(nextPage.getBytes());
        }
        
        return CustomerListResponse.builder()
                .items(items)
                .nextCursor(nextCursor)
                .build();
    }
    
    public CustomerResponse getCustomerByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }
        
        // Spring automatically decodes URL encoding (%40 -> @)
        // Normalize to lowercase - emails are stored in lowercase
        String normalizedEmail = email.trim().toLowerCase();
        
        // Use native query to avoid UUID conversion issues
        Customer customer = customerRepository.findByEmailNative(normalizedEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Customer not found with email: " + email));
        
        // Return 404 for deleted customers
        if (customer.getStatus() == Customer.CustomerStatus.DELETED) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found");
        }
        
        return CustomerResponse.fromEntity(customer);
    }
    
    public CustomerResponse getCustomerById(UUID customerId) {
        // Use native query with manual mapping to avoid UUID conversion issues
        String customerIdString = customerId.toString();
        Customer customer = customerRepository.findByCustomerIdNative(customerIdString)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
        
        // Return 404 for deleted customers (soft delete behavior)
        if (customer.getStatus() == Customer.CustomerStatus.DELETED) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found");
        }
        
        return CustomerResponse.fromEntity(customer);
    }
    
    @Transactional
    public CustomerResponse updateCustomer(UUID customerId, CustomerUpdateRequest request) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
        
        if (customer.getStatus() == Customer.CustomerStatus.DELETED) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found");
        }
        
        // Check email uniqueness if being updated
        if (request.getEmail() != null) {
            String normalizedEmail = request.getEmail().trim().toLowerCase();
            if (!normalizedEmail.equals(customer.getEmail())) {
                if (customerRepository.existsByEmailAndCustomerIdNot(normalizedEmail, customerId)) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, 
                        "Email already exists");
                }
                customer.setEmail(normalizedEmail);
            }
        }
        
        if (request.getPhone() != null) {
            customer.setPhone(request.getPhone());
        }
        if (request.getFullName() != null) {
            customer.setFullName(request.getFullName());
        }
        if (request.getBillingAddress() != null) {
            customer.setBillingAddress(request.getBillingAddress());
        }
        if (request.getShippingAddress() != null) {
            customer.setShippingAddress(request.getShippingAddress());
        }
        if (request.getMarketingOptIn() != null) {
            customer.setMarketingOptIn(request.getMarketingOptIn());
        }
        
        customer = customerRepository.save(customer);
        return CustomerResponse.fromEntity(customer);
    }
    
    @Transactional
    public void deleteCustomer(UUID customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
        
        if (customer.getStatus() == Customer.CustomerStatus.DELETED) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found");
        }
        
        customer.setStatus(Customer.CustomerStatus.DELETED);
        customer.setDeletedAt(LocalDateTime.now());
        customerRepository.save(customer);
    }
    
    @Transactional
    public CustomerResponse restoreCustomer(UUID customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
        
        if (customer.getStatus() != Customer.CustomerStatus.DELETED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, 
                "Customer is not deleted");
        }
        
        customer.setStatus(Customer.CustomerStatus.ACTIVE);
        customer.setDeletedAt(null);
        customer = customerRepository.save(customer);
        return CustomerResponse.fromEntity(customer);
    }
}

