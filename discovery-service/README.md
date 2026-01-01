# 🎯 Discovery Service (Eureka Server)

## 📌 이 서비스가 하는 일
MSA에서 "전화번호부" 역할을 합니다.
- 각 마이크로서비스가 시작될 때 자신을 등록
- 다른 서비스를 찾을 때 Eureka에게 물어봄
- 서비스 Health Check (살아있는지 확인)

## 🚀 실행 방법

### 1. Maven으로 실행
```bash
# 프로젝트 루트에서
mvn spring-boot:run
```

### 2. JAR 파일로 실행
```bash
# 빌드
mvn clean package

# 실행
java -jar target/discovery-service-1.0.0.jar
```

## 🌐 접속 정보
- **Eureka 대시보드**: http://localhost:8761
- **로그인 정보**
  - ID: `eureka`
  - Password: `eureka123`

## 📁 프로젝트 구조
```
discovery-service/
├── pom.xml                          # Maven 설정 (의존성 관리)
├── src/main/java/
│   └── com/msa/discovery/
│       ├── DiscoveryServiceApplication.java  # 메인 클래스
│       └── config/
│           └── SecurityConfig.java          # 보안 설정
└── src/main/resources/
    └── application.yml              # 애플리케이션 설정
```

## 🔍 핵심 개념 설명

### Eureka란?
Netflix에서 만든 서비스 디스커버리 도구입니다.

**비유로 이해하기:**
- 전화번호부 = Eureka Server
- 가게 = 각 마이크로서비스
- 가게가 오픈하면 → 전화번호부에 등록
- 손님이 가게 찾을 때 → 전화번호부 확인

### 왜 필요한가?
1. **동적 서비스 관리**: 서비스가 추가/삭제될 때 자동 관리
2. **로드 밸런싱**: 같은 서비스가 여러 개 있을 때 분산
3. **장애 대응**: 죽은 서비스 자동 제거

## ⚙️ 주요 설정 설명

### application.yml
```yaml
eureka:
  client:
    register-with-eureka: false  # 자기 자신은 등록 안함
    fetch-registry: false        # 다른 서비스 목록 안 가져옴
```

**왜 이렇게 설정?**
- Eureka Server는 관리자 역할이므로 자신을 서비스로 등록할 필요 없음

### Self-Preservation Mode
```yaml
eureka:
  server:
    enable-self-preservation: false  # 개발환경용
```

**무엇인가?**
- 네트워크 장애 시 서비스를 바로 제거하지 않는 보호 모드
- 개발: false (빠른 제거)
- 운영: true (안정성 우선)

## 🚨 자주 발생하는 문제

### 1. 서비스가 Eureka에 안 보일 때
- **원인**: 등록까지 30초 정도 걸림
- **해결**: 잠시 기다린 후 새로고침

### 2. "EMERGENCY! EUREKA MAY BE INCORRECTLY..." 메시지
- **원인**: Self-preservation 모드 활성화
- **해결**: 개발환경에서는 무시해도 됨

### 3. 대시보드 접속이 안 될 때
- **원인**: Security 설정
- **해결**: ID/PW 확인 (eureka/eureka123)

## 📝 실무 체크리스트

### 개발 환경
✅ Self-preservation 비활성화
✅ 짧은 eviction 타이머 (5초)
✅ 단일 인스턴스 실행

### 운영 환경
✅ Self-preservation 활성화
✅ 긴 eviction 타이머 (60초)
✅ 클러스터 구성 (최소 2대)
✅ 환경변수로 비밀번호 관리
✅ HTTPS 적용

## 🔗 다음 단계
1. API Gateway 구축
2. Auth Service 구현
3. 각 서비스를 Eureka에 등록

## 💡 팀장의 한마디
"Eureka Server는 MSA의 심장입니다. 이게 죽으면 서비스들이 서로를 못 찾아요. 
운영환경에서는 반드시 이중화하고, 모니터링 꼭 걸어두세요!"

---
작성: 30년차 백엔드 팀장