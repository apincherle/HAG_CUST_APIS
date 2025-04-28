package com.example.controller;

import com.example.dto.PlacementQueryRequest;
import com.example.dto.ReassignPlacementRequest;
import com.example.dto.ReassignPlacementRequest.BrokerTeamDto;
import com.example.dto.ReassignPlacementRequest.BrokerUserDto;
import com.example.model.Placement;
import com.example.service.PlacementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlacementControllerTest {

    private PlacementService placementService;
    private PlacementController placementController;
    private ObjectMapper objectMapper;
    private Placement placement;

    @BeforeEach
    void setUp() {
        placementService = mock(PlacementService.class);
        placementController = new PlacementController();
        // Inject the mock service (assuming package-private or public field)
        try {
            var field = PlacementController.class.getDeclaredField("placementService");
            field.setAccessible(true);
            field.set(placementController, placementService);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        objectMapper = new ObjectMapper();
        placement = new Placement();
        placement.setId("test-id");
        placement.setClientName("Test Client");
        placement.setStatus("draft");
    }

    @Test
    void testGetAllPlacements() {
        when(placementService.findAll()).thenReturn(Collections.singletonList(placement));
        var response = placementController.getAllPlacements();
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
        assertEquals("test-id", response.getBody().get(0).getId());
    }

    @Test
    void testGetPlacementById() {
        when(placementService.findById("test-id")).thenReturn(placement);
        var response = placementController.getPlacementById("test-id");
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("test-id", response.getBody().getId());
    }

    @Test
    void testCreatePlacementSuccess() {
        when(placementService.save(any(Placement.class))).thenReturn(placement);
        var response = placementController.createPlacement(placement);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("test-id", ((Placement)response.getBody()).getId());
    }

    @Test
    void testCreatePlacementDuplicate() {
        when(placementService.save(any(Placement.class)))
                .thenThrow(new org.springframework.dao.DuplicateKeyException("Placement with id test-id already exists."));
        var response = placementController.createPlacement(placement);
        assertEquals(409, response.getStatusCodeValue());
        assertEquals("Placement with id test-id already exists.", response.getBody());
    }

    @Test
    void testUpdatePlacementSuccess() {
        when(placementService.update(eq("test-id"), any(Placement.class))).thenReturn(placement);
        var response = placementController.updatePlacement("test-id", placement);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("test-id", ((Placement)response.getBody()).getId());
    }

    @Test
    void testUpdatePlacementNotFound() {
        when(placementService.update(eq("test-id"), any(Placement.class)))
                .thenThrow(new IllegalArgumentException("Placement with id test-id does not exist."));
        var response = placementController.updatePlacement("test-id", placement);
        assertEquals(404, response.getStatusCodeValue());
        assertEquals("Placement with id test-id does not exist.", response.getBody());
    }

    @Test
    void testDeletePlacementSuccess() {
        doNothing().when(placementService).deleteById("test-id");
        var response = placementController.deletePlacement("test-id");
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testDeletePlacementNotFound() {
        doThrow(new IllegalArgumentException("Placement with id test-id does not exist."))
                .when(placementService).deleteById("test-id");
        var response = placementController.deletePlacement("test-id");
        assertEquals(404, response.getStatusCodeValue());
        assertEquals("Placement with id test-id does not exist.", response.getBody());
    }

    @Test
    void testGetPlacementsByQuery() {
        PlacementQueryRequest queryRequest = new PlacementQueryRequest();
        queryRequest.setClient_name(List.of("Test Client"));
        queryRequest.setStatus(List.of("draft"));
        queryRequest.setUser_only(true);
        queryRequest.setOrder_by("clientName");
        queryRequest.setOrder_dir("asc");

        when(placementService.getPlacementsByQuery(any(PlacementQueryRequest.class), eq("user-123")))
                .thenReturn(Collections.singletonList(placement));

        var response = placementController.getPlacementsByQuery(queryRequest, "user-123");
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("test-id", response.getBody().get(0).getId());
    }

    @Test
    void testReassignPlacementSuccess() {
        ReassignPlacementRequest req = new ReassignPlacementRequest();
        BrokerTeamDto teamDto = new BrokerTeamDto();
        teamDto.setTeam_id("team-123");
        BrokerUserDto userDto = new BrokerUserDto();
        userDto.setUser_email("user@email.com");
        req.setBroker_team(teamDto);
        req.setBroker_user(userDto);

        doNothing().when(placementService).reassignPlacement(eq("test-id"), ArgumentMatchers.any(ReassignPlacementRequest.class));
        var response = placementController.reassignPlacement("test-id", req);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testReassignPlacementNotFound() {
        ReassignPlacementRequest req = new ReassignPlacementRequest();
        BrokerTeamDto teamDto = new BrokerTeamDto();
        teamDto.setTeam_id("team-123");
        BrokerUserDto userDto = new BrokerUserDto();
        userDto.setUser_email("user@email.com");
        req.setBroker_team(teamDto);
        req.setBroker_user(userDto);

        doThrow(new IllegalArgumentException("Broker team not found"))
                .when(placementService).reassignPlacement(eq("test-id"), ArgumentMatchers.any(ReassignPlacementRequest.class));
        var response = placementController.reassignPlacement("test-id", req);
        assertEquals(404, response.getStatusCodeValue());
        assertEquals("Broker team not found", response.getBody());
    }
} 