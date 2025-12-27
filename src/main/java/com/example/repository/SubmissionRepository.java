package com.example.repository;

import com.example.model.Submission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, UUID> {
    
    List<Submission> findByCustomerId(UUID customerId);
    
    Optional<Submission> findBySubmissionNumber(String submissionNumber);
    
    @Query("SELECT s FROM Submission s WHERE " +
           "(:customerId IS NULL OR s.customerId = :customerId) AND " +
           "(:status IS NULL OR s.status = :status) AND " +
           "(:q IS NULL OR LOWER(s.submissionNumber) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(s.notesCustomer) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Submission> searchSubmissions(
            @Param("customerId") UUID customerId,
            @Param("status") Submission.SubmissionStatus status,
            @Param("q") String q,
            Pageable pageable
    );
    
    long countByCustomerId(UUID customerId);
}

