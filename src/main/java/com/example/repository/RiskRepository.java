package com.example.repository;

import com.example.model.Risk;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RiskRepository extends MongoRepository<Risk, String> {
} 