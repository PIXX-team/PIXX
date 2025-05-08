package com.ssafy.fourcut.domain.user.controller;

import com.ssafy.fourcut.domain.user.dto.TokenDto;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ApiResponse<Object>> kakaoLogin(
            @RequestBody Map<String, String> body,
            HttpServletResponse response
    ) {
        String code = body.get("code");
        if (code == null || code.isBlank()) {
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.<Object>builder()
                            .status(HttpStatus.BAD_REQUEST.value())
                            .message("code 파라미터를 입력해 주세요.")
                            .data(null)
                            .build());
        }

        try {
            TokenDto tokens = kakaoAuthService.loginWithKakao(code);

            // 공통 Cookie 빌더
            ResponseCookie accessCookie = ResponseCookie.from("accessToken", tokens.getAccessToken())
                    .domain(cookieProps.getDomain())
                    .path(cookieProps.getPath())
                    .sameSite(cookieProps.getSameSite())
                    .httpOnly(cookieProps.isHttpOnly())
                    .secure(cookieProps.isSecure())
                    .maxAge(jwtTokenProvider.getAccessTokenExpiry() / 1000)
                    .build();
            response.addHeader("Set-Cookie", accessCookie.toString());

            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", tokens.getRefreshToken())
                    .domain(cookieProps.getDomain())
                    .path(cookieProps.getPath())
                    .sameSite(cookieProps.getSameSite())
                    .httpOnly(cookieProps.isHttpOnly())
                    .secure(cookieProps.isSecure())
                    .maxAge(jwtTokenProvider.getRefreshTokenExpiry() / 1000)
                    .build();
            response.addHeader("Set-Cookie", refreshCookie.toString());

            return ResponseEntity.ok(ApiResponse.builder()
                    .status(HttpStatus.OK.value())
                    .message("카카오 로그인 성공")
                    .data(null)
                    .build());

        } catch (HttpClientErrorException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.builder()
                            .status(HttpStatus.UNAUTHORIZED.value())
                            .message("카카오 API 요청 실패: " + ex.getResponseBodyAsString())
                            .data(null)
                            .build()
            );
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("카카오 로그인 중 내부 오류가 발생했습니다.")
                            .data(null)
                            .build()
            );
        }
    }

    // 개발용 JWT 발급
    @PostMapping("/kakao/jwt")
    public ResponseEntity<ApiResponse<TokenDto>> issueJwtByKakaoId(@RequestBody Map<String, Long> body) {
        TokenDto tokens = kakaoAuthService.loginWithKakaoId(body.get("kakao_id"));
        return ResponseEntity.ok(ApiResponse.<TokenDto>builder()
                .status(200)
                .message("카카오 ID 기반 JWT 발급 성공")
                .data(tokens)
                .build()
        );
    }

    // 서비스 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Object>> logout(HttpServletResponse response) {
        // 공통 삭제용 쿠키
        ResponseCookie delAccess = ResponseCookie.from("accessToken", "")
                .domain(cookieProps.getDomain())
                .path(cookieProps.getPath())
                .sameSite(cookieProps.getSameSite())
                .httpOnly(cookieProps.isHttpOnly())
                .secure(cookieProps.isSecure())
                .maxAge(0)
                .build();
        ResponseCookie delRefresh = ResponseCookie.from("refreshToken", "")
                .domain(cookieProps.getDomain())
                .path(cookieProps.getPath())
                .sameSite(cookieProps.getSameSite())
                .httpOnly(cookieProps.isHttpOnly())
                .secure(cookieProps.isSecure())
                .maxAge(0)
                .build();
        response.addHeader("Set-Cookie", delAccess.toString());
        response.addHeader("Set-Cookie", delRefresh.toString());

        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .message("로그아웃이 완료되었습니다.")
                .data(null)
                .build());
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
    public ResponseEntity<ApiResponse<Object>> withdraw(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
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
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.builder()
                            .status(401)
                            .message("유효한 액세스 토큰이 필요합니다.")
                            .data(null)
                            .build()
            );
        }

        Claims claims = jwtTokenProvider.parseToken(token);
        Integer userId = claims.get("user_id", Integer.class);
        kakaoAuthService.withdrawByUserId(userId);

        // 쿠키 삭제
        ResponseCookie delAccess = ResponseCookie.from("accessToken", "")
                .domain(cookieProps.getDomain())
                .path(cookieProps.getPath())
                .sameSite(cookieProps.getSameSite())
                .httpOnly(cookieProps.isHttpOnly())
                .secure(cookieProps.isSecure())
                .maxAge(0)
                .build();
        ResponseCookie delRefresh = ResponseCookie.from("refreshToken", "")
                .domain(cookieProps.getDomain())
                .path(cookieProps.getPath())
                .sameSite(cookieProps.getSameSite())
                .httpOnly(cookieProps.isHttpOnly())
                .secure(cookieProps.isSecure())
                .maxAge(0)
                .build();
        response.addHeader("Set-Cookie", delAccess.toString());
        response.addHeader("Set-Cookie", delRefresh.toString());

        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .message("회원 탈퇴가 완료되었습니다.")
                .data(null)
                .build());
    }

    // 토큰 재발급
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Object>> refresh(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {

        // 1) 리프레시 토큰 유효성 검사
        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.builder()
                            .status(HttpStatus.UNAUTHORIZED.value())
                            .message("유효한 리프레시 토큰이 필요합니다.")
                            .data(null)
                            .build()
            );
        }

        // 2) 기존 리프레시 토큰에서 클레임 파싱
        Claims claims = jwtTokenProvider.parseToken(refreshToken);
        Integer userId    = claims.get("user_id", Integer.class);
        Long    kakaoId   = claims.get("kakao_id", Long.class);
        String  nickname  = claims.get("nickname", String.class);
        String  userEmail = claims.get("user_email", String.class);

        // 3) 전체 클레임 복원하여 새 액세스 토큰 발급
        Map<String, Object> fullClaims = Map.of(
                "user_id",    userId,
                "kakao_id",   kakaoId,
                "nickname",   nickname,
                "user_email", userEmail
        );
        String newAccessToken  = jwtTokenProvider.createAccessToken(fullClaims);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(userId);

        // 4) 쿠키로 세팅 (CookieProperties 사용)
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", newAccessToken)
                .domain(cookieProps.getDomain())
                .path(cookieProps.getPath())
                .sameSite(cookieProps.getSameSite())
                .httpOnly(cookieProps.isHttpOnly())
                .secure(cookieProps.isSecure())
                .maxAge(jwtTokenProvider.getAccessTokenExpiry() / 1000)
                .build();
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", newRefreshToken)
                .domain(cookieProps.getDomain())
                .path(cookieProps.getPath())
                .sameSite(cookieProps.getSameSite())
                .httpOnly(cookieProps.isHttpOnly())
                .secure(cookieProps.isSecure())
                .maxAge(jwtTokenProvider.getRefreshTokenExpiry() / 1000)
                .build();
        response.addHeader("Set-Cookie", accessCookie.toString());
        response.addHeader("Set-Cookie", refreshCookie.toString());

        // 5) 응답
        return ResponseEntity.ok(ApiResponse.builder()
                .status(HttpStatus.OK.value())
                .message("토큰 재발급 성공")
                .data(null)
                .build());
    }
}