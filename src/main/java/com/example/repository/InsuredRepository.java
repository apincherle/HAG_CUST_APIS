package com.example.repository;

import com.example.model.Insured;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface InsuredRepository extends MongoRepository<Insured, String> {
} 