package com.example.repository;

import com.example.model.SubmissionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SubmissionItemRepository extends JpaRepository<SubmissionItem, UUID> {
    
    List<SubmissionItem> findBySubmission_SubmissionId(UUID submissionId);
    
    @Query("SELECT si FROM SubmissionItem si WHERE si.submission.status = :status")
    List<SubmissionItem> findBySubmissionStatus(@Param("status") com.example.model.Submission.SubmissionStatus status);
}

