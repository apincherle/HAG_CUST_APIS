package com.example.dto;

import com.example.model.SubmissionItem;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SubmissionItemCreateLite {
    @NotNull(message = "Game is required")
    private SubmissionItem.GameType game;
    
    @NotBlank(message = "Free text line is required")
    @Size(max = 300, message = "Free text line must not exceed 300 characters")
    private String freeTextLine;
    
    @Size(max = 1000, message = "Customer notes must not exceed 1000 characters")
    private String customerNotes;
    
    @Min(value = 2, message = "Requested photo slots must be exactly 2 (front and back)")
    @Max(value = 2, message = "Requested photo slots must be exactly 2 (front and back)")
    private Integer requestedPhotoSlots = 2;
    
    @Size(max = 255, message = "Front photo ID must not exceed 255 characters")
    private String frontPhotoId;
    
    @Size(max = 255, message = "Back photo ID must not exceed 255 characters")
    private String backPhotoId;
}

