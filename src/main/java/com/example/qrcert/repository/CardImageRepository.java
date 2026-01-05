package com.example.qrcert.repository;

import com.example.qrcert.entity.CardImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardImageRepository extends JpaRepository<CardImage, Long> {
    List<CardImage> findByCertificateId(Long certificateId);
}

