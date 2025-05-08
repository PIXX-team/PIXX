package com.ssafy.fourcut.domain.user.entity;

import com.ssafy.fourcut.domain.image.entity.Album;
import com.ssafy.fourcut.domain.image.entity.Feed;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DialectOverride;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "user")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "kakao_id", nullable = false)
    private Long kakaoId;

    @Column(name = "nickname", nullable = false, length = 45)
    private String nickname;

    @Column(name = "user_email", nullable = false, length = 100)
    private String userEmail;

    @Column(name = "created_at", nullable = false, updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "user_token", nullable = false, columnDefinition = "INT DEFAULT 3")
    private Integer userToken;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TokenLog> tokenLogs;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Feed> feeds;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Album> albums;
}
