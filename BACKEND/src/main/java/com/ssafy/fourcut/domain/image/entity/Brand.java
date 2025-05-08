package com.ssafy.fourcut.domain.image.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "brand")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Brand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "brand_id")
    private Integer brandId;

    @Column(name = "brand_name", nullable = false, length = 45)
    private String brandName;

    @OneToMany(mappedBy = "brand", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Feed> feeds;
}