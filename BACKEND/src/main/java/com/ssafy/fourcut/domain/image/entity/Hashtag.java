package com.ssafy.fourcut.domain.image.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "hashtag")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Hashtag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hashtag_id")
    private Integer hashtagId;

    @Column(name = "hashtag_content", nullable = false, length = 100)
    private String hashtagContent;

    @OneToMany(mappedBy = "hashtag", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HashFeed> hashFeeds = new ArrayList<>();
}