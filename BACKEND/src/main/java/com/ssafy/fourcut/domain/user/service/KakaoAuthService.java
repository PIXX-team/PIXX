package com.ssafy.fourcut.domain.user.service;

import com.ssafy.fourcut.domain.user.dto.TokenDto;
import com.ssafy.fourcut.domain.user.entity.User;
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

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String clientSecret;
    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String redirectUri;

    private final String tokenUri    = "https://kauth.kakao.com/oauth/token";
    private final String userInfoUri = "https://kapi.kakao.com/v2/user/me";

    public TokenDto loginWithKakao(String code) {
        // 1) 카카오 토큰 교환
        String kakaoToken = getKakaoAccessToken(code);
        // 2) 유저 정보 조회
        Map<String, Object> kakaoUser = getKakaoUserInfo(kakaoToken);

        Long kakaoId = ((Number) kakaoUser.get("id")).longValue();
        Map<String, Object> kakaoAccount = (Map<String, Object>) kakaoUser.get("kakao_account");
        String email    = (String) kakaoAccount.get("email");
        String nickname = (String) ((Map<String,Object>)kakaoAccount.get("profile")).get("nickname");

        // 3) 회원 조회/등록
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseGet(() -> userRepository.save(User.builder()
                        .kakaoId(kakaoId)
                        .nickname(nickname)
                        .userEmail(email)
                        .createdAt(LocalDateTime.now())
                        .userToken(0)
                        .build()));

        // 4) JWT 발급
        Map<String, Object> claims = Map.of(
                "user_id",    user.getUserId(),
                "kakao_id",   user.getKakaoId(),
                "nickname",   user.getNickname(),
                "user_email", user.getUserEmail()
        );
        String accessToken  = jwtTokenProvider.createAccessToken(claims);
        String refreshToken = jwtTokenProvider.createRefreshToken();

        return new TokenDto(accessToken, refreshToken);
    }

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

    public TokenDto loginWithKakaoId(Long kakaoId) {
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new RuntimeException("등록된 사용자가 없습니다: kakaoId=" + kakaoId));

        Map<String,Object> claims = Map.of(
                "user_id",    user.getUserId(),
                "kakao_id",   user.getKakaoId(),
                "nickname",   user.getNickname(),
                "user_email", user.getUserEmail()
        );
        String accessToken  = jwtTokenProvider.createAccessToken(claims);
        String refreshToken = jwtTokenProvider.createRefreshToken();
        return new TokenDto(accessToken, refreshToken);
    }


}
