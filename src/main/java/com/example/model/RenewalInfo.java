package com.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RenewalInfo {
    private String contract;
    private String placement;
}
