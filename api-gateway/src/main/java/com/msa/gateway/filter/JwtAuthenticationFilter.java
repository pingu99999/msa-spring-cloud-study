package com.msa.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * JWT 인증 필터
 * 
 * 역할:
 * 1. Authorization 헤더에서 JWT 토큰 추출
 * 2. 토큰 유효성 검증 (서명, 만료시간)
 * 3. 유효한 경우: 사용자 정보를 헤더에 추가하고 요청 전달
 * 4. 유효하지 않은 경우: 401 Unauthorized 응답
 * 
 * 흐름:
 * 1. 클라이언트 요청 → Gateway
 * 2. JWT 토큰 검증
 * 3. 검증 성공 → X-User-Id, X-User-Role 헤더 추가 → 내부 서비스 전달
 * 4. 검증 실패 → 401 응답
 * 
 * 실무 포인트:
 * - Gateway에서만 JWT 검증 (내부 서비스는 헤더 정보만 사용)
 * - Public Key 방식 사용 시 auth-service에서 키 관리
 * - Redis를 통한 토큰 블랙리스트 관리 고려
 * 
 * @author 30년차 백엔드 팀장
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    public JwtAuthenticationFilter() {
        super(Config.class);
    }
    
    /**
     * JWT 인증 필터 로직
     */
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            
            // 1. Authorization 헤더 확인
            String authHeader = request.getHeaders().getFirst("Authorization");
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("인증 토큰이 없습니다. Path: {}", request.getPath().value());
                return onError(exchange, "인증 토큰이 필요합니다", HttpStatus.UNAUTHORIZED);
            }
            
            // 2. JWT 토큰 추출 ("Bearer " 제거)
            String token = authHeader.substring(7);
            
            try {
                // 3. JWT 토큰 검증
                Claims claims = validateToken(token);
                
                // 4. 사용자 정보 추출
                String userId = claims.getSubject();           // 사용자 ID
                String userRole = claims.get("role", String.class); // 사용자 역할
                String username = claims.get("username", String.class); // 사용자명
                
                log.debug("JWT 검증 성공 - UserId: {}, Role: {}, Username: {}", 
                         userId, userRole, username);
                
                // 5. 내부 서비스 전달용 헤더 추가
                ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Id", userId)           // 사용자 ID
                    .header("X-User-Role", userRole)       // 사용자 역할
                    .header("X-User-Name", username)       // 사용자명
                    .header("X-Auth-Type", "JWT")          // 인증 타입
                    .build();
                
                // 6. 수정된 요청으로 다음 필터 체인 실행
                return chain.filter(exchange.mutate().request(mutatedRequest).build());
                
            } catch (JwtException e) {
                log.error("JWT 토큰 검증 실패: {}", e.getMessage());
                return onError(exchange, "유효하지 않은 토큰입니다", HttpStatus.UNAUTHORIZED);
            } catch (Exception e) {
                log.error("JWT 처리 중 오류 발생: {}", e.getMessage(), e);
                return onError(exchange, "인증 처리 중 오류가 발생했습니다", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        };
    }
    
    /**
     * JWT 토큰 유효성 검증
     * 
     * @param token JWT 토큰
     * @return Claims 토큰에서 추출한 사용자 정보
     * @throws JwtException 토큰이 유효하지 않은 경우
     */
    private Claims validateToken(String token) {
        // Secret Key 생성 (실제로는 auth-service와 동일한 키 사용)
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        
        return Jwts.parser()
            .verifyWith(key)          // 서명 검증
            .build()
            .parseSignedClaims(token) // 토큰 파싱 및 검증
            .getPayload();
    }
    
    /**
     * 에러 응답 생성
     * 
     * @param exchange ServerWebExchange
     * @param message 에러 메시지
     * @param httpStatus HTTP 상태 코드
     * @return Mono<Void>
     */
    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        response.getHeaders().add("Content-Type", "application/json");
        
        // JSON 형태의 에러 응답 생성
        String errorResponse = String.format(
            "{\"error\": \"%s\", \"message\": \"%s\", \"timestamp\": \"%s\"}",
            httpStatus.getReasonPhrase(),
            message,
            java.time.Instant.now()
        );
        
        DataBuffer buffer = response.bufferFactory().wrap(errorResponse.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
    
    /**
     * 필터 설정 클래스
     * 필요 시 추가 설정을 위한 클래스 (현재는 빈 클래스)
     */
    public static class Config {
        // 필터 설정이 필요한 경우 여기에 추가
        // 예: private boolean enableLogging;
    }
}

/*
 * 실무 활용 팁:
 * 
 * 1. Public/Private Key 방식:
 *    - auth-service: Private Key로 토큰 생성
 *    - gateway: Public Key로 토큰 검증
 *    - 보안성 향상, auth-service 재시작 시에도 토큰 유효
 * 
 * 2. 토큰 블랙리스트:
 *    - Redis에 로그아웃된 토큰 저장
 *    - 토큰 검증 시 블랙리스트 확인
 * 
 * 3. 토큰 갱신:
 *    - Access Token 만료 시 Refresh Token으로 재발급
 *    - 401 응답 시 클라이언트가 자동으로 토큰 갱신
 * 
 * 4. 성능 최적화:
 *    - JWT 파싱 결과 캐시 (같은 토큰 반복 검증 방지)
 *    - 비동기 처리로 성능 향상
 */