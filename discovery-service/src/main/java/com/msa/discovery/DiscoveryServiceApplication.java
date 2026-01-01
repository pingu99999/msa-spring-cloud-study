package com.msa.discovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Eureka Server (서비스 디스커버리 서버)
 * 
 * MSA에서 각 마이크로서비스들이 자신을 등록하고,
 * 다른 서비스의 위치를 찾을 수 있게 해주는 중앙 레지스트리 역할
 * 
 * 예시:
 * - user-service가 시작되면 → Eureka에 "나 여기 있어요!" 등록
 * - gateway가 user-service를 찾을 때 → Eureka에게 "user-service 어디있어?" 물어봄
 * 
 * @author 팀장
 */
@SpringBootApplication  // Spring Boot 애플리케이션임을 선언
@EnableEurekaServer     // 이 애플리케이션을 Eureka Server로 동작하게 함
public class DiscoveryServiceApplication {
    
    public static void main(String[] args) {
        // Spring Boot 애플리케이션 시작
        // 실행하면 http://localhost:8761 에서 Eureka 대시보드 확인 가능
        SpringApplication.run(DiscoveryServiceApplication.class, args);
        
        System.out.println("==============================================");
        System.out.println("Eureka Server 시작 완료!");
        System.out.println("대시보드 접속: http://localhost:8761");
        System.out.println("ID: eureka / PW: eureka123");
        System.out.println("==============================================");
    }
}