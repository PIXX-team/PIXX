package com.ssafy.fourcut.domain.user.service;

import com.ssafy.fourcut.domain.image.entity.Album;
import com.ssafy.fourcut.domain.image.repository.AlbumRepository;
import com.ssafy.fourcut.domain.user.dto.TokenDto;
import com.ssafy.fourcut.domain.user.entity.User;
import com.ssafy.fourcut.domain.user.exception.UserNotFoundException;
import com.ssafy.fourcut.domain.user.repository.UserRepository;
import com.ssafy.fourcut.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class KakaoAuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RestTemplate restTemplate;
    private final AlbumRepository albumRepository;

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String clientSecret;
    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String redirectUri;

    private final String tokenUri    = "https://kauth.kakao.com/oauth/token";
    private final String userInfoUri = "https://kapi.kakao.com/v2/user/me";

    // 카카오 로그인
    public TokenDto loginWithKakao(String code) {

        String kakaoToken = getKakaoAccessToken(code);

        Map<String, Object> kakaoUser = getKakaoUserInfo(kakaoToken);
        Long kakaoId = ((Number) kakaoUser.get("id")).longValue();
        String email    = (String)((Map<?,?>)kakaoUser.get("kakao_account")).get("email");
        String nickname = (String)((Map<?,?>)((Map<?,?>)kakaoUser.get("kakao_account")).get("profile")).get("nickname");

        User user = userRepository.findByKakaoId(kakaoId)
                .orElseGet(() -> {
                    User newUser = userRepository.save(User.builder()
                            .kakaoId(kakaoId)
                            .nickname(nickname)
                            .userEmail(email)
                            .createdAt(LocalDateTime.now())
                            .userToken(0)
                            .build());

                    Album defaultAlbum = Album.builder()
                            .user(newUser)
                            .albumName("기본 앨범")
                            .albumMemo("")
                            .createdAt(LocalDateTime.now())
                            .defaultAlbum(true)
                            .favoriteAlbum(false)
                            .build();
                    albumRepository.save(defaultAlbum);

                    Album favoriteAlbum = Album.builder()
                            .user(newUser)
                            .albumName("즐겨찾기")
                            .albumMemo("")
                            .createdAt(LocalDateTime.now())
                            .defaultAlbum(false)
                            .favoriteAlbum(true)
                            .build();
                    albumRepository.save(favoriteAlbum);

                    return newUser;
                });

        // JWT 발급
        Map<String, Object> claims = Map.of(
                "user_id",    user.getUserId(),
                "kakao_id",   user.getKakaoId(),
                "nickname",   user.getNickname(),
                "user_email", user.getUserEmail()
        );
        String accessToken  = jwtTokenProvider.createAccessToken(claims);
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getUserId());

        return new TokenDto(accessToken, refreshToken);
    }

    // 카카오 access 토큰 요청
    private String getKakaoAccessToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String,String> params = new LinkedMultiValueMap<>();
        params.add("grant_type",   "authorization_code");
        params.add("client_id",    clientId);
        params.add("client_secret",clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("code",         code);

        HttpEntity<MultiValueMap<String,String>> req = new HttpEntity<>(params, headers);
        ResponseEntity<Map> resp = restTemplate.exchange(tokenUri, HttpMethod.POST, req, Map.class);

        Map<String,Object> body = resp.getBody();
        if (body == null || body.get("access_token") == null) {
            throw new RuntimeException("카카오 토큰 요청 실패");
        }
        return (String) body.get("access_token");
    }

    // 카카오 유저 정보 받아오기
    private Map<String,Object> getKakaoUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> resp = restTemplate.exchange(userInfoUri, HttpMethod.GET, entity, Map.class);

        Map<String,Object> body = resp.getBody();
        if (body == null || body.get("id") == null) {
            throw new RuntimeException("카카오 사용자 정보 조회 실패");
        }
        return body;
    }

    // 카카오 id 로그인
    public TokenDto loginWithKakaoId(Long kakaoId) {
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new UserNotFoundException(kakaoId));

        Map<String,Object> claims = Map.of(
                "user_id",    user.getUserId(),
                "kakao_id",   user.getKakaoId(),
                "nickname",   user.getNickname(),
                "user_email", user.getUserEmail()
        );
        String accessToken  = jwtTokenProvider.createAccessToken(claims);
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getUserId());
        return new TokenDto(accessToken, refreshToken);
    }

    // 회원 탈퇴
    public void withdrawByUserId(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        userRepository.delete(user);
    }

}
