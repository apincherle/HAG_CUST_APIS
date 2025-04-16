package com.example.repository;

import com.example.model.Programme;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProgrammeRepository extends MongoRepository<Programme, String> {
} 