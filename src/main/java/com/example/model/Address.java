package com.example.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    @Column(name = "line1", length = 200)
    private String line1;
    
    @Column(name = "line2", length = 200, nullable = true)
    private String line2;
    
    @Column(name = "city", length = 120)
    private String city;
    
    @Column(name = "region", length = 120, nullable = true)
    private String region;
    
    @Column(name = "postcode", length = 20)
    private String postcode;
    
    @Column(name = "country", length = 2)
    private String country; // ISO-3166 alpha-2
}

