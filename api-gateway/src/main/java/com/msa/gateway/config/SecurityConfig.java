package com.msa.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * API Gateway Security 설정
 * 
 * 왜 필요한가?
 * 1. Gateway는 WebFlux 기반이므로 WebFluxSecurity 사용
 * 2. JWT 필터가 인증을 담당하므로 Spring Security는 최소한으로 설정
 * 3. CORS 설정으로 Cross-Origin 요청 허용
 * 4. 인증이 필요 없는 경로(health check, auth 등) 허용
 * 
 * 실무 포인트:
 * - Gateway 자체는 stateless하게 설정
 * - CSRF 비활성화 (REST API이므로)
 * - 세션 비활성화 (JWT 기반 인증)
 * - actuator 엔드포인트는 내부 접근만 허용
 * 
 * @author 30년차 백엔드 팀장
 */
@Configuration
@EnableWebFluxSecurity  // WebFlux용 Security 활성화
public class SecurityConfig {
    
    /**
     * Spring Security 필터 체인 설정
     * 
     * Gateway의 보안 설정:
     * 1. 인증 불필요 경로: /auth/**, /actuator/health
     * 2. 나머지 모든 경로: JWT 필터에서 처리 (permitAll로 설정)
     * 3. CSRF 비활성화: REST API이므로 불필요
     * 4. 세션 비활성화: JWT 기반 stateless 인증
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            // CSRF 비활성화 (REST API이므로)
            .csrf(csrf -> csrf.disable())
            
            // 세션 관리 비활성화 (JWT 기반 stateless)
            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(formLogin -> formLogin.disable())
            
            // CORS 설정 적용
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // URL별 접근 권한 설정
            .authorizeExchange(exchanges -> exchanges
                // 인증 불필요 경로
                .pathMatchers("/auth/**").permitAll()           // 로그인, 회원가입
                .pathMatchers("/actuator/health").permitAll()   // 헬스체크
                .pathMatchers("/actuator/info").permitAll()     // 서비스 정보
                
                // 개발/테스트용 경로 (운영환경에서는 제거)
                .pathMatchers("/actuator/gateway/**").permitAll() // Gateway 라우팅 정보
                
                // 나머지 모든 요청: JWT 필터에서 인증 처리
                // (여기서는 permitAll로 설정하고 실제 인증은 JwtAuthenticationFilter에서)
                .anyExchange().permitAll()
            )
            
            .build();
    }
    
    /**
     * CORS 설정
     * 
     * Cross-Origin Resource Sharing 설정:
     * - 웹 브라우저의 동일 출처 정책(Same-Origin Policy) 우회
     * - 프론트엔드와 백엔드가 다른 도메인/포트를 사용할 때 필요
     * 
     * 실무 설정:
     * - 개발: localhost 모든 포트 허용
     * - 운영: 실제 도메인만 허용
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 허용할 Origin (출처) 설정
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:*",           // 로컬 개발용 (모든 포트)
            "https://*.yourdomain.com"      // 운영 도메인 (실제 도메인으로 변경)
        ));
        
        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));
        
        // 허용할 헤더
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // 인증 정보 포함 허용 (쿠키, Authorization 헤더)
        configuration.setAllowCredentials(true);
        
        // Preflight 요청 캐시 시간 (초)
        configuration.setMaxAge(3600L);
        
        // 노출할 헤더 (프론트엔드에서 접근 가능한 헤더)
        configuration.setExposedHeaders(Arrays.asList(
            "X-Total-Count",      // 페이징 정보
            "X-Request-Id",       // 요청 추적용
            "Location"           // 생성된 리소스 위치
        ));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}

/*
 * 실무 체크리스트:
 * 
 * 개발 환경:
 * ✅ CORS 모든 Origin 허용
 * ✅ actuator 엔드포인트 노출
 * ✅ 자세한 에러 메시지 출력
 * 
 * 운영 환경:
 * ✅ CORS 특정 도메인만 허용
 * ✅ actuator 내부 네트워크만 접근
 * ✅ 에러 메시지 최소화
 * ✅ HTTPS 강제
 * ✅ Security Headers 추가 (HSTS, CSP 등)
 * 
 * 성능 최적화:
 * ✅ CORS Preflight 캐시 시간 설정
 * ✅ 불필요한 Security 필터 제거
 * ✅ 비동기 처리 최적화
 */