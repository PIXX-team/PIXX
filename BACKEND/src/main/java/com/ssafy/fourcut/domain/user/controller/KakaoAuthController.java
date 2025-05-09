package com.ssafy.fourcut.domain.user.controller;

import com.ssafy.fourcut.domain.user.dto.TokenDto;
import com.ssafy.fourcut.domain.user.exception.CustomExceptions.*;
import com.ssafy.fourcut.domain.user.service.KakaoAuthService;
import com.ssafy.fourcut.global.config.CookieProperties;
import com.ssafy.fourcut.global.dto.ApiResponse;
import com.ssafy.fourcut.global.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class KakaoAuthController {

    private final KakaoAuthService kakaoAuthService;
    private final JwtTokenProvider jwtTokenProvider;
    private final CookieProperties cookieProps;

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String kakaoClientId;

    @Value("${kakao.logout-redirect-uri}")
    private String kakaoLogoutRedirectUri;

     // 카카오 로그인
    @PostMapping("/kakao/login")
    public ApiResponse<Void> kakaoLogin(
            @RequestBody Map<String, String> body,
            HttpServletResponse response
    ) {
        String code = body.get("code");
        if (code == null || code.isBlank()) {
            throw new MissingCodeException();
        }

        TokenDto tokens;
        try {
            tokens = kakaoAuthService.loginWithKakao(code);
        } catch (HttpClientErrorException ex) {
            throw new KakaoApiException(ex.getResponseBodyAsString());
        }

        // 쿠키 세팅
        addCookie(response, "accessToken", tokens.getAccessToken(), jwtTokenProvider.getAccessTokenExpiry() / 1000);
        addCookie(response, "refreshToken", tokens.getRefreshToken(), jwtTokenProvider.getRefreshTokenExpiry() / 1000);

        return ApiResponse.<Void>builder()
                .status(200)
                .message("카카오 로그인 성공")
                .data(null)
                .build();
    }


    // 개발용 JWT 발급
    @PostMapping("/kakao/jwt")
    public ApiResponse<TokenDto> issueJwtByKakaoId(@RequestBody Map<String, Long> body) {
        TokenDto tokens = kakaoAuthService.loginWithKakaoId(body.get("kakao_id"));
        return ApiResponse.<TokenDto>builder()
                .status(200)
                .message("카카오 ID 기반 JWT 발급 성공")
                .data(tokens)
                .build();
    }


    //서비스 로그아웃
    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletResponse response) {
        // 쿠키 삭제
        addCookie(response, "accessToken", "", 0);
        addCookie(response, "refreshToken", "", 0);

        return ApiResponse.<Void>builder()
                .status(200)
                .message("로그아웃이 완료되었습니다.")
                .data(null)
                .build();
    }


     // 카카오 로그아웃 (Redirect)
    @GetMapping("/logout/kakao")
    public void kakaoLogout(HttpServletResponse response) throws IOException {
        String url = "https://kauth.kakao.com/oauth/logout"
                + "?client_id=" + kakaoClientId
                + "&logout_redirect_uri=" + kakaoLogoutRedirectUri;
        response.sendRedirect(url);
    }

    // 회원 탈퇴
    @DeleteMapping("/withdraw")
    public ApiResponse<Void> withdraw(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        // 토큰 추출
        String token = null;
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if ("accessToken".equals(c.getName())) {
                    token = c.getValue();
                    break;
                }
            }
        }
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            throw new JwtInvalidException();
        }

        Claims claims = jwtTokenProvider.parseToken(token);
        Integer userId = claims.get("user_id", Integer.class);
        kakaoAuthService.withdrawByUserId(userId);

        // 쿠키 삭제
        addCookie(response, "accessToken", "", 0);
        addCookie(response, "refreshToken", "", 0);

        return ApiResponse.<Void>builder()
                .status(200)
                .message("회원 탈퇴가 완료되었습니다.")
                .data(null)
                .build();
    }

    // 토큰 재발급
    @PostMapping("/refresh")
    public ApiResponse<Void> refresh(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {
        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
            throw new JwtInvalidException();
        }

        Claims claims = jwtTokenProvider.parseToken(refreshToken);
        Integer userId   = claims.get("user_id", Integer.class);
        Long    kakaoId  = claims.get("kakao_id", Long.class);
        String  nickname = claims.get("nickname", String.class);
        String  email    = claims.get("user_email", String.class);

        // 새 토큰 발급
        Map<String,Object> fullClaims = Map.of(
                "user_id",    userId,
                "kakao_id",   kakaoId,
                "nickname",   nickname,
                "user_email", email
        );
        String newAccessToken  = jwtTokenProvider.createAccessToken(fullClaims);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(userId);

        // 쿠키 세팅
        addCookie(response, "accessToken", newAccessToken, jwtTokenProvider.getAccessTokenExpiry() / 1000);
        addCookie(response, "refreshToken", newRefreshToken, jwtTokenProvider.getRefreshTokenExpiry() / 1000);

        return ApiResponse.<Void>builder()
                .status(200)
                .message("토큰 재발급 성공")
                .data(null)
                .build();
    }

    // 공통 Cookie 세팅 유틸
    private void addCookie(HttpServletResponse response, String name, String value, long maxAge) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .domain(cookieProps.getDomain())
                .path(cookieProps.getPath())
                .sameSite(cookieProps.getSameSite())
                .httpOnly(cookieProps.isHttpOnly())
                .secure(cookieProps.isSecure())
                .maxAge(maxAge)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }
}
