package com.example.controller;

import com.example.model.Placement;
import com.example.service.PlacementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/placements")
@Tag(name = "Placements", description = "Placements management APIs")
public class PlacementController {

    @Autowired
    private PlacementService placementService;

    @Operation(summary = "Create a new placement")
    @PostMapping
    public ResponseEntity<Placement> createPlacement(@RequestBody Placement placement) {
        return ResponseEntity.ok(placementService.createPlacement(placement));
    }
} 