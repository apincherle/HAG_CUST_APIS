package com.example.controller;

import com.example.model.Placement;
import com.example.service.PlacementService;
import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/placements")
@Tag(name = "Placements", description = "Placement management APIs")
public class PlacementController {

    @Autowired
    private PlacementService placementService;

    @Operation(summary = "Get all placements")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved placements"),
        @ApiResponse(responseCode = "404", description = "No placements found")
    })
    @GetMapping
    public ResponseEntity<List<Placement>> getAllPlacements() {
        return ResponseEntity.ok(placementService.findAll());
    }

    @Operation(summary = "Get a placement by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved the placement"),
        @ApiResponse(responseCode = "404", description = "Placement not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Placement> getPlacementById(@PathVariable String id) {
        return ResponseEntity.ok(placementService.findById(id));
    }

    @Operation(summary = "Create a new placement")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully created the placement")
    })
    @PostMapping
    public ResponseEntity<Placement> createPlacement(@RequestBody Placement placement) {
        return ResponseEntity.ok(placementService.save(placement));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlacement(@PathVariable String id) {
        placementService.deleteById(id);
        return ResponseEntity.ok().build();
    }
} 