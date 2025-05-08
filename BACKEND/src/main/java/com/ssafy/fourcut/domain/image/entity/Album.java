package com.ssafy.fourcut.domain.image.entity;

import com.ssafy.fourcut.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CurrentTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "album")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Album {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "album_id")
    private Integer albumId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "album_name", nullable = false, length = 100)
    private String albumName;

    @Column(name = "album_memo", length = 100)
    private String albumMemo;

    @Column(name = "created_at", nullable = false)
    @CurrentTimestamp
    private LocalDateTime createdAt;

    @Column(name = "default_album", nullable = false, columnDefinition="boolean default false")
    private Boolean defaultAlbum;

    @Column(name = "favorite_album", nullable = false, columnDefinition="boolean default false")
    private Boolean favoriteAlbum;

    @OneToMany(mappedBy = "album", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Feed> feeds;
}
