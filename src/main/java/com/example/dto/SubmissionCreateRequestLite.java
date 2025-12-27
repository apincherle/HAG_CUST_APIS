package com.example.dto;

import com.example.model.Submission;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class SubmissionCreateRequestLite {
    @NotNull(message = "Customer ID is required")
    private UUID customerId;
    
    private Submission.ServiceLevel serviceLevel = Submission.ServiceLevel.BRONZE;
    
    @Size(max = 2000, message = "Customer notes must not exceed 2000 characters")
    private String notesCustomer;
    
    @NotEmpty(message = "At least one item is required")
    @Size(min = 1, max = 500, message = "Items must be between 1 and 500")
    @Valid
    private List<SubmissionItemCreateLite> items;
}

