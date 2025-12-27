package com.example.repository;

import com.example.model.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("dev")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:sqlite:./data/dev.db",
    "spring.jpa.hibernate.ddl-auto=validate"
})
public class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    public void testFindByEmailNative_ReturnsCorrectUUID() {
        // Given: The known customer ID in the database
        UUID expectedCustomerId = UUID.fromString("95240174-43c0-4f75-a716-a2f701e7c9fd");
        String email = "a@b.com";

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
        // Given: The known customer ID
        UUID expectedCustomerId = UUID.fromString("95240174-43c0-4f75-a716-a2f701e7c9fd");
        String customerIdString = "95240174-43c0-4f75-a716-a2f701e7c9fd";

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
        // Given: The known customer ID
        UUID expectedCustomerId = UUID.fromString("95240174-43c0-4f75-a716-a2f701e7c9fd");
        String email = "a@b.com";

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
        assertEquals("95240174-43c0-4f75-a716-a2f701e7c9fd", actualUuidString,
            "UUID string should be: 95240174-43c0-4f75-a716-a2f701e7c9fd, but was: " + actualUuidString);
        
        // Verify it's a valid UUID
        UUID parsedUuid = UUID.fromString(actualUuidString);
        assertEquals(expectedCustomerId, parsedUuid, "Parsed UUID should match expected");
    }
}

