package com.msa.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * API Gateway 메인 애플리케이션
 * 
 * 역할:
 * 1. 모든 외부 요청의 단일 진입점 (포트 8080)
 * 2. JWT 토큰 검증 (인증된 요청만 내부 서비스로 전달)
 * 3. 서비스별 라우팅 (URL 패턴에 따라 적절한 서비스로 전달)
 * 4. 횡단 관심사 처리 (CORS, 로깅, Rate Limiting)
 * 
 * 요청 흐름:
 * Client → Gateway(8080) → Eureka 조회 → 해당 Service
 * 
 * 예시:
 * - POST /auth/login → auth-service로 라우팅
 * - GET /api/users/profile → user-service로 라우팅 (JWT 검증 후)
 * - GET /api/products → core-service로 라우팅 (JWT 검증 후)
 * 
 * @author 30년차 백엔드 팀장
 */
@SpringBootApplication
@EnableDiscoveryClient  // Eureka 클라이언트로 동작 (자신을 Eureka에 등록)
public class ApiGatewayApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
        
        System.out.println("========================================");
        System.out.println("🚪 API Gateway 시작 완료!");
        System.out.println("📍 접속 주소: http://localhost:8080");
        System.out.println("🔍 Eureka 등록됨: discovery-service");
        System.out.println("🛡️ JWT 검증 활성화됨");
        System.out.println("========================================");
    }
}