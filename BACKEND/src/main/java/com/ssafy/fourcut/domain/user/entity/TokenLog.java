package com.ssafy.fourcut.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "token_log")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class TokenLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_log_id")
    private Integer tokenLogId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "token_history", nullable = false, length = 100)
    private String tokenHistory;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
