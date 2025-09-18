package com.example.controller;

import com.example.repository.PlacementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private PlacementRepository placementRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @GetMapping("/mongodb")
    public String testMongoDB() {
        try {
            long count = placementRepository.count();
            return "MongoDB connection successful. Found " + count + " placements.";
        } catch (Exception e) {
            return "MongoDB connection failed: " + e.getMessage();
        }
    }

    @GetMapping("/mongodb-details")
    public Map<String, Object> testMongoDBDetails() {
        Map<String, Object> result = new HashMap<>();
        try {
            // Test repository count
            long repositoryCount = placementRepository.count();
            result.put("repositoryCount", repositoryCount);
            
            // Test direct MongoDB query
            long templateCount = mongoTemplate.getCollection("placements").countDocuments();
            result.put("templateCount", templateCount);
            
            // Get database name
            String dbName = mongoTemplate.getDb().getName();
            result.put("databaseName", dbName);
            
            // List all collections
            result.put("collections", mongoTemplate.getCollectionNames());
            
            result.put("status", "success");
        } catch (Exception e) {
            result.put("status", "error");
            result.put("error", e.getMessage());
        }
        return result;
    }

    @GetMapping("/placements")
    public Object testPlacements() {
        try {
            return placementRepository.findAll();
        } catch (Exception e) {
            return "Error retrieving placements: " + e.getMessage();
        }
    }

    @GetMapping("/raw-placements")
    public Object testRawPlacements() {
        try {
            // Get raw documents from MongoDB
            return mongoTemplate.findAll(org.bson.Document.class, "placements");
        } catch (Exception e) {
            return "Error retrieving raw placements: " + e.getMessage();
        }
    }
}
