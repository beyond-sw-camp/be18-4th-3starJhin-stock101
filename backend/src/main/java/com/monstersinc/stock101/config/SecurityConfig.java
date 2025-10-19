package com.monstersinc.stock101.config;

import com.monstersinc.stock101.auth.jwt.JwtAuthenticationFilter;
import com.monstersinc.stock101.exception.handler.CustomAuthenticationEntryPoint;
import io.micrometer.core.instrument.binder.logging.LogbackMetrics;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, LogbackMetrics logbackMetrics) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(customizer ->
                        customizer.configurationSource(getCorsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        // 1) 로그인 관련
                        .requestMatchers("/api/v1/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/logout").authenticated()

                        // 2) 사용자 정보 관련
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/me").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/users/me").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/users/me").authenticated()

                        // 2) 게시물, 좋아요, 댓글 등록 및 내가 작성한 게시물 조회; 로그인 필요
                        .requestMatchers(HttpMethod.POST, "/api/v1/board/posts").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/board/posts/{postId}/like").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/board/posts/{postId}/comments").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/board/me").authenticated()
                        // 나머지 요청은 일단 모두 허용.
                        .anyRequest().permitAll()
                )
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(new CustomAuthenticationEntryPoint()))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(logout -> logout.disable());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }


    private static CorsConfigurationSource getCorsConfigurationSource() {

        return (request) -> {
            CorsConfiguration corsConfiguration = new CorsConfiguration();
            
            corsConfiguration.setAllowedOriginPatterns(List.of(
                "http://frontend.beyond.com",
                "http://localhost:5173",
                "http://localhost:5174"
            ));

            // 허용할 메소드
            corsConfiguration.setAllowedMethods(Arrays.asList(
                    "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
            ));

            // 요청 헤더
            corsConfiguration.setAllowedHeaders(Arrays.asList(
                    "Authorization", "Content-Type"
            ));

            // 응답 헤더
            corsConfiguration.setExposedHeaders(List.of("Authorization"));

            // 쿠키 / 세션 허용
            corsConfiguration.setAllowCredentials(true);

            // preflight 캐시 유지 시간
            corsConfiguration.setMaxAge(3600L);

            return corsConfiguration;
        };
    }

}
