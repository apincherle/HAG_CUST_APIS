package com.example.config;

import com.example.model.Address;
import com.example.model.Customer;
import com.example.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

/**
 * Initializes test data from CSV file before tests run.
 * This ensures required test customers exist in the database.
 * Only runs in test contexts (when active profile is 'dev' or 'test').
 */
@Component
@org.springframework.context.annotation.Profile({"dev", "test"})
public class TestDataInitializer {

    @Autowired
    private CustomerRepository customerRepository;

    @PostConstruct
    @Transactional
    public void initializeTestData() {
        try {
            // Load CSV file from test resources
            InputStream inputStream = getClass().getClassLoader()
                    .getResourceAsStream("com/example/repository/customers.csv");
            
            if (inputStream == null) {
                // Try alternative path
                inputStream = getClass().getResourceAsStream("/com/example/repository/customers.csv");
            }
            
            if (inputStream == null) {
                System.out.println("WARNING: customers.csv not found, skipping test data initialization");
                return;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue; // Skip empty lines and comments
                    }
                    
                    String[] fields = line.split(",");
                    if (fields.length < 20) {
                        System.out.println("WARNING: Skipping invalid CSV line: " + line);
                        continue;
                    }
                    
                    // Parse CSV fields
                    String customerIdStr = fields[0].trim();
                    String billingCity = fields[1].trim();
                    String billingCountry = fields[2].trim();
                    String billingLine1 = fields[3].trim();
                    String billingLine2 = fields[4].trim();
                    String billingPostcode = fields[5].trim();
                    String billingRegion = fields[6].trim();
                    long createdAtMillis = Long.parseLong(fields[7].trim());
                    String deletedAtStr = fields[8].trim();
                    String email = fields[9].trim();
                    String fullName = fields[10].trim();
                    int marketingOptInInt = Integer.parseInt(fields[11].trim());
                    String phone = fields[12].trim();
                    String shippingCity = fields[13].trim();
                    String shippingCountry = fields[14].trim();
                    String shippingLine1 = fields[15].trim();
                    String shippingLine2 = fields[16].trim();
                    String shippingPostcode = fields[17].trim();
                    String shippingRegion = fields[18].trim();
                    String statusStr = fields[19].trim();
                    long updatedAtMillis = Long.parseLong(fields[20].trim());
                    
                    UUID customerId = UUID.fromString(customerIdStr);
                    
                    // Check if customer already exists
                    if (customerRepository.findByCustomerIdNative(customerIdStr).isPresent()) {
                        System.out.println("Test customer already exists: " + customerIdStr);
                        continue;
                    }
                    
                    // Create customer
                    Customer customer = new Customer();
                    customer.setCustomerId(customerId);
                    customer.setEmail(email.toLowerCase()); // Normalize email
                    customer.setPhone(phone.isEmpty() ? null : phone);
                    customer.setFullName(fullName);
                    customer.setMarketingOptIn(marketingOptInInt == 1);
                    customer.setStatus(Customer.CustomerStatus.valueOf(statusStr));
                    
                    // Set billing address
                    Address billingAddress = new Address();
                    billingAddress.setLine1(billingLine1.isEmpty() ? null : billingLine1);
                    billingAddress.setLine2(billingLine2.isEmpty() ? null : billingLine2);
                    billingAddress.setCity(billingCity.isEmpty() ? null : billingCity);
                    billingAddress.setRegion(billingRegion.isEmpty() ? null : billingRegion);
                    billingAddress.setPostcode(billingPostcode.isEmpty() ? null : billingPostcode);
                    billingAddress.setCountry(billingCountry.isEmpty() ? null : billingCountry);
                    customer.setBillingAddress(billingAddress);
                    
                    // Set shipping address
                    Address shippingAddress = new Address();
                    shippingAddress.setLine1(shippingLine1.isEmpty() ? null : shippingLine1);
                    shippingAddress.setLine2(shippingLine2.isEmpty() ? null : shippingLine2);
                    shippingAddress.setCity(shippingCity.isEmpty() ? null : shippingCity);
                    shippingAddress.setRegion(shippingRegion.isEmpty() ? null : shippingRegion);
                    shippingAddress.setPostcode(shippingPostcode.isEmpty() ? null : shippingPostcode);
                    shippingAddress.setCountry(shippingCountry.isEmpty() ? null : shippingCountry);
                    customer.setShippingAddress(shippingAddress);
                    
                    // Set timestamps
                    LocalDateTime createdAt = LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(createdAtMillis), ZoneId.systemDefault());
                    LocalDateTime updatedAt = LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(updatedAtMillis), ZoneId.systemDefault());
                    customer.setCreatedAt(createdAt);
                    customer.setUpdatedAt(updatedAt);
                    
                    if (!deletedAtStr.isEmpty()) {
                        long deletedAtMillis = Long.parseLong(deletedAtStr);
                        LocalDateTime deletedAt = LocalDateTime.ofInstant(
                                Instant.ofEpochMilli(deletedAtMillis), ZoneId.systemDefault());
                        customer.setDeletedAt(deletedAt);
                    }
                    
                    // Save customer
                    customerRepository.save(customer);
                    System.out.println("Created test customer: " + customerIdStr + " (" + email + ")");
                }
            }
        } catch (Exception e) {
            System.err.println("ERROR: Failed to initialize test data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

