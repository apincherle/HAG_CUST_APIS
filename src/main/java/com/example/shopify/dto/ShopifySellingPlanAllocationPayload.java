package com.example.shopify.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShopifySellingPlanAllocationPayload {

    @JsonProperty("selling_plan")
    private ShopifySellingPlanPayload sellingPlan;
    @JsonProperty("selling_plan_group_id")
    private Long sellingPlanGroupId;
}
