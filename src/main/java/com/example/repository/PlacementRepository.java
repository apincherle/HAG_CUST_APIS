package com.example.repository;

import com.example.model.Placement;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlacementRepository extends MongoRepository<Placement, String> {
} 