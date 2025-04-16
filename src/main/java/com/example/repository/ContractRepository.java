package com.example.repository;

import com.example.model.Contract;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ContractRepository extends MongoRepository<Contract, String> {
} 