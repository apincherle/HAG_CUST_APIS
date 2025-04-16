package com.example.service;

import com.example.model.*;
import com.example.repository.*;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
//import java.util.Optional;

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
                // Save sections within programmes if they exist
                if (programme.getSections() != null && !programme.getSections().isEmpty()) {
                    List<Section> savedSections = new ArrayList<>();
                    for (Section section : programme.getSections()) {
                        savedSections.add(sectionRepository.save(section));
                    }
                    programme.setSections(savedSections);
                }
                savedProgrammes.add(programmeRepository.save(programme));
            }
            placement.setProgrammes(savedProgrammes);
        }

        // 8. Finally save the placement
        return placementRepository.save(placement);
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
        placementRepository.deleteById(id);
    }
} 