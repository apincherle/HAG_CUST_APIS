package com.example.service;

import com.example.model.*;
import com.example.repository.*;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
//import java.util.Optional;

import com.example.dto.PlacementQueryRequest;
import com.example.dto.ReassignPlacementRequest;
import com.example.dto.*;

@Service
public class PlacementService {

    @Autowired
    private PlacementRepository placementRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private BranchRepository branchRepository;
    
    @Autowired
    private BrokerTeamRepository brokerTeamRepository;
    
    @Autowired
    private DocumentRepository documentRepository;
    
    @Autowired
    private ProgrammeRepository programmeRepository;
    
    @Autowired
    private SectionRepository sectionRepository;
    
    @Autowired
    private UnderwriterPoolRepository underwriterPoolRepository;
    
    @Autowired
    private MetadataRepository metadataRepository;

    @Autowired
    private RiskRepository riskRepository;
    
    @Autowired
    private InsuredRepository insuredRepository;
    
    @Autowired
    private LimitRepository limitRepository;
    
    @Autowired
    private PremiumRepository premiumRepository;
    
    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    private Schema schema;

    @PostConstruct
    public void init() {
        // Load the schema on service initialization
        try (InputStream inputStream = getClass().getResourceAsStream("/placements.json")) {
            JSONObject rawSchema = null;
            if (inputStream != null) {
                rawSchema = new JSONObject(new JSONTokener(inputStream));
            }
            schema = SchemaLoader.load(rawSchema);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load JSON schema", e);
        }
    }

    public Placement createPlacement(Placement placement) {
        // Convert placement to JSONObject for validation
        JSONObject jsonData = new JSONObject(placement);
        // Validate against schema
        schema.validate(jsonData);
        // If validation passes, save to MongoDB
        return placementRepository.save(placement);
    }

    @Transactional
    public Placement save(Placement placement) {
        // Check if placement with the same ID already exists
        if (placement.getId() != null && placementRepository.existsById(placement.getId())) {
            throw new DuplicateKeyException("Placement with id " + placement.getId() + " already exists.");
        }
        // 1. Save Metadata (required)
        if (placement.getMetadata() != null) {
            metadataRepository.save(placement.getMetadata());
        } else {
            throw new IllegalArgumentException("Metadata is required for placement");
        }

        // 2. Save User (required)
        if (placement.getUser() != null) {
            User savedUser = userRepository.save(placement.getUser());
            placement.setUser(savedUser);
        } else {
            throw new IllegalArgumentException("User is required for placement");
        }

        // 3. Save Branch (required)
        if (placement.getBranch() != null) {
            Branch savedBranch = branchRepository.save(placement.getBranch());
            placement.setBranch(savedBranch);
        } else {
            throw new IllegalArgumentException("Branch is required for placement");
        }

        // 4. Save Broker Team (required)
        if (placement.getBrokerTeam() != null) {
            BrokerTeam savedBrokerTeam = brokerTeamRepository.save(placement.getBrokerTeam());
            placement.setBrokerTeam(savedBrokerTeam);
        } else {
            throw new IllegalArgumentException("Broker Team is required for placement");
        }

        // 5. Save Underwriter Pool (optional)
        if (placement.getUnderwriterPool() != null && !placement.getUnderwriterPool().isEmpty()) {
            List<UnderwriterPool> savedUnderwriters = new ArrayList<>();
            for (UnderwriterPool underwriter : placement.getUnderwriterPool()) {
                // Save organization and company first if they exist
                if (underwriter.getOrganisation() != null) {
                    underwriter.setOrganisation(saveOrganisation(underwriter.getOrganisation()));
                }
                if (underwriter.getCompany() != null) {
                    underwriter.setCompany(saveCompany(underwriter.getCompany()));
                }
                savedUnderwriters.add(underwriterPoolRepository.save(underwriter));
            }
            placement.setUnderwriterPool(savedUnderwriters);
        }

        // 6. Save Documents (optional)
        if (placement.getDocuments() != null && !placement.getDocuments().isEmpty()) {
            List<Document> savedDocuments = new ArrayList<>();
            for (Document document : placement.getDocuments()) {
                savedDocuments.add(documentRepository.save(document));
            }
            placement.setDocuments(savedDocuments);
        }

        // 7. Save Programmes (optional)
        if (placement.getProgrammes() != null && !placement.getProgrammes().isEmpty()) {
            List<Programme> savedProgrammes = new ArrayList<>();
            for (Programme programme : placement.getProgrammes()) {
                // Save contracts within programmes if they exist
                if (programme.getContracts() != null && !programme.getContracts().isEmpty()) {
                    List<Contract> savedContracts = new ArrayList<>();
                    for (Contract contract : programme.getContracts()) {
                        // Save sections within contracts if they exist
                        if (contract.getSections() != null && !contract.getSections().isEmpty()) {
                            List<Section> savedSections = new ArrayList<>();
                            for (Section section : contract.getSections()) {
                                // Save risks
                                if (section.getRisks() != null) {
                                    section.setRisks(section.getRisks().stream()
                                        .map(riskRepository::save)
                                        .collect(Collectors.toList()));
                                }
                                
                                // Save insureds
                                if (section.getInsureds() != null) {
                                    section.setInsureds(section.getInsureds().stream()
                                        .map(insuredRepository::save)
                                        .collect(Collectors.toList()));
                                }
                                
                                // Save limits
                                if (section.getLimits() != null) {
                                    section.setLimits(section.getLimits().stream()
                                        .map(limitRepository::save)
                                        .collect(Collectors.toList()));
                                }
                                
                                // Save premiums
                                if (section.getPremiums() != null) {
                                    section.setPremiums(section.getPremiums().stream()
                                        .map(premiumRepository::save)
                                        .collect(Collectors.toList()));
                                }
                                
                                savedSections.add(sectionRepository.save(section));
                            }
                            contract.setSections(savedSections);
                        }
                        savedContracts.add(contractRepository.save(contract));
                    }
                    programme.setContracts(savedContracts);
                }
                savedProgrammes.add(programmeRepository.save(programme));
            }
            placement.setProgrammes(savedProgrammes);
        }

        // 8. Finally save the placement
        return placementRepository.save(placement);
    }

    @Transactional
    public Placement update(String id, Placement updatedPlacement) {
        if (!placementRepository.existsById(id)) {
            throw new IllegalArgumentException("Placement with id " + id + " does not exist.");
        }
        updatedPlacement.setId(id);
        return placementRepository.save(updatedPlacement);
    }

    private UnderwriterPool.Organisation saveOrganisation(UnderwriterPool.Organisation organisation) {
        // Save organization logic here
        return organisation;
    }

    private UnderwriterPool.Company saveCompany(UnderwriterPool.Company company) {
        // Save company logic here
        return company;
    }

    @Transactional(readOnly = true)
    public Placement findById(String id) {
        return placementRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Placement not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Placement> findAll() {
        return placementRepository.findAll();
    }

    @Transactional
    public void deleteById(String id) {
        if (!placementRepository.existsById(id)) {
            throw new IllegalArgumentException("Placement with id " + id + " does not exist.");
        }
        placementRepository.deleteById(id);
    }

    public List<Placement> getPlacementsByQuery(PlacementQueryRequest req, String currentUser) {
        Query query = new Query();

        if (req.getClient_name() != null && !req.getClient_name().isEmpty()) {
            query.addCriteria(Criteria.where("clientName").in(req.getClient_name()));
        }
        if (req.getPlacement_name() != null && !req.getPlacement_name().isEmpty()) {
            query.addCriteria(Criteria.where("description").in(req.getPlacement_name()));
        }
        if (req.getEffective_year() != null && !req.getEffective_year().isEmpty()) {
            List<Integer> years = req.getEffective_year().stream().map(Integer::parseInt).toList();
            query.addCriteria(Criteria.where("effectiveYear").in(years));
        }
        if (req.getOwner_name() != null && !req.getOwner_name().isEmpty()) {
            query.addCriteria(Criteria.where("user.firstName").in(req.getOwner_name()));
        }
        if (req.getStatus() != null && !req.getStatus().isEmpty()) {
            query.addCriteria(Criteria.where("status").in(req.getStatus()));
        }
        if (req.getInception_from() != null && !req.getInception_from().isEmpty()) {
            query.addCriteria(Criteria.where("inceptionDate").gte(req.getInception_from()));
        }
        if (req.getInception_to() != null && !req.getInception_to().isEmpty()) {
            query.addCriteria(Criteria.where("inceptionDate").lte(req.getInception_to()));
        }
//        // user_only: if true, filter by current user
//        if (req.getUser_only() == null || req.getUser_only()) {
//            query.addCriteria(Criteria.where("user._xid").is(currentUser));
//        }

        // Ordering
        if (req.getOrder_by() != null && !req.getOrder_by().isEmpty()) {
            String field = switch (req.getOrder_by()) {
                case "clientName" -> "clientName";
                case "placementName" -> "description";
                case "effectiveYear" -> "effectiveYear";
                case "ownerName" -> "user.firstName";
                case "contractInceptiondate" -> "inceptionDate";
                case "Status" -> "status";
                default -> "clientName";
            };
            Sort.Direction dir = "desc".equalsIgnoreCase(req.getOrder_dir()) ? Sort.Direction.DESC : Sort.Direction.ASC;
            query.with(Sort.by(dir, field));
        }

        return mongoTemplate.find(query, Placement.class);
    }

    public void reassignPlacement(String placementId, ReassignPlacementRequest request) {
        // Lookup broker team
        BrokerTeam team = brokerTeamRepository.findById(request.getBroker_team().getTeam_id())
                .orElseThrow(() -> new IllegalArgumentException("Broker team not found"));

        // Lookup broker user by email (assuming you have a method for this)
        //User user = userRepository.findByEmail(request.getBroker_user().getUser_email())
        //.orElseThrow(() -> new IllegalArgumentException("Broker user not found"));

        // You can now update the placement as needed, e.g.:
        Placement placement = placementRepository.findById(placementId)
                .orElseThrow(() -> new IllegalArgumentException("Placement not found"));

        placement.setBrokerTeam(team);
        //placement.setUser(user);

        placementRepository.save(placement);
    }

    @Transactional(readOnly = true)
    public PlacementMarketResponse getPlacementsForMarket() {
        List<Placement> placements = placementRepository.findAll();
        
        List<PlacementMarketDto> marketPlacements = placements.stream()
            .map(this::convertToMarketDto)
            .collect(Collectors.toList());
        
        PlacementMarketResponse response = new PlacementMarketResponse();
        response.setPlacements(marketPlacements);
        response.setPageNumber(0);
        response.setPageSize(marketPlacements.size());
        response.setCount((long) marketPlacements.size());
        response.setTotalResults((long) marketPlacements.size());
        
        return response;
    }

    private PlacementMarketDto convertToMarketDto(Placement placement) {
        PlacementMarketDto dto = new PlacementMarketDto();
        
        // Basic placement info
        dto.setPlacementId(placement.getId());
        dto.setClientName(placement.getClientName());
        dto.setDescription(placement.getDescription());
        dto.setEffectiveYear(placement.getEffectiveYear());
        dto.setEarliestInceptionDate(placement.getInceptionDate());
        dto.setStatus(placement.getStatus());
        dto.setType(placement.getType());
        
        // Metadata
        if (placement.getMetadata() != null) {
            PlacementMetadataDto metadataDto = new PlacementMetadataDto();
            metadataDto.setCreatedDate(placement.getMetadata().getCreationDate());
            metadataDto.setCreatedChannel(placement.getMetadata().getCreationChannel());
            metadataDto.setModifiedDate(placement.getMetadata().getModifiedDate());
            metadataDto.setModifiedChannel(placement.getMetadata().getModifiedChannel());
            
            // Created by user info
            if (placement.getUser() != null) {
                UserInfoDto createdBy = new UserInfoDto();
                createdBy.setFirstName(placement.getUser().getFirstName());
                createdBy.setLastName(placement.getUser().getLastName());
                // Note: user_email is not available in the current User model
                metadataDto.setCreatedBy(createdBy);
                metadataDto.setModifiedBy(createdBy); // Using same user for both
            }
            
            dto.setMetadata(metadataDto);
        }
        
        // Broker team info
        if (placement.getBrokerTeam() != null) {
            BrokerTeamDto brokerTeamDto = new BrokerTeamDto();
            brokerTeamDto.setTeamId(placement.getBrokerTeam().getXid());
            brokerTeamDto.setTeamName(placement.getBrokerTeam().getName());
            // Note: company_name and branch_name would need to be looked up from related entities
            dto.setBrokerTeam(brokerTeamDto);
        }
        
        // Broker user info
        if (placement.getUser() != null) {
            BrokerUserDto brokerUserDto = new BrokerUserDto();
            brokerUserDto.setFirstName(placement.getUser().getFirstName());
            brokerUserDto.setLastName(placement.getUser().getLastName());
            // Note: user_email is not available in the current User model
            dto.setBrokerUser(brokerUserDto);
        }
        
        // Programmes
        if (placement.getProgrammes() != null && !placement.getProgrammes().isEmpty()) {
            List<ProgrammeDto> programmeDtos = placement.getProgrammes().stream()
                .map(this::convertProgrammeToDto)
                .collect(Collectors.toList());
            dto.setProgrammes(programmeDtos);
        }
        
        return dto;
    }

    private ProgrammeDto convertProgrammeToDto(Programme programme) {
        ProgrammeDto dto = new ProgrammeDto();
        dto.setProgrammeId(programme.getId());
        dto.setDescription(programme.getDescription());
        dto.setEarliestInceptionDate(programme.getInceptionDate());
        dto.setStatus(programme.getStatusCode());
        return dto;
    }
} 