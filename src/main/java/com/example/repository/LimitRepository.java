package com.example.repository;

import com.example.model.Limit;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LimitRepository extends MongoRepository<Limit, String> {
} 