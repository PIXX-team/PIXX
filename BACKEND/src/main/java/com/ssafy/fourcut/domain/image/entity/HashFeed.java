package com.ssafy.fourcut.domain.image.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "hash_feed")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class HashFeed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hash_feed_id")
    private Integer hashFeedId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "feed_id", nullable = false)
    private Feed feed;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hashtag_id", nullable = false)
    private Hashtag hashtag;
}
