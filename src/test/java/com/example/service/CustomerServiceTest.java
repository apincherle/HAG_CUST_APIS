package com.example.service;

import com.example.dto.CustomerResponse;
import com.example.model.Customer;
import com.example.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("dev")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:postgresql://localhost:5432/hags_customer",
    "spring.datasource.username=hags_user",
    "spring.datasource.password=hags_password",
    "spring.jpa.hibernate.ddl-auto=update"
})
public class CustomerServiceTest {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    public void testGetCustomerById_ReturnsCorrectCustomer() {
        // Given: The known customer ID in the database
        UUID expectedCustomerId = UUID.fromString("95240174-43c0-4f75-a716-a2f701e7c9fd");
        String expectedEmail = "a@b.com";

        // When: Fetching the customer by ID using the service
        CustomerResponse customer = customerService.getCustomerById(expectedCustomerId);

        // Then: Customer should be found and all fields should match
        assertNotNull(customer, "Customer should not be null");
        assertEquals(expectedCustomerId, customer.getCustomerId(), 
            "Customer ID should match exactly: " + expectedCustomerId);
        assertEquals(expectedEmail, customer.getEmail(), 
            "Email should match: " + expectedEmail);
        
        // Verify UUID string representation
        assertEquals("95240174-43c0-4f75-a716-a2f701e7c9fd", 
            customer.getCustomerId().toString(), 
            "UUID string representation should match exactly");
        
        // Verify customer is active
        assertEquals(Customer.CustomerStatus.ACTIVE, customer.getStatus(), 
            "Customer status should be ACTIVE");
        
        // Verify other fields are populated
        assertNotNull(customer.getFullName(), "Full name should not be null");
        assertNotNull(customer.getBillingAddress(), "Billing address should not be null");
        assertNotNull(customer.getShippingAddress(), "Shipping address should not be null");
    }

    @Test
    public void testGetCustomerById_WithStringUUID() {
        // Given: The known customer ID as a string
        String customerIdString = "95240174-43c0-4f75-a716-a2f701e7c9fd";
        UUID expectedCustomerId = UUID.fromString(customerIdString);

        // When: Converting string to UUID and fetching customer
        UUID customerId = UUID.fromString(customerIdString);
        CustomerResponse customer = customerService.getCustomerById(customerId);

        // Then: Customer should be found with correct UUID
        assertNotNull(customer, "Customer should not be null");
        assertEquals(expectedCustomerId, customer.getCustomerId(), 
            "Customer ID should match: " + expectedCustomerId);
        assertEquals(customerIdString, customer.getCustomerId().toString(), 
            "UUID string should match: " + customerIdString);
    }

    @Test
    public void testGetCustomerById_NotFound() {
        // Given: A non-existent customer ID
        UUID nonExistentId = UUID.fromString("00000000-0000-0000-0000-000000000000");

        // When/Then: Should throw NOT_FOUND exception
        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> {
            customerService.getCustomerById(nonExistentId);
        }, "Should throw ResponseStatusException for non-existent customer");
    }

    @Test
    public void testListCustomers_ReturnsCorrectUUID() {
        // Given: The known customer ID in the database
        UUID expectedCustomerId = UUID.fromString("95240174-43c0-4f75-a716-a2f701e7c9fd");
        String expectedEmail = "a@b.com";

        // When: Listing customers with default parameters
        com.example.dto.CustomerListResponse response = customerService.listCustomers(
            null, null, null, null, 50, null, "created_at", "desc");

        // Then: Customer should be found in the list with correct UUID
        assertNotNull(response, "Response should not be null");
        assertNotNull(response.getItems(), "Items should not be null");
        assertFalse(response.getItems().isEmpty(), "Items should not be empty");
        
        // Find the customer with the expected email
        CustomerResponse customer = response.getItems().stream()
            .filter(c -> expectedEmail.equals(c.getEmail()))
            .findFirst()
            .orElse(null);
        
        assertNotNull(customer, "Customer with email " + expectedEmail + " should be found");
        assertEquals(expectedCustomerId, customer.getCustomerId(), 
            "Customer ID should match exactly: " + expectedCustomerId);
        assertEquals("95240174-43c0-4f75-a716-a2f701e7c9fd", 
            customer.getCustomerId().toString(), 
            "UUID string representation should match exactly");
    }

    @Test
    public void testListCustomers_WithEmailFilter_ReturnsCorrectUUID() {
        // Given: The known customer ID and email
        UUID expectedCustomerId = UUID.fromString("95240174-43c0-4f75-a716-a2f701e7c9fd");
        String expectedEmail = "a@b.com";

        // When: Listing customers filtered by email
        com.example.dto.CustomerListResponse response = customerService.listCustomers(
            null, expectedEmail, null, null, 50, null, "created_at", "desc");

        // Then: Customer should be found with correct UUID
        assertNotNull(response, "Response should not be null");
        assertNotNull(response.getItems(), "Items should not be null");
        assertFalse(response.getItems().isEmpty(), "Items should not be empty");
        
        CustomerResponse customer = response.getItems().get(0);
        assertEquals(expectedCustomerId, customer.getCustomerId(), 
            "Customer ID should match exactly: " + expectedCustomerId);
        assertEquals(expectedEmail, customer.getEmail(), 
            "Email should match: " + expectedEmail);
        assertEquals("95240174-43c0-4f75-a716-a2f701e7c9fd", 
            customer.getCustomerId().toString(), 
            "UUID string representation should match exactly");
    }
}

