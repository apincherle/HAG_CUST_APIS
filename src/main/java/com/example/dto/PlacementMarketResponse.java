package com.example.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class PlacementMarketResponse {
    private List<PlacementMarketDto> placements;
    
    @JsonProperty("page_number")
    private Integer pageNumber;
    
    @JsonProperty("page_size")
    private Integer pageSize;
    
    private Long count;
    
    @JsonProperty("total_results")
    private Long totalResults;
}
