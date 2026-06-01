package com.example.shopify.service;

import com.example.model.Customer;
import com.example.repository.CustomerRepository;
import com.example.shopify.dto.ShopifyCustomerPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShopifyCustomerSyncServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    private ShopifyCustomerSyncService service;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        service = new ShopifyCustomerSyncService(customerRepository, objectMapper);
    }

    @Test
    void upsertFromShopify_createsNewCustomer() {
        when(customerRepository.findByShopifyCustomerId(99L)).thenReturn(Optional.empty());
        when(customerRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

        ShopifyCustomerPayload payload = new ShopifyCustomerPayload();
        payload.setId(99L);
        payload.setEmail("new@example.com");
        payload.setFirstName("Ada");
        payload.setLastName("Lovelace");

        Customer saved = service.upsertFromShopify(payload);

        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(captor.capture());
        assertEquals(99L, captor.getValue().getShopifyCustomerId());
        assertEquals("new@example.com", captor.getValue().getEmail());
        assertEquals("Ada Lovelace", captor.getValue().getFullName());
        assertEquals("Ada Lovelace", saved.getFullName());
    }

    @Test
    void handleCustomerWebhook_parsesPayload() throws Exception {
        when(customerRepository.findByShopifyCustomerId(1L)).thenReturn(Optional.empty());
        when(customerRepository.findByEmail("a@b.com")).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

        byte[] json = """
                {"id":1,"email":"a@b.com","first_name":"Test","last_name":"User"}
                """.strip().getBytes();

        service.handleCustomerWebhook("customers/create", json);
        verify(customerRepository).save(any(Customer.class));
    }
}
