package com.example.dto;

import com.example.model.Address;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CustomerUpdateRequest {
    @Email(message = "Email must be valid")
    private String email;
    
    @Size(max = 50, message = "Phone must not exceed 50 characters")
    private String phone;
    
    @Size(max = 200, message = "Full name must not exceed 200 characters")
    private String fullName;
    
    private Address billingAddress;
    
    private Address shippingAddress;
    
    private Boolean marketingOptIn;
}

