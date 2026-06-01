package com.example.qrcert.repository;

import com.example.qrcert.entity.CardCertificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}

