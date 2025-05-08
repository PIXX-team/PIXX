package com.ssafy.fourcut.global.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access.expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh.expiration}")
    private long refreshTokenExpiration;

    private Key signingKey;

    @PostConstruct
    protected void init() {
        secretKey = secretKey.trim();
        byte[] keyBytes;
        try {
            // 1) 프로퍼티에 Base64로 넣었으면 디코딩
            keyBytes = Decoders.BASE64.decode(secretKey);
        } catch (IllegalArgumentException e) {
            // 2) 평문(utf-8)일 경우
            keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        }
        this.signingKey  = Keys.hmacShaKeyFor(keyBytes);
    }


    // 🔹 토큰 안의 클레임 추출
    public Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .setAllowedClockSkewSeconds(180)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            return null;
        }
    }

    // 🔹 토큰 유효성 검사
    public boolean validateToken(String token) {
        return parseToken(token) != null;
    }

    public String createAccessToken(Map<String, Object> claims) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(Integer userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenExpiration);

        return Jwts.builder()
                .claim("user_id", userId)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // httponly 적용 시 필요
    public long getAccessTokenExpiry() {
        return accessTokenExpiration;
    }
    public long getRefreshTokenExpiry() {
        return refreshTokenExpiration;
    }


}
