package com.example.qrcert.repository;

import com.example.qrcert.entity.CardCertificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CardCertificateRepository extends JpaRepository<CardCertificate, Long> {
    Optional<CardCertificate> findByPublicId(String publicId);
    Optional<CardCertificate> findBySerialNumber(String serialNumber);
    Optional<CardCertificate> findBySerialNumberIgnoreCase(String serialNumber);
    Optional<CardCertificate> findByInspectionId(String inspectionId);
    Optional<CardCertificate> findByItemId(String itemId);
    List<CardCertificate> findAllByItemId(String itemId);
    boolean existsByPublicId(String publicId);
    boolean existsBySerialNumber(String serialNumber);

    @Query("""
        SELECT c FROM CardCertificate c
        WHERE c.status = 'VERIFIED'
          AND c.gradedAt >= :startInclusive
          AND c.gradedAt < :endExclusive
        ORDER BY c.gradedAt ASC, c.id ASC
        """)
    List<CardCertificate> findGradedOnDate(
        @Param("startInclusive") LocalDateTime startInclusive,
        @Param("endExclusive") LocalDateTime endExclusive
    );

    @Query("""
        SELECT c FROM CardCertificate c
        WHERE c.status = 'VERIFIED'
          AND c.submissionId = :submissionId
          AND c.gradedAt >= :startInclusive
          AND c.gradedAt < :endExclusive
        ORDER BY c.gradedAt ASC, c.id ASC
        """)
    List<CardCertificate> findGradedOnDateBySubmission(
        @Param("submissionId") String submissionId,
        @Param("startInclusive") LocalDateTime startInclusive,
        @Param("endExclusive") LocalDateTime endExclusive
    );
}

