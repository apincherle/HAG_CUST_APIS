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
        // Check for duplicate email
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, 
                "Customer with email already exists");
        }
        
        Customer customer = new Customer();
        customer.setCustomerId(UUID.randomUUID());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setFullName(request.getFullName());
        customer.setBillingAddress(request.getBillingAddress());
        customer.setShippingAddress(request.getShippingAddress());
        customer.setMarketingOptIn(request.getMarketingOptIn() != null ? request.getMarketingOptIn() : false);
        customer.setStatus(Customer.CustomerStatus.ACTIVE);
        
        customer = customerRepository.save(customer);
        return CustomerResponse.fromEntity(customer);
    }
    
    @Transactional(readOnly = true)
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
        
        // Map sort field names
        String jpaSortField = sortField;
        if ("created_at".equals(sortField)) {
            jpaSortField = "createdAt";
        } else if ("full_name".equals(sortField)) {
            jpaSortField = "fullName";
        }
        
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(direction, jpaSortField));
        
        Page<Customer> page = customerRepository.searchCustomers(q, email, phone, status, pageable);
        
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
    
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerByEmail(String email) {
        Customer customer = customerRepository.findByEmailAndStatus(email, Customer.CustomerStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
        return CustomerResponse.fromEntity(customer);
    }
    
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(UUID customerId) {
        Customer customer = customerRepository.findByCustomerIdAndStatus(customerId, Customer.CustomerStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
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
        if (request.getEmail() != null && !request.getEmail().equals(customer.getEmail())) {
            if (customerRepository.existsByEmailAndCustomerIdNot(request.getEmail(), customerId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, 
                    "Email already exists");
            }
            customer.setEmail(request.getEmail());
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

