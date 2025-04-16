package com.example.repository;

import com.example.model.Premium;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PremiumRepository extends MongoRepository<Premium, String> {
} 