package com.example.dto;

import com.example.model.SubmissionIntakeCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionIntakeCodeDto {
    private String value;
    private SubmissionIntakeCode.BarcodeFormat barcodeFormat;
    private String qrValue;
    
    public static SubmissionIntakeCodeDto fromEntity(com.example.model.SubmissionIntakeCode entity) {
        return SubmissionIntakeCodeDto.builder()
                .value(entity.getValue())
                .barcodeFormat(entity.getBarcodeFormat())
                .qrValue(entity.getQrValue())
                .build();
    }
}

