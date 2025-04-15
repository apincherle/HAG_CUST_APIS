package com.example.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Map;

@Data
@Document(collection = "placements")
public class Placement {
    @Id
    private String _id;
    private Map<String, Object> _metadata;
    // Add other fields as per schema
    // Using Map<String, Object> for flexibility with the JSON structure
    private Map<String, Object> data;
} 