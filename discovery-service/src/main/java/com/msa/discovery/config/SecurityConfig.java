package com.msa.discovery.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Eureka Server 보안 설정
 * 
 * 왜 필요한가?
 * - Eureka 대시보드는 민감한 정보(서비스 IP, 포트 등)를 노출
 * - 무단 접근을 막기 위해 Basic Auth 적용
 * - 서비스 등록/해제를 아무나 할 수 없도록 보호
 * 
 * @author 팀장
 */
@Configuration           // Spring 설정 클래스임을 선언
@EnableWebSecurity      // Spring Security 활성화
public class SecurityConfig {
    
    /**
     * Spring Security 필터 체인 설정
     * 
     * 실무 포인트:
     * 1. Eureka 클라이언트(다른 서비스들)는 Basic Auth로 인증
     * 2. Health Check는 인증 없이 접근 가능 (모니터링용)
     * 3. CSRF는 Eureka API에만 비활성화 (REST API이므로)
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF(Cross-Site Request Forgery) 보호 설정
            .csrf(csrf -> csrf
                // /eureka/** 경로는 CSRF 보호 제외
                // 왜? Eureka 클라이언트가 REST API로 통신하기 때문
                .ignoringRequestMatchers("/eureka/**")
            )
            
            // URL별 접근 권한 설정
            .authorizeHttpRequests(auth -> auth
                // /eureka/** : 인증 필요 (서비스 등록/조회)
                .requestMatchers("/eureka/**").authenticated()
                
                // /actuator/health : 인증 불필요 (헬스체크용)
                // K8s나 로드밸런서가 주기적으로 체크하므로 열어둠
                .requestMatchers("/actuator/health").permitAll()
                
                // 그 외 모든 요청: 인증 필요 (대시보드 접근 등)
                .anyRequest().authenticated()
            )
            
            // Basic Authentication 사용
            // application.yml의 spring.security.user.name/password 사용
            .httpBasic(Customizer.withDefaults());
        
        return http.build();
    }
    
    /*
     * 실무 체크리스트:
     * ✅ Basic Auth ID/PW는 환경변수로 관리
     * ✅ 운영환경에서는 더 강력한 인증 고려 (OAuth2, mTLS)
     * ✅ Eureka 간 통신(클러스터)도 인증 설정 필요
     * ✅ 네트워크 레벨 보안도 추가 (Security Group, VPC)
     */
}