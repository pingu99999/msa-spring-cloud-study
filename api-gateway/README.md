# 🚪 API Gateway - MSA의 단일 진입점

## 📌 이 서비스가 하는 일
MSA의 "정문 경비원" 역할을 합니다.
- 모든 외부 요청의 **단일 진입점**
- **JWT 토큰 검증** 후 내부 서비스로 라우팅
- **CORS, 로깅, 모니터링** 등 횡단 관심사 처리
- **서비스 디스커버리**를 통한 동적 라우팅

## 🎯 요청 흐름

```
[Client] 
   ↓ (JWT Token)
[API Gateway:8080] 
   ↓ (Token 검증)
[Eureka Server] 
   ↓ (서비스 조회)
[Target Service] (auth/user/core)
```

## 🚀 실행 방법

### 전제 조건
1. **Eureka Server 실행** (discovery-service)
2. **Java 17** 설치
3. **Maven** 설치

### 1. Maven으로 실행
```bash
# api-gateway 디렉토리에서
mvn spring-boot:run
```

### 2. JAR 파일로 실행
```bash
# 빌드
mvn clean package

# 실행
java -jar target/api-gateway-1.0.0.jar
```

## 🌐 접속 정보
- **Gateway 주소**: http://localhost:8080
- **Eureka 등록**: discovery-service에서 확인 가능
- **Health Check**: http://localhost:8080/actuator/health

## 📡 라우팅 규칙

| URL 패턴 | 대상 서비스 | JWT 검증 | 예시 |
|----------|-------------|----------|------|
| `/auth/**` | auth-service | ❌ 불필요 | `POST /auth/login` |
| `/api/users/**` | user-service | ✅ 필수 | `GET /api/users/profile` |
| `/api/core/**` | core-service | ✅ 필수 | `GET /api/core/products` |
| `/api/**` | core-service | ✅ 필수 | `GET /api/orders` |

## 🔐 JWT 인증 흐름

### 1. 로그인 (인증 불필요)
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "user", "password": "pass"}'
```

### 2. 인증 필요 API 호출
```bash
curl -X GET http://localhost:8080/api/users/profile \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 3. Gateway의 JWT 처리
1. **토큰 추출**: `Authorization: Bearer {token}` 헤더에서
2. **토큰 검증**: 서명, 만료시간 확인
3. **헤더 추가**: 내부 서비스로 전달 시
   ```
   X-User-Id: 12345
   X-User-Role: USER  
   X-User-Name: johndoe
   X-Auth-Type: JWT
   ```

## 📁 프로젝트 구조
```
api-gateway/
├── pom.xml                                    # Maven 설정
├── src/main/java/com/msa/gateway/
│   ├── ApiGatewayApplication.java            # 메인 클래스
│   ├── config/
│   │   └── SecurityConfig.java               # Spring Security 설정
│   └── filter/
│       └── JwtAuthenticationFilter.java      # JWT 인증 필터
└── src/main/resources/
    └── application.yml                        # 설정 파일
```

## ⚙️ 핵심 설정 설명

### 1. 라우팅 설정 (application.yml)
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: auth-service
          uri: lb://auth-service    # 로드밸런서 + 서비스명
          predicates:
            - Path=/auth/**         # 조건: /auth/로 시작
          filters:
            - StripPrefix=1         # /auth 제거 후 전달
```

### 2. JWT 검증 필터
- **위치**: `JwtAuthenticationFilter.java`
- **역할**: 토큰 검증 후 사용자 정보 헤더 추가
- **적용**: `/api/**` 경로에만

### 3. CORS 설정
- **개발**: `localhost:*` 모든 포트 허용
- **운영**: 실제 도메인만 허용

## 🚨 자주 발생하는 문제

### 1. "서비스를 찾을 수 없음" 오류
```
No instances available for auth-service
```
**해결**: 
- Eureka Server 실행 확인
- 대상 서비스(auth-service)가 Eureka에 등록되었는지 확인

### 2. JWT 토큰 검증 실패
```
{"error": "Unauthorized", "message": "유효하지 않은 토큰입니다"}
```
**해결**:
- 토큰 만료 확인
- `jwt.secret` 설정이 auth-service와 동일한지 확인

### 3. CORS 에러 (브라우저)
```
Access to fetch at 'http://localhost:8080/api/users' from origin 'http://localhost:3000' has been blocked by CORS policy
```
**해결**:
- `application.yml`의 CORS 설정 확인
- 프론트엔드 Origin이 허용 목록에 있는지 확인

## 🔍 모니터링 & 디버깅

### 1. Gateway 라우팅 상태 확인
```bash
curl http://localhost:8080/actuator/gateway/routes
```

### 2. 로그 레벨 설정
```yaml
logging:
  level:
    org.springframework.cloud.gateway: DEBUG  # 라우팅 상세 로그
    com.msa.gateway: DEBUG                   # 우리 코드 로그
```

### 3. 요청 추적
모든 요청에는 `X-Request-Id`가 자동 추가되어 로그에서 추적 가능

## 📝 실무 체크리스트

### 개발 환경
✅ 자세한 에러 메시지 출력  
✅ CORS 모든 Origin 허용  
✅ Gateway 라우팅 정보 노출  
✅ 디버그 로그 활성화  

### 운영 환경
✅ 에러 메시지 최소화  
✅ CORS 특정 도메인만 허용  
✅ HTTPS 강제 설정  
✅ JWT Secret 환경변수 관리  
✅ Rate Limiting 추가  
✅ Circuit Breaker 패턴 적용  

## 🔗 다음 단계
1. **Auth Service** 구현 (JWT 발급)
2. **User Service** 구현 (사용자 관리)
3. **Core Service** 구현 (비즈니스 로직)
4. **전체 서비스 통합 테스트**

## 💡 팀장의 한마디
"Gateway는 MSA의 관문입니다. 여기가 다운되면 전체 시스템 마비에요.  
반드시 이중화하고, Circuit Breaker로 장애 전파를 막으세요.  
그리고 JWT 검증 로직은 auth-service와 100% 동일해야 합니다!"

---
작성: 30년차 백엔드 팀장