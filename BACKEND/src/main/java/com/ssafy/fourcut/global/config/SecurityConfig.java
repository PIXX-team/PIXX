package com.ssafy.fourcut.global.config;

import com.ssafy.fourcut.global.security.JwtAuthenticationFilter;
import com.ssafy.fourcut.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // options 요청 인증 없이 통과
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // 1) 탈퇴는 인증 필요
                        .requestMatchers(HttpMethod.DELETE, "/auth/withdraw").authenticated()
                        // 2) 그 외 카카오/로그인/로그아웃 관련은 풀어주고
                        .requestMatchers(
                                "/auth/kakao/callback",
                                "/auth/kakao/login",
                                "/auth/kakao/jwt",
                                "/auth/logout",
                                "/auth/logout/kakao",
                                "/auth/refresh"
                        ).permitAll()
                        // 3) 나머지 요청은 모두 인증 필요
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
