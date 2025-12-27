package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionCreateResponseLite {
    private SubmissionLite submission;
    private SubmissionIntakeCodeDto intakeCode;
    private List<SubmissionItemLite> items;
}

