package com.example.repository;

import com.example.model.BrokerTeam;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BrokerTeamRepository extends MongoRepository<BrokerTeam, String> {
} 