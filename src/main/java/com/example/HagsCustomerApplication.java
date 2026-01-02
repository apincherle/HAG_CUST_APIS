package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.example", "com.example.qrcert"})
public class HagsCustomerApplication {
    public static void main(String[] args) {
        SpringApplication.run(HagsCustomerApplication.class, args);
    }
}

