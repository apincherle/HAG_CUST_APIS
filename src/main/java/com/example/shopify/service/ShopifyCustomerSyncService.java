package com.example.shopify.service;

import com.example.model.Address;
import com.example.model.Customer;
import com.example.repository.CustomerRepository;
import com.example.shopify.dto.ShopifyAddressPayload;
import com.example.shopify.dto.ShopifyCustomerPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class ShopifyCustomerSyncService {

    private static final Logger log = LoggerFactory.getLogger(ShopifyCustomerSyncService.class);

    private final CustomerRepository customerRepository;
    private final ObjectMapper objectMapper;

    public ShopifyCustomerSyncService(CustomerRepository customerRepository, ObjectMapper objectMapper) {
        this.customerRepository = customerRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void handleCustomerWebhook(String topic, byte[] payload) throws Exception {
        ShopifyCustomerPayload shopifyCustomer = objectMapper.readValue(payload, ShopifyCustomerPayload.class);
        if (shopifyCustomer.getId() == null) {
            throw new IllegalArgumentException("Shopify customer payload missing id");
        }
        upsertFromShopify(shopifyCustomer);
        log.info("Processed Shopify {} for shopify_customer_id={}", topic, shopifyCustomer.getId());
    }

    @Transactional
    public Customer upsertFromShopify(ShopifyCustomerPayload shopifyCustomer) {
        Long shopifyId = shopifyCustomer.getId();
        String email = normalizeEmail(shopifyCustomer.getEmail());

        Customer customer = customerRepository.findByShopifyCustomerId(shopifyId)
                .orElseGet(() -> findByEmailOrNew(email, shopifyId));

        customer.setShopifyCustomerId(shopifyId);
        customer.setShopifyUpdatedAt(LocalDateTime.now());
        if (email != null && !email.isBlank()) {
            customer.setEmail(email);
        } else if (customer.getEmail() == null || customer.getEmail().isBlank()) {
            customer.setEmail("shopify-" + shopifyId + "@placeholder.local");
        }
        customer.setPhone(firstNonBlank(shopifyCustomer.getPhone(),
                shopifyCustomer.getDefaultAddress() != null ? shopifyCustomer.getDefaultAddress().getPhone() : null,
                customer.getPhone()));
        customer.setFullName(buildFullName(shopifyCustomer, customer.getFullName()));
        if (shopifyCustomer.getDefaultAddress() != null) {
            Address address = toAddress(shopifyCustomer.getDefaultAddress());
            customer.setShippingAddress(address);
            if (customer.getBillingAddress() == null) {
                customer.setBillingAddress(address);
            }
        }
        if (customer.getStatus() == null) {
            customer.setStatus(Customer.CustomerStatus.ACTIVE);
        }
        if (customer.getMarketingOptIn() == null) {
            customer.setMarketingOptIn(false);
        }
        return customerRepository.save(customer);
    }

    private Customer findByEmailOrNew(String email, Long shopifyId) {
        if (email != null && !email.isBlank()) {
            Optional<Customer> byEmail = customerRepository.findByEmail(email);
            if (byEmail.isPresent()) {
                Customer existing = byEmail.get();
                if (existing.getShopifyCustomerId() != null
                        && !existing.getShopifyCustomerId().equals(shopifyId)) {
                    throw new IllegalStateException(
                            "Email already linked to a different Shopify customer: " + email);
                }
                return existing;
            }
        }
        Customer created = new Customer();
        created.setCustomerId(UUID.randomUUID());
        created.setStatus(Customer.CustomerStatus.ACTIVE);
        created.setMarketingOptIn(false);
        return created;
    }

    private static String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        String trimmed = email.trim().toLowerCase();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String buildFullName(ShopifyCustomerPayload payload, String fallback) {
        String first = payload.getFirstName() != null ? payload.getFirstName().trim() : "";
        String last = payload.getLastName() != null ? payload.getLastName().trim() : "";
        String combined = (first + " " + last).trim();
        if (!combined.isEmpty()) {
            return combined;
        }
        if (fallback != null && !fallback.isBlank()) {
            return fallback;
        }
        String email = payload.getEmail();
        if (email != null && email.contains("@")) {
            return email.substring(0, email.indexOf('@'));
        }
        return "Shopify Customer";
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private static Address toAddress(ShopifyAddressPayload src) {
        Address address = new Address();
        address.setLine1(src.getAddress1());
        address.setLine2(src.getAddress2());
        address.setCity(src.getCity());
        address.setRegion(src.getProvince());
        address.setPostcode(src.getZip());
        String country = src.getCountryCode();
        if (country == null || country.isBlank()) {
            country = src.getCountry();
        }
        if (country != null && country.length() > 2) {
            country = country.substring(0, 2).toUpperCase();
        }
        address.setCountry(country);
        return address;
    }
}
