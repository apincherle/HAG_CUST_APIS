package com.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;
import java.util.List;
import java.util.ArrayList;

@Data
@org.springframework.data.mongodb.core.mapping.Document(collection = "placements")
public class Placement {
    @Id
    private String id;
    
    @Field("_metadata")
    private Metadata metadata;
    
    private User user;
    
    @Field("placement_read_access")
    private List<String> placementReadAccess;
    
    private Branch branch;
    
    @Field("client_name")
    private String clientName;
    
    private String description;
    
    @Field("effective_year")
    private Integer effectiveYear;
    
    @Field("broker_team")
    private BrokerTeam brokerTeam;
    
    @Field("inception_date")
    private String inceptionDate;
    
    private String type;
    
    @Field("underwriter_pool")
    private List<UnderwriterPool> underwriterPool;
    
    @Field("documents")
    private List<Document> documents;
    
    @Field("programmes")
    private List<Programme> programmes;
    
    private String status;
    
    @Field("submission_requests")
    private List<SubmissionRequest> submissionRequests;
    
    @Field("submission_state")
    private Document.SubmissionState submissionState;

    public Placement() {
        this.placementReadAccess = new ArrayList<>();
        this.underwriterPool = new ArrayList<>();
        this.documents = new ArrayList<>();
        this.programmes = new ArrayList<>();
        this.submissionRequests = new ArrayList<>();
    }
} 