package com.ssafy.fourcut.global.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String token = resolveToken(request);

        if (token != null) {
            if (jwtTokenProvider.validateToken(token)) {
                Claims claims = jwtTokenProvider.parseToken(token);

                Integer userId = claims.get("user_id", Integer.class);
                String nickname = claims.get("nickname", String.class);

                User principal = new org.springframework.security.core.userdetails.User(
                        String.valueOf(userId),
                        "",
                        java.util.Collections.emptyList()
                );

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        principal, token, principal.getAuthorities()
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않은 JWT 토큰입니다.");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        String body = String.format(message);
        response.getWriter().write(body);
    }

    private String resolveToken(HttpServletRequest request) {
        // 1) 쿠키에 담긴 accessToken 먼저 꺼냄
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        // 2) 헤더로도 지원 (예전 방식, 필요시 사용)
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}