package com.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
import java.util.ArrayList;

@Data
public class Section {
    @JsonProperty("_id")
    private String id;
    
    private Boolean default_;
    
    @JsonProperty("geographic_coverage")
    private GeographicCoverage geographicCoverage;
    
    @JsonProperty("geographic_coverage_description")
    private String geographicCoverageDescription;
    
    private String reference;
    private String description;
    
    @JsonProperty("sequence_number")
    private Integer sequenceNumber;
    
    private String status;
    
    @JsonProperty("classification_type")
    private String classificationType;
    
    @JsonProperty("cover_type")
    private String coverType;
    
    @JsonProperty("insureds")
    private List<Insured> insureds;
    
    @JsonProperty("product_code")
    private String productCode;
    
    @JsonProperty("inception_date")
    private String inceptionDate;
    
    @JsonProperty("expiry_date")
    private String expiryDate;
    
    @JsonProperty("period_type")
    private String periodType;
    
    @JsonProperty("period_duration_number")
    private Integer periodDurationNumber;
    
    @JsonProperty("duration_unit_type_code")
    private String durationUnitTypeCode;
    
    @JsonProperty("period_qualifier_type_code")
    private String periodQualifierTypeCode;
    
    @JsonProperty("period_description")
    private String periodDescription;
    
    @JsonProperty("order_percentage")
    private Double orderPercentage;
    
    @JsonProperty("line_of_business")
    private String lineOfBusiness;
    
    @JsonProperty("class_of_business")
    private String classOfBusiness;
    
    @JsonProperty("settlement_due_date")
    private String settlementDueDate;
    
    @JsonProperty("installment_period_of_credit")
    private Integer installmentPeriodOfCredit;
    
    @JsonProperty("adjustment_period_of_credit")
    private Integer adjustmentPeriodOfCredit;
    
    @JsonProperty("allocation_of_premium_to_year_of_account")
    private Integer allocationOfPremiumToYearOfAccount;
    
    @JsonProperty("stamp_permission_type")
    private String stampPermissionType;
    
    @JsonProperty("risks")
    private List<Risk> risks;
    
    @JsonProperty("limits")
    private List<Limit> limits;
    
    @JsonProperty("excesses")
    private List<Excess> excesses;
    
    @JsonProperty("deductibles")
    private List<Deductible> deductibles;
    
    @JsonProperty("premiums")
    private List<Premium> premiums;
    
    @JsonProperty("binding_information")
    private BindingInformation bindingInformation;
    
    @JsonProperty("documents")
    private List<Document> documents;
    
    @JsonProperty("submission_state")
    private Document.SubmissionState submissionState;
    
    @JsonProperty("_metadata")
    private Metadata metadata;
    
    @JsonProperty("period_qualifier_description")
    private String periodQualifierDescription;
    
    @JsonProperty("unresolved_fo")
    private Boolean unresolvedFo;
    
    @JsonProperty("facility_usage")
    private List<String> facilityUsage;
    
    private Locking locking;
    
    @JsonProperty("conveyance_description")
    private String conveyanceDescription;
    
    @JsonProperty("interest_description")
    private String interestDescription;
    
    @JsonProperty("status_flags")
    private List<String> statusFlags;

    public Section() {
        this.insureds = new ArrayList<>();
        this.risks = new ArrayList<>();
        this.limits = new ArrayList<>();
        this.excesses = new ArrayList<>();
        this.deductibles = new ArrayList<>();
        this.premiums = new ArrayList<>();
        this.documents = new ArrayList<>();
        this.facilityUsage = new ArrayList<>();
        this.statusFlags = new ArrayList<>();
    }
    
    @Data
    public static class BindingInformation {
        @JsonProperty("written_line_type")
        private String writtenLineType;
        
        @JsonProperty("currency_code")
        private String currencyCode;
        
        @JsonProperty("written_line_basis")
        private String writtenLineBasis;
        
        @JsonProperty("signed_line_basis")
        private String signedLineBasis;
        
        @JsonProperty("signed_down_decimal_places")
        private Integer signedDownDecimalPlaces;
        
        @JsonProperty("signed_down_order_percentage")
        private Double signedDownOrderPercentage;
    }
} 