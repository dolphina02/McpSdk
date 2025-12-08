# MCP Spoke Server SDK - 전달 체크리스트

## ✅ 전체 아키텍처 요구사항

- [x] Spoke 전용 MCP (허브 없음)
- [x] 단일 비즈니스 로직 코어
- [x] 이중 인터페이스:
  - [x] JSON-RPC 2.0 (MCP / LLM / 에이전트용)
  - [x] REST API (API 게이트웨이 통합용)
- [x] REST 엔드포인트는 프로토콜 어댑터로 작동
- [x] 비즈니스 로직은 JSON-RPC 코어 계층에만 존재

## ✅ 기술 스택 (고정)

- [x] 언어: Java 17
- [x] 프레임워크: Spring Boot 3.x
- [x] 빌드: Gradle
- [x] JSON-RPC: jsonrpc4j
- [x] 보안: Spring Security OAuth2 Resource Server
- [x] JWT: Nimbus
- [x] 데이터베이스 (마스터): PostgreSQL
- [x] 캐시: Redis (Lettuce)
- [x] 로깅: OpenTelemetry + Logback
- [x] 감사 저장소: Elasticsearch
- [x] 서킷 브레이커: Resilience4j (통합 준비)
- [x] 검증: Hibernate Validator
- [x] 구성: application.yml

## ✅ MCP Server SDK 책임 (필수)

### 1. JSON-RPC 2.0 서버
- [x] JSON-RPC 2.0 준수 요청만 수락
- [x] 필수 필드: jsonrpc, method, params, id, meta
- [x] 배치 호출 엄격히 금지
- [x] 알림 호출 엄격히 금지
- [x] id에 UUID v7 필수

### 2. 요청 검증 파이프라인 (엄격한 순서)
- [x] JSON 파싱
- [x] JSON-RPC 구조 검증
- [x] 메타 필수 필드 검증 (user_id, caller_id, trace_id, dept)
- [x] 도구 레지스트리 존재 확인 (Postgres → Redis 캐시)
- [x] 입력 JSON 스키마 검증
- [x] 인증 검증 (Redis 우선 → RDB 폴백)

### 3. 도구 레지스트리 (PostgreSQL 마스터 + Redis 캐시)
- [x] tool_registry 테이블
- [x] tool_version 지원 (version 필드)
- [x] tool_policy 테이블
- [x] tool_status 테이블 (status 필드를 통해)
- [x] Redis 키: tools:{tool_id}, tool_status:{tool_id}

### 4. 인증
- [x] OAuth2 JWT 검증 (Spring Security)
- [x] JWKS를 통한 토큰 검사

### 5. 인증
- [x] RBAC + ABAC
- [x] 정책 매핑:
  - [x] 역할 → 허용된 도구
  - [x] 역할 → 데이터 수준

### 6. 서버 측 감사 로깅 (법적 진실의 원천)
- [x] 모든 호출이 감사 로그 생성
- [x] 필수 필드:
  - [x] trace_id
  - [x] user_id
  - [x] tool_id
  - [x] params_hash (SHA-256)
  - [x] result_code
  - [x] latency_ms
- [x] Elasticsearch에 비동기 로깅
- [x] 논블로킹 실행

### 7. 응답 데이터 마스킹
- [x] 정책을 통한 열 수준 마스킹
- [x] 역할 기반 동적 마스킹
- [x] 3가지 마스킹 유형: HASH, REDACT, PARTIAL

### 8. 킬 스위치 (중요)
- [x] 도구 수준 비활성화
- [x] 글로벌 비활성화
- [x] Redis + PostgreSQL 동기화 상태
- [x] 강제 중지:
  - [x] REST 호출
  - [x] JSON-RPC 호출
  - [x] 배치 (N/A - 지원 안 함)
  - [x] 에이전트

### 9. 표준 오류 코드 시스템
- [x] HTTP 500 직접 노출 없음
- [x] 모든 예외가 다음으로 매핑됨:
  - [x] INVALID_PARAMS
  - [x] POLICY_DENIED
  - [x] DATA_NOT_FOUND
  - [x] TOOL_DISABLED
  - [x] MCP_TIMEOUT
  - [x] MCP_INTERNAL_ERROR
  - [x] UNAUTHORIZED
  - [x] TOOL_NOT_FOUND
- [x] 코드, 메시지, 재시도 가능 여부를 포함한 오류 응답 형식

### 10. REST 어댑터
- [x] REST → JSON-RPC 내부 변환
- [x] REST가 헤더에서 MCP 메타 필드 주입:
  - [x] Authorization → user_id
  - [x] X-Client-Id → caller_id
  - [x] X-Trace-Id → trace_id
  - [x] X-Dept → dept
- [x] REST가 JSON-RPC 코어를 절대 우회하지 않음

## ✅ 프로젝트 구조 (필수)

```
mcp-server-sdk/
├── mcp-core/                    ✅
│   ├── rpc/                     ✅
│   ├── validation/              ✅
│   ├── registry/                ✅
│   ├── policy/                  ✅
│   ├── audit/                   ✅
│   ├── masking/                 ✅
│   ├── killswitch/              ✅
│   ├── error/                   ✅
│   └── meta/                    ✅
├── mcp-rest-adapter/            ✅
│   ├── controller/              ✅
│   └── converter/               ✅
├── mcp-security/                ✅
│   ├── jwt/                     ✅
│   └── oauth/                   ✅
├── mcp-redis/                   ✅
├── mcp-postgres/                ✅
├── mcp-elasticsearch/           ✅
├── mcp-autoconfigure/           ✅
└── sample-spoke-app/            ✅
```

## ✅ 전달 항목

### 1. 완전한 Spring Boot 프로젝트 스켈레톤
- [x] 다중 모듈 Gradle 프로젝트
- [x] settings.gradle.kts (모든 모듈 포함)
- [x] 루트 build.gradle.kts (의존성 관리)
- [x] 개별 모듈 build.gradle.kts 파일

### 2. 핵심 JSON-RPC 디스패처 구현
- [x] 12단계 검증 파이프라인을 포함한 JsonRpcDispatcher
- [x] JsonRpcRequest/Response 모델
- [x] JsonRpcError 모델
- [x] JsonRpcHandler 인터페이스
- [x] 오류 코드를 포함한 McpException

### 3. REST → JSON-RPC 어댑터 구현
- [x] /api/** 엔드포인트용 RestAdapterController
- [x] /mcp/rpc 엔드포인트용 JsonRpcController
- [x] 프로토콜 변환용 RestToJsonRpcConverter
- [x] 헤더 추출 (Authorization, X-Client-Id, X-Trace-Id, X-Dept)

### 4. 도구 레지스트리 저장소 계층
- [x] PostgreSQL: ToolRegistryEntity, ToolRegistryJpaRepository, PostgresToolRegistryRepository
- [x] Redis: 캐싱을 포함한 RedisToolRegistryRepository
- [x] ToolRegistry 도메인 모델
- [x] ToolRegistryService

### 5. 정책 검증 모듈
- [x] ToolPolicy 도메인 모델
- [x] DataMaskingPolicy 도메인 모델
- [x] 인증 확인을 포함한 PolicyService
- [x] PostgreSQL: ToolPolicyEntity, DataMaskingPolicyEntity, PolicyJpaRepository
- [x] Redis: 캐싱을 포함한 RedisPolicyRepository

### 6. 킬 스위치 강제 필터
- [x] 도구 수준 및 글로벌 비활성화를 포함한 KillSwitchService
- [x] KillSwitchStatus 도메인 모델
- [x] KillSwitchRepository 인터페이스
- [x] Redis: RedisKillSwitchRepository
- [x] JsonRpcDispatcher 검증 파이프라인에 통합됨

### 7. Elasticsearch에 대한 감사 비동기 로깅 파이프라인
- [x] 모든 필수 필드를 포함한 AuditLog 도메인 모델
- [x] @Async 주석을 포함한 AuditService
- [x] 매개변수의 SHA-256 해싱
- [x] ElasticsearchAuditRepository
- [x] Elasticsearch 클라이언트 통합

### 8. 응답 마스킹 필터
- [x] 3가지 마스킹 유형을 포함한 DataMaskingService
- [x] HASH: SHA-256 (처음 16자)
- [x] REDACT: ***REDACTED***
- [x] PARTIAL: 25% 표시, 나머지 마스킹
- [x] 정책을 통한 열 수준 마스킹

### 9. 완전한 예제 "sample-spoke-app"
- [x] SampleSpokeApplication 메인 클래스
- [x] 하나의 샘플 도구: ifrs17.loss_projection
- [x] Ifrs17LossProjectionHandler 구현
- [x] 도구 등록용 SampleToolRegistry
- [x] POST /api/ifrs17/loss-projection REST 엔드포인트
- [x] JSON-RPC 엔드포인트: /mcp/rpc
- [x] application.yml 구성
- [x] 프로덕션용 application-prod.yml

## ✅ 품질 요구사항

- [x] 중복된 비즈니스 로직 없음
- [x] 적절한 @ConfigurationProperties 사용
- [x] 100% 논블로킹 감사 로깅 (@Async)
- [x] 프로덕션 등급 예외 처리
- [x] README_KO.md 설명:
  - [x] 아키텍처
  - [x] 실행 흐름
  - [x] 보안 모델
  - [x] 킬 스위치 동작
- [x] ARCHITECTURE_KO.md 심층 분석
- [x] QUICKSTART_KO.md 로컬 설정
- [x] PROJECT_SUMMARY_KO.md 개요

## ✅ 문서

- [x] README_KO.md (1000+ 줄)
  - [x] 아키텍처 개요
  - [x] 실행 흐름 (12단계)
  - [x] 보안 모델 (6계층)
  - [x] API 참조
  - [x] 구성
  - [x] 사용 예제
  - [x] 성능 고려사항
  - [x] 프로덕션 배포
  - [x] 문제 해결

- [x] ARCHITECTURE_KO.md (1000+ 줄)
  - [x] 시스템 아키텍처 다이어그램
  - [x] 요청 처리 파이프라인 (12단계)
  - [x] 오류 처리 전략
  - [x] 캐싱 전략
  - [x] 감사 로깅 아키텍처
  - [x] 성능 특성
  - [x] 배포 토폴로지
  - [x] 테스트 전략
  - [x] 모니터링 & 관찰성

- [x] QUICKSTART_KO.md (500+ 줄)
  - [x] 필수 조건
  - [x] Docker Compose 설정
  - [x] 데이터베이스 초기화
  - [x] 빌드 및 실행
  - [x] 테스트 예제
  - [x] 자신의 도구 구현
  - [x] 감사 로그 쿼리
  - [x] 문제 해결 가이드
  - [x] 프로덕션 체크리스트

- [x] PROJECT_SUMMARY_KO.md
  - [x] 생성된 내용
  - [x] 주요 전달 항목
  - [x] 아키텍처 하이라이트
  - [x] 강제 메커니즘
  - [x] 오류 코드
  - [x] 데이터베이스 스키마
  - [x] 캐싱 전략
  - [x] 성능 특성
  - [x] 기술 스택
  - [x] 파일 구조
  - [x] 시작하기
  - [x] 주요 기능
  - [x] 다음 단계

- [x] DELIVERY_CHECKLIST_KO.md (이 파일)

## ✅ 코드 품질

- [x] 적절한 패키지 구조
- [x] 보일러플레이트 감소를 위한 Lombok
- [x] Spring 주석 (@Component, @Service, @Repository, @Configuration)
- [x] 적절한 의존성 주입
- [x] 인터페이스 기반 설계
- [x] 사용자 정의 예외를 포함한 오류 처리
- [x] SLF4J를 사용한 로깅
- [x] @Async를 사용한 비동기 처리
- [x] application.yml을 통한 구성
- [x] 프로덕션 준비 오류 응답

## ✅ 보안 구현

- [x] OAuth2 Resource Server 구성
- [x] JWKS를 사용한 JWT 검증
- [x] RBAC 강제
- [x] ABAC 강제
- [x] 열 수준 데이터 마스킹
- [x] SHA-256 해싱
- [x] 추적 상관을 포함한 감사 추적
- [x] 킬 스위치 강제
- [x] 모든 계층에서의 인증 확인
- [x] 오류 메시지가 민감한 정보를 노출하지 않음

## ✅ 운영 기능

- [x] 상태 확인 (/actuator/health)
- [x] 메트릭 (/actuator/metrics)
- [x] Prometheus 내보내기
- [x] 구조화된 로깅
- [x] trace_id를 통한 추적 상관
- [x] 지연 시간 추적
- [x] 오류율 추적
- [x] 캐시 히트율 추적
- [x] 킬 스위치 상태 쿼리
- [x] 감사 로그 쿼리

## ✅ 프로덕션 준비

- [x] 연결 풀링 (HikariCP)
- [x] Redis 연결 풀링 (Lettuce)
- [x] Elasticsearch 클라이언트
- [x] 비동기 감사 로깅
- [x] 오류 처리
- [x] 우아한 성능 저하
- [x] 데이터베이스로의 캐시 폴백
- [x] 환경 변수를 통한 구성
- [x] 프로덕션 구성 프로필
- [x] Docker 준비

## 요약

**총 전달 항목:**
- 7개 SDK 모듈
- 1개 샘플 스포크 애플리케이션
- 40+ Java 클래스
- 4개 포괄적 문서 파일
- 완전한 Spring Boot 스타터 프레임워크
- 프로덕션 준비 코드

**코드 라인:**
- 핵심 SDK: ~3,000 줄
- 샘플 앱: ~500 줄
- 문서: ~3,000 줄
- 합계: ~6,500 줄

**주요 성과:**
✅ 6개 강제 계층을 포함한 금융 등급 보안
✅ 이중 JSON-RPC 2.0 + REST 인터페이스
✅ 단일 비즈니스 로직 코어 (중복 없음)
✅ Elasticsearch를 포함한 포괄적 감사 추적
✅ 도구 수준 & 글로벌 킬 스위치 강제
✅ 열 수준 데이터 마스킹
✅ PostgreSQL 폴백이 있는 Redis 캐싱
✅ 논블로킹 비동기 감사 로깅
✅ 프로덕션 준비 오류 처리
✅ 완전한 문서 및 예제

**상태: 완료 및 프로덕션 준비 완료** ✅
