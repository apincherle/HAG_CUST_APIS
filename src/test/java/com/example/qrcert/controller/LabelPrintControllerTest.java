package com.example.qrcert.controller;

import com.example.exception.GlobalExceptionHandler;
import com.example.qrcert.label.LabelPrintOperations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class LabelPrintControllerTest {

    private static final byte[] SAMPLE_PDF = new byte[] {37, 80, 68, 70};

    @Mock
    private LabelPrintOperations labelPrintService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new LabelPrintController(labelPrintService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    @Test
    void downloadLabelSheetPdfReturnsPdfAttachment() throws Exception {
        when(labelPrintService.generateSheetPdfForDate(eq(null), eq(null))).thenReturn(SAMPLE_PDF);

        mockMvc.perform(get("/api/labels/pdf"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_PDF))
            .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("labels-")))
            .andExpect(content().bytes(SAMPLE_PDF));
    }

    @Test
    void downloadLabelSheetPdfReturns404WhenNoCerts() throws Exception {
        when(labelPrintService.generateSheetPdfForDate(any(), any()))
            .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "No graded certificates found"));

        mockMvc.perform(get("/api/labels/pdf").param("date", "2026-07-07"))
            .andExpect(status().isNotFound());
    }

    @Test
    void reprintLabelReturnsPdf() throws Exception {
        when(labelPrintService.reprintSingleLabel("HAGS-2026-000123")).thenReturn(SAMPLE_PDF);

        mockMvc.perform(get("/api/labels/pdf/reprint").param("certificateNumber", "HAGS-2026-000123"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_PDF))
            .andExpect(content().bytes(SAMPLE_PDF));
    }
}
