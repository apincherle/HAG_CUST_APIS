package com.example.service;

import com.example.model.Placement;
import com.example.repository.PlacementRepository;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;

@Service
public class PlacementService {

    @Autowired
    private PlacementRepository repository;

    private Schema schema;

    @PostConstruct
    public void init() {
        // Load the schema on service initialization
        try (InputStream inputStream = getClass().getResourceAsStream("/placements.json")) {
            JSONObject rawSchema = new JSONObject(new JSONTokener(inputStream));
            schema = SchemaLoader.load(rawSchema);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load JSON schema", e);
        }
    }

    public Placement createPlacement(Placement placement) {
        // Convert placement to JSONObject for validation
        JSONObject jsonData = new JSONObject(placement);
        // Validate against schema
        schema.validate(jsonData);
        // If validation passes, save to MongoDB
        return repository.save(placement);
    }
} 