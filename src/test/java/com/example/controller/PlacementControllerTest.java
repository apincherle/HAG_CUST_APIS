package com.example.controller;

import com.example.model.Placement;
import com.example.service.PlacementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.dto.PlacementQueryRequest;

@SpringBootTest
@AutoConfigureMockMvc
class PlacementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlacementService placementService;

    @Autowired
    private ObjectMapper objectMapper;

    private Placement placement;

    @BeforeEach
    void setUp() {
        placement = new Placement();
        placement.setId("test-id");
        placement.setClientName("Test Client");
        placement.setStatus("draft");
        // Set other required fields as needed for your schema...
    }

    @Test
    void testGetAllPlacements() throws Exception {
        Mockito.when(placementService.findAll()).thenReturn(Collections.singletonList(placement));

        mockMvc.perform(get("/api/placements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]._id").value("test-id"));
    }

    @Test
    void testGetPlacementById() throws Exception {
        Mockito.when(placementService.findById("test-id")).thenReturn(placement);

        mockMvc.perform(get("/api/placements/test-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._id").value("test-id"));
    }

    @Test
    void testCreatePlacementSuccess() throws Exception {
        Mockito.when(placementService.save(any(Placement.class))).thenReturn(placement);

        mockMvc.perform(post("/api/placements")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(placement)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._id").value("test-id"));
    }

    @Test
    void testCreatePlacementDuplicate() throws Exception {
        Mockito.when(placementService.save(any(Placement.class)))
                .thenThrow(new DuplicateKeyException("Placement with id test-id already exists."));

        mockMvc.perform(post("/api/placements")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(placement)))
                .andExpect(status().isConflict())
                .andExpect(content().string("Placement with id test-id already exists."));
    }

    @Test
    void testUpdatePlacementSuccess() throws Exception {
        Mockito.when(placementService.update(eq("test-id"), any(Placement.class))).thenReturn(placement);

        mockMvc.perform(put("/api/placements/test-id")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(placement)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._id").value("test-id"));
    }

    @Test
    void testUpdatePlacementNotFound() throws Exception {
        Mockito.when(placementService.update(eq("test-id"), any(Placement.class)))
                .thenThrow(new IllegalArgumentException("Placement with id test-id does not exist."));

        mockMvc.perform(put("/api/placements/test-id")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(placement)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Placement with id test-id does not exist."));
    }

    @Test
    void testDeletePlacementSuccess() throws Exception {
        Mockito.doNothing().when(placementService).deleteById("test-id");

        mockMvc.perform(delete("/api/placements/test-id"))
                .andExpect(status().isOk());
    }

    @Test
    void testDeletePlacementNotFound() throws Exception {
        Mockito.doThrow(new IllegalArgumentException("Placement with id test-id does not exist."))
                .when(placementService).deleteById("test-id");

        mockMvc.perform(delete("/api/placements/test-id"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Placement with id test-id does not exist."));
    }

    @Test
    void testGetPlacementsByQuery() throws Exception {
        PlacementQueryRequest queryRequest = new PlacementQueryRequest();
        queryRequest.setClient_name(List.of("Test Client"));
        queryRequest.setStatus(List.of("draft"));
        queryRequest.setUser_only(true);
        queryRequest.setOrder_by("clientName");
        queryRequest.setOrder_dir("asc");

        List<Placement> placements = Collections.singletonList(placement);

        Mockito.when(placementService.getPlacementsByQuery(any(PlacementQueryRequest.class), eq("user-123")))
                .thenReturn(placements);

        mockMvc.perform(post("/api/placements/query")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", "user-123")
                .content(objectMapper.writeValueAsString(queryRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]._id").value("test-id"));
    }
} 