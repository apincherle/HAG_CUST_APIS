package com.example.repository;

import com.example.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepositoryCustom {
    Optional<Customer> findByCustomerIdNative(String customerIdString);
    Optional<Customer> findByEmailNative(String email);
    Page<Customer> findAllNative(Pageable pageable);
    Page<Customer> searchCustomersNative(String q, String email, String phone, Customer.CustomerStatus status, Pageable pageable);
}

