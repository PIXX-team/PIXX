package com.ssafy.fourcut.domain.image.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "face_vector")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class FaceVector {
    @Id
    @Column(name = "face_id")
    private Integer faceId;

    @ElementCollection
    @CollectionTable(name = "face_vector_data", joinColumns = @JoinColumn(name = "face_id"))
    @Column(name = "vector_data")
    private List<Double> vectorData;

    @OneToMany(mappedBy = "faceVector", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Feed> feed;

}
