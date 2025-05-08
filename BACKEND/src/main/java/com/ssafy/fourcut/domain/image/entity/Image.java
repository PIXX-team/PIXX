package com.ssafy.fourcut.domain.image.entity;

import com.ssafy.fourcut.domain.image.entity.enums.ImageType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CurrentTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "image")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Integer imageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_id", nullable = false)
    private Feed feed;

    @Column(name = "image_url", nullable = false, length = 200)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "image_type", nullable = false, length = 10)
    private ImageType imageType;

    @Column(name = "created_at", nullable = false)
    @CurrentTimestamp
    private LocalDateTime createdAt;
}