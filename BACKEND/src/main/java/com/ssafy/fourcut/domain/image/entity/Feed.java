package com.ssafy.fourcut.domain.image.entity;

import com.ssafy.fourcut.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CurrentTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "feed")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Feed {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feed_id")
    private Integer feedId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id", nullable = false)
    private Album album;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "face_id", nullable = true)
    private FaceVector faceVector;

    @Column(name = "feed_title", length = 100)
    private String feedTitle;

    @Column(name = "feed_location", length = 100)
    private String feedLocation;

    @Column(name = "feed_population", length = 100)
    private Integer feedPopulation;

    @Column(name = "feed_date", nullable = false)
    @CurrentTimestamp
    private LocalDateTime feedDate;

    @Column(name = "feed_memo")
    private String feedMemo;

    @Column(name = "feed_favorite", nullable = false, columnDefinition="boolean default false")
    private Boolean feedFavorite;

    @OneToMany(mappedBy = "feed", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<HashFeed> hashFeeds;

    @OneToMany(mappedBy = "feed", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Image> images;
}