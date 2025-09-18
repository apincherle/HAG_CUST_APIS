package com.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
import java.util.ArrayList;

@Data
public class Contract {
    @JsonProperty("_id")
    private String id;
    
    @JsonProperty("broker_team")
    private BrokerTeam brokerTeam;
    
    private User user;
    
    @JsonProperty("broker_code")
    private String brokerCode;
    
    @JsonProperty("broker_contract_ref")
    private String brokerContractRef;
    
    @JsonProperty("contract_umr")
    private String contractUmr;
    
    @JsonProperty("sequence_number")
    private Integer sequenceNumber;
    
    private String description;
    
    @JsonProperty("contract_type")
    private String contractType;
    
    @JsonProperty("cover_type")
    private String coverType;
    
    @JsonProperty("insureds")
    private List<Insured> insureds;
    
    private String status;
    
    @JsonProperty("sub_status")
    private String subStatus;
    
    @JsonProperty("documents")
    private List<Document> documents;
    
    @JsonProperty("sections")
    private List<Section> sections;
    
    @JsonProperty("backload_indicator")
    private Boolean backloadIndicator;
    
    @JsonProperty("backload_reason_code")
    private String backloadReasonCode;
    
    @JsonProperty("backload_description")
    private String backloadDescription;
    
    @JsonProperty("submission_state")
    private Document.SubmissionState submissionState;
    
    private Integer version;
    
    @JsonProperty("based_on_version")
    private Integer basedOnVersion;
    
    @JsonProperty("facility_published")
    private Boolean facilityPublished;
    
    @JsonProperty("facility_access")
    private List<String> facilityAccess;
    
    @JsonProperty("_metadata")
    private Metadata metadata;
    
    @JsonProperty("unresolved_fo")
    private Boolean unresolvedFo;
    
    @JsonProperty("first_sign_and_close_date")
    private String firstSignAndCloseDate;
    
    @JsonProperty("read_access")
    private List<String> readAccess;
    
    private Locking locking;
    
    @JsonProperty("status_flags")
    private List<String> statusFlags;
    
    @JsonProperty("umr_released")
    private Boolean umrReleased;
    
    @JsonProperty("uses_digital_contract")
    private Boolean usesDigitalContract;
    
    @JsonProperty("digital_contract")
    private DigitalContract digitalContract;
    
    @JsonProperty("cancel_and_replace_allowed")
    private Boolean cancelAndReplaceAllowed;
    
    @JsonProperty("renewed_from")
    private List<RenewalInfo> renewedFrom;
    
    @JsonProperty("renewed_to")
    private List<RenewalInfo> renewedTo;

    public Contract() {
        this.insureds = new ArrayList<>();
        this.documents = new ArrayList<>();
        this.sections = new ArrayList<>();
        this.facilityAccess = new ArrayList<>();
        this.readAccess = new ArrayList<>();
        this.statusFlags = new ArrayList<>();
        this.renewedFrom = new ArrayList<>();
        this.renewedTo = new ArrayList<>();
    }
} 