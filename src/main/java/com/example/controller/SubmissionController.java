package com.example.controller;

import com.example.dto.*;
import com.example.service.SubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/submissions")
@Tag(name = "Submissions", description = "Submission management API")
public class SubmissionController {
    
    @Autowired
    private SubmissionService submissionService;
    
    @PostMapping
    @Operation(summary = "Create submission", operationId = "createSubmission")
    public ResponseEntity<SubmissionCreateResponseLite> createSubmission(
            @Valid @RequestBody SubmissionCreateRequestLite request) {
        SubmissionCreateResponseLite response = submissionService.createSubmission(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Location", "/v1/submissions/" + response.getSubmission().getSubmissionId())
                .body(response);
    }
    
    @GetMapping("/{submission_id}")
    @Operation(summary = "Get submission by id", operationId = "getSubmission")
    public ResponseEntity<SubmissionLite> getSubmission(
            @Parameter(description = "Submission GUID", required = true) 
            @PathVariable("submission_id") UUID submissionId) {
        SubmissionLite submission = submissionService.getSubmissionById(submissionId);
        return ResponseEntity.ok(submission);
    }
    
    @GetMapping("/customer/{customer_id}")
    @Operation(summary = "Get all submissions for a customer", operationId = "getSubmissionsByCustomer")
    public ResponseEntity<List<SubmissionLite>> getSubmissionsByCustomer(
            @Parameter(description = "Customer GUID", required = true) 
            @PathVariable("customer_id") UUID customerId) {
        List<SubmissionLite> submissions = submissionService.getSubmissionsByCustomerId(customerId);
        return ResponseEntity.ok(submissions);
    }
}

