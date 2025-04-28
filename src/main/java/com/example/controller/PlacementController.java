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
import org.springframework.dao.DuplicateKeyException;

import java.util.List;

import com.example.dto.PlacementQueryRequest;
import com.example.dto.ReassignPlacementRequest;

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
        @ApiResponse(responseCode = "200", description = "Successfully created the placement"),
        @ApiResponse(responseCode = "409", description = "Placement with the same ID already exists")
    })
    @PostMapping
    public ResponseEntity<?> createPlacement(@RequestBody Placement placement) {
        try {
            return ResponseEntity.ok(placementService.save(placement));
        } catch (DuplicateKeyException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePlacement(@PathVariable String id, @RequestBody Placement placement) {
        try {
            return ResponseEntity.ok(placementService.update(id, placement));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePlacement(@PathVariable String id) {
        try {
            placementService.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @PostMapping("/query")
    @Operation(summary = "Query placements with multiple filters and ordering")
    public ResponseEntity<List<Placement>> getPlacementsByQuery(
            @RequestBody PlacementQueryRequest request,
            @RequestHeader("X-User-Id") String userId // or get from security context
    ) {
        List<Placement> results = placementService.getPlacementsByQuery(request, userId);
        return ResponseEntity.ok(results);
    }

    @Operation(
        summary = "Reassign a placement to a new broker team and user",
        description = "Reassigns the placement to the specified broker team and broker user. Throws an error if either does not exist."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Placement reassigned successfully"),
        @ApiResponse(responseCode = "404", description = "Placement, broker team, or broker user not found")
    })
    @PostMapping("/{id}/reassign")
    public ResponseEntity<?> reassignPlacement(
            @PathVariable String id,
            @org.springframework.web.bind.annotation.RequestBody ReassignPlacementRequest request
    ) {
        try {
            placementService.reassignPlacement(id, request);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }
} 