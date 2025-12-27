package com.example.service;

import com.example.dto.*;
import com.example.dto.SubmissionIntakeCodeDto;
import com.example.model.Submission;
import com.example.model.SubmissionItem;
import com.example.model.SubmissionIntakeCode;
import com.example.repository.CustomerRepository;
import com.example.repository.SubmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SubmissionService {
    
    @Autowired
    private SubmissionRepository submissionRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Transactional
    public SubmissionCreateResponseLite createSubmission(SubmissionCreateRequestLite request) {
        // Verify customer exists using native query to avoid UUID conversion issues
        String customerIdString = request.getCustomerId().toString();
        if (!customerRepository.findByCustomerIdNative(customerIdString).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer not found");
        }
        
        // Create submission
        Submission submission = new Submission();
        submission.setCustomerId(request.getCustomerId());
        submission.setServiceLevel(request.getServiceLevel() != null ? request.getServiceLevel() : Submission.ServiceLevel.BRONZE);
        // shippingAddressId removed - will use customer's shipping address when needed
        submission.setNotesCustomer(request.getNotesCustomer());
        submission.setStatus(Submission.SubmissionStatus.DRAFT);
        
        // Generate submission number
        String submissionNumber = "SUB-" + System.currentTimeMillis();
        submission.setSubmissionNumber(submissionNumber);
        
        // Create items
        List<SubmissionItem> items = new java.util.ArrayList<>();
        for (int i = 0; i < request.getItems().size(); i++) {
            SubmissionItemCreateLite itemRequest = request.getItems().get(i);
            SubmissionItem item = new SubmissionItem();
            item.setSubmission(submission);
            item.setLineNumber(i + 1);
            item.setGame(itemRequest.getGame());
            item.setFreeTextLine(itemRequest.getFreeTextLine());
            item.setCustomerNotes(itemRequest.getCustomerNotes());
            item.setRequestedPhotoSlots(itemRequest.getRequestedPhotoSlots() != null ? 
                    itemRequest.getRequestedPhotoSlots() : 2);
            item.setFrontPhotoId(itemRequest.getFrontPhotoId());
            item.setBackPhotoId(itemRequest.getBackPhotoId());
            item.setEnrichmentStatus(SubmissionItem.EnrichmentStatus.PENDING);
            items.add(item);
        }
        submission.setItems(items);
        
        // Create intake code
        SubmissionIntakeCode intakeCode = new SubmissionIntakeCode();
        intakeCode.setSubmission(submission);
        intakeCode.setValue(submissionNumber);
        intakeCode.setBarcodeFormat(SubmissionIntakeCode.BarcodeFormat.CODE_128);
        intakeCode.setQrValue("https://api.yourdomain.com/submissions/" + submission.getSubmissionId());
        submission.setIntakeCode(intakeCode);
        
        submission = submissionRepository.save(submission);
        
        // Build response
        List<SubmissionItemLite> itemLites = submission.getItems().stream()
                .map(SubmissionItemLite::fromEntity)
                .collect(Collectors.toList());
        
        return SubmissionCreateResponseLite.builder()
                .submission(SubmissionLite.fromEntity(submission))
                .intakeCode(SubmissionIntakeCodeDto.fromEntity(submission.getIntakeCode()))
                .items(itemLites)
                .build();
    }
    
    public SubmissionLite getSubmissionById(UUID submissionId) {
        Submission submission = submissionRepository.findBySubmissionIdAndStatusNot(
                submissionId, Submission.SubmissionStatus.CANCELLED)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Submission not found"));
        return SubmissionLite.fromEntity(submission);
    }
    
    public List<SubmissionLite> getSubmissionsByCustomerId(UUID customerId) {
        List<Submission> submissions = submissionRepository.findByCustomerIdAndStatusNot(
                customerId, Submission.SubmissionStatus.CANCELLED);
        return submissions.stream()
                .map(SubmissionLite::fromEntity)
                .collect(Collectors.toList());
    }
}

