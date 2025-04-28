package com.example.dto;

import lombok.Data;
import java.util.List;

@Data
public class PlacementQueryRequest {
    private List<String> client_name;
    private List<String> placement_name;
    private List<String> effective_year;
    private List<String> owner_name;
    private List<String> status;
    private String inception_from;
    private String inception_to;
    private Boolean user_only = true;

    // Ordering
    private String order_by; // e.g. "clientName", "placementName", etc.
    private String order_dir; // "asc" or "desc"
} 