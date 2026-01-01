# 🚀 MSA 실무 프로젝트 - Spring Cloud 기반 마이크로서비스 아키텍처

## 📌 프로젝트 개요
30년 경력 백엔드 팀장의 시각으로 구축한 실무 수준의 MSA 아키텍처입니다.
학습용이지만 실제 프로덕션에서 사용 가능한 구조로 설계되었습니다.

## 🏗️ 전체 아키텍처

```
[Client] → [API Gateway:8080] → [Eureka:8761] → [Services]
                                       ↓
                            [Auth:8081] [User:8082] [Core:8083]
```

## 📦 서비스 구성

| 서비스 | 포트 | 역할 | 상태 |
|--------|------|------|------|
| discovery-service | 8761 | 서비스 레지스트리 (Eureka) | ✅ 완료 |
| api-gateway | 8080 | API 라우팅 & 인증 | 🚧 진행중 |
| auth-service | 8081 | 인증/인가 (JWT) | ⏳ 예정 |
| user-service | 8082 | 사용자 관리 | ⏳ 예정 |
| core-service | 8083 | 비즈니스 로직 | ⏳ 예정 |

## 🛠️ 기술 스택

### Backend
- Java 17
- Spring Boot 3.x
- Spring Cloud (Gateway, Eureka, OpenFeign)
- Spring Security + JWT

### Database
- MySQL/PostgreSQL (서비스별 독립 DB)

### Infrastructure
- Docker & Docker Compose
- AWS EC2

## 🚀 실행 방법

### 1. Eureka Server 실행
```bash
cd discovery-service
mvn spring-boot:run
# http://localhost:8761 (eureka/eureka123)
```

### 2. 전체 서비스 실행 (Docker Compose) - 예정
```bash
docker-compose up -d
```

## 📝 개발 진행 상황

- [x] 0단계: MSA 전체 아키텍처 설계
- [x] 1단계: Discovery Service (Eureka Server) 구축
- [ ] 2단계: API Gateway 구성
- [ ] 3단계: Auth Service 구현
- [ ] 4단계: User Service 구현
- [ ] 5단계: 서비스 간 인증 연동
- [ ] 6단계: Docker Compose 설정
- [ ] 7단계: AWS EC2 배포

## 💡 핵심 설계 원칙

1. **DB 격리**: 서비스별 독립 DB (DB-per-service)
2. **인증 중앙화**: Gateway에서 JWT 검증
3. **서비스 자율성**: 각 서비스 독립 배포 가능
4. **장애 격리**: 한 서비스 장애가 전체에 영향 없음

## 🔍 실무 체크포인트

### 개발 환경
- ✅ Eureka self-preservation 비활성화
- ✅ 로컬 개발용 설정 분리
- ✅ 디버깅 로그 활성화

### 운영 환경
- ⬜ Eureka 클러스터링
- ⬜ 환경변수로 민감정보 관리
- ⬜ HTTPS 적용
- ⬜ 모니터링 & 알림

## 👨‍💼 팀장의 조언

> "MSA는 은탄환이 아닙니다. 팀 규모가 작고 서비스가 단순하다면 모놀리스가 나을 수 있습니다.
> 하지만 독립적인 배포와 확장성이 중요하다면 MSA는 최고의 선택입니다."

## 📚 참고 자료

- [Spring Cloud 공식 문서](https://spring.io/projects/spring-cloud)
- [Microservices Patterns](https://microservices.io/patterns/index.html)

---
**작성자**: 30년차 백엔드 팀장  
**목적**: MSA 실무 학습 프로젝트