package com.example.repository;

import com.example.model.Placement;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PlacementRepository extends MongoRepository<Placement, String> {
} 