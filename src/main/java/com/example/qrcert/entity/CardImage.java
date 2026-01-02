package com.example.qrcert.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "card_image", indexes = {
    @Index(name = "idx_card_image_certificate_id", columnList = "certificate_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "certificate_id", nullable = false, foreignKey = @ForeignKey(name = "fk_card_image_certificate"))
    private CardCertificate certificate;

    @Column(name = "kind", nullable = false, length = 50)
    private String kind; // front, back, surface_L, surface_R, etc.

    @Column(name = "url", nullable = false, length = 500)
    private String url;

    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}

