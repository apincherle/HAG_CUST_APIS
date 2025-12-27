package com.example.repository;

import com.example.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID>, CustomerRepositoryCustom {
    
    Optional<Customer> findByEmailAndStatus(String email, Customer.CustomerStatus status);
    
    Optional<Customer> findByEmail(String email);
    
    Optional<Customer> findByCustomerIdAndStatus(UUID customerId, Customer.CustomerStatus status);
    
    @Query("SELECT c FROM Customer c WHERE " +
           "(:q IS NULL OR LOWER(c.fullName) LIKE LOWER(:q) OR " +
           "LOWER(c.email) LIKE LOWER(:q) OR " +
           "c.phone LIKE :q) AND " +
           "(:email IS NULL OR c.email = :email) AND " +
           "(:phone IS NULL OR c.phone = :phone) AND " +
           "(:status IS NULL OR c.status = :status) AND " +
           "c.status != 'DELETED'")
    Page<Customer> searchCustomers(
            @Param("q") String q,
            @Param("email") String email,
            @Param("phone") String phone,
            @Param("status") Customer.CustomerStatus status,
            Pageable pageable
    );
    
    boolean existsByEmailAndCustomerIdNot(String email, UUID excludeId);
    
    boolean existsByEmail(String email);
}

