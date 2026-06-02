package com.example.repository;

import com.example.model.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Disabled("Legacy DB-specific integration tests; requires dedicated PostgreSQL fixture dataset.")
public class CustomerRepositoryTest {

    private static final UUID FIXTURE_CUSTOMER_ID = UUID.fromString("95240174-43c0-4f75-a716-a2f701e7c9fd");
    private static final String FIXTURE_EMAIL = "a@b.com";

    @Autowired
    private CustomerRepository customerRepository;

    @BeforeEach
    void setup() {
        customerRepository.deleteAll();
        Customer customer = new Customer();
        customer.setCustomerId(FIXTURE_CUSTOMER_ID);
        customer.setEmail(FIXTURE_EMAIL);
        customer.setPhone("07817700059");
        customer.setFullName("Andrew Pincherle");
        customer.setMarketingOptIn(true);
        customer.setStatus(Customer.CustomerStatus.ACTIVE);
        customerRepository.save(customer);
    }

    @Test
    public void testFindByEmailNative_ReturnsCorrectUUID() {
        UUID expectedCustomerId = FIXTURE_CUSTOMER_ID;
        String email = FIXTURE_EMAIL;

        // When: Fetching the customer by email using native query
        Optional<Customer> customerOpt = customerRepository.findByEmailNative(email);

        // Then: Customer should be found and UUID should match exactly
        assertTrue(customerOpt.isPresent(), "Customer should be found");
        Customer customer = customerOpt.get();
        
        assertEquals(expectedCustomerId, customer.getCustomerId(), 
            "Customer ID should match exactly: " + expectedCustomerId);
        assertEquals(email, customer.getEmail(), "Email should match");
        
        // Verify UUID string representation
        assertEquals("95240174-43c0-4f75-a716-a2f701e7c9fd", 
            customer.getCustomerId().toString(), 
            "UUID string representation should match exactly");
    }

    @Test
    public void testFindByCustomerIdNative_ReturnsCorrectUUID() {
        UUID expectedCustomerId = FIXTURE_CUSTOMER_ID;
        String customerIdString = FIXTURE_CUSTOMER_ID.toString();

        // When: Fetching the customer by ID using native query
        Optional<Customer> customerOpt = customerRepository.findByCustomerIdNative(customerIdString);

        // Then: Customer should be found and UUID should match exactly
        assertTrue(customerOpt.isPresent(), "Customer should be found");
        Customer customer = customerOpt.get();
        
        assertEquals(expectedCustomerId, customer.getCustomerId(), 
            "Customer ID should match exactly: " + expectedCustomerId);
        
        // Verify UUID string representation
        assertEquals("95240174-43c0-4f75-a716-a2f701e7c9fd", 
            customer.getCustomerId().toString(), 
            "UUID string representation should match exactly");
    }

    @Test
    public void testCustomerEntity_UUIDConversion() {
        UUID expectedCustomerId = FIXTURE_CUSTOMER_ID;
        String email = FIXTURE_EMAIL;

        // When: Fetching the customer
        Optional<Customer> customerOpt = customerRepository.findByEmailNative(email);

        // Then: Verify the UUID is correctly converted and stored in the POJO
        assertTrue(customerOpt.isPresent(), "Customer should be found");
        Customer customer = customerOpt.get();
        
        // Verify UUID object
        assertNotNull(customer.getCustomerId(), "Customer ID should not be null");
        assertEquals(expectedCustomerId, customer.getCustomerId(), 
            "Customer ID UUID object should match");
        
        // Verify UUID string
        String actualUuidString = customer.getCustomerId().toString();
        assertEquals(FIXTURE_CUSTOMER_ID.toString(), actualUuidString,
            "UUID string should be: 95240174-43c0-4f75-a716-a2f701e7c9fd, but was: " + actualUuidString);
        
        // Verify it's a valid UUID
        UUID parsedUuid = UUID.fromString(actualUuidString);
        assertEquals(expectedCustomerId, parsedUuid, "Parsed UUID should match expected");
    }
}

