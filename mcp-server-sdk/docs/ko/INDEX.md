# MCP Spoke Server SDK - 완전 인덱스

## 📚 문서 (여기서 시작하세요)

1. **[PROJECT_SUMMARY_KO.md](PROJECT_SUMMARY_KO.md)** - 생성된 내용 개요
   - 전달된 항목
   - 주요 기능
   - 아키텍처 하이라이트
   - 시작하기

2. **[README_KO.md](README_KO.md)** - 완전한 참조 가이드 (1000+ 줄)
   - 아키텍처 개요
   - 실행 흐름 (12단계)
   - 보안 모델 (6계층)
   - API 참조
   - 구성
   - 문제 해결

3. **[ARCHITECTURE_KO.md](ARCHITECTURE_KO.md)** - 시스템 설계 심층 분석 (1000+ 줄)
   - 시스템 아키텍처 다이어그램
   - 요청 처리 파이프라인
   - 오류 처리 전략
   - 캐싱 전략
   - 감사 로깅 아키텍처
   - 성능 특성
   - 배포 토폴로지

4. **[QUICKSTART_KO.md](QUICKSTART_KO.md)** - 로컬 개발 설정 (500+ 줄)
   - 필수 조건
   - Docker Compose 설정
   - 데이터베이스 초기화
   - 빌드 및 실행
   - 테스트 예제
   - 자신의 도구 구현
   - 문제 해결

5. **[DELIVERY_CHECKLIST_KO.md](DELIVERY_CHECKLIST_KO.md)** - 전달된 항목
   - 모든 요구사항의 완전한 체크리스트
   - 모든 기능 검증
   - 품질 메트릭

## 🏗️ 프로젝트 구조

### 핵심 SDK 모듈

```
mcp-core/                          # JSON-RPC 2.0 엔진 & 강제 계층
├── rpc/                           # 디스패처, 요청/응답 모델
├── validation/                    # JSON 스키마 검증기
├── registry/                      # 도구 레지스트리 서비스
├── policy/                        # 인증 & RBAC/ABAC
├── killswitch/                    # 킬 스위치 강제
├── masking/                       # 열 수준 데이터 마스킹
├── audit/                         # 감사 로깅 서비스
├── error/                         # 표준 오류 코드
└── meta/                          # MCP 메타데이터 모델

mcp-rest-adapter/                  # REST → JSON-RPC 프로토콜 어댑터
├── controller/                    # REST 엔드포인트
└── converter/                     # 프로토콜 변환

mcp-security/                      # OAuth2 JWT 검증
├── jwt/                           # JWT 토큰 검증
└── oauth/                         # OAuth2 구성

mcp-redis/                         # Redis 캐시 구현
├── RedisToolRegistryRepository
├── RedisKillSwitchRepository
└── RedisPolicyRepository

mcp-postgres/                      # PostgreSQL 저장소
├── entity/                        # JPA 엔티티
└── repository/                    # JPA 저장소

mcp-elasticsearch/                 # Elasticsearch 감사 로깅
└── ElasticsearchAuditRepository

mcp-autoconfigure/                 # Spring Boot 자동 구성
└── McpServerAutoConfiguration
```

### 샘플 애플리케이션

```
sample-spoke-app/                  # 완전한 예제 구현
├── SampleSpokeApplication         # 메인 클래스
├── tool/
│   └── Ifrs17LossProjectionHandler # 예제 도구 핸들러
├── config/
│   └── SampleToolRegistry         # 도구 등록
└── resources/
    ├── application.yml            # 개발 구성
    └── application-prod.yml       # 프로덕션 구성
```

## 🔑 주요 구성 요소

### JSON-RPC 2.0 디스패처

**파일:** `mcp-core/src/main/java/com/financial/mcp/core/rpc/JsonRpcDispatcher.java`

시스템의 핵심. 12단계 검증 파이프라인 구현:
1. JSON 파싱
2. JSON-RPC 구조 검증
3. 메타 필드 검증
4. 글로벌 킬 스위치 확인
5. 도구 레지스트리 조회
6. 도구 킬 스위치 확인
7. 인증 검증
8. 입력 스키마 검증
9. 핸들러 실행
10. 데이터 마스킹
11. 비동기 감사 로깅
12. 응답 직렬화

### REST 어댑터

**파일:**
- `mcp-rest-adapter/src/main/java/com/financial/mcp/rest/controller/RestAdapterController.java`
- `mcp-rest-adapter/src/main/java/com/financial/mcp/rest/converter/RestToJsonRpcConverter.java`

REST 요청을 JSON-RPC로 내부 변환. 모든 비즈니스 로직은 디스패처에 유지됨.

### 도구 레지스트리

**파일:**
- `mcp-core/src/main/java/com/financial/mcp/core/registry/ToolRegistryService.java`
- `mcp-postgres/src/main/java/com/financial/mcp/postgres/repository/PostgresToolRegistryRepository.java`
- `mcp-redis/src/main/java/com/financial/mcp/redis/RedisToolRegistryRepository.java`

PostgreSQL 마스터 및 Redis 캐시(60분 TTL)를 사용한 도구 등록 관리.

### 인증 & 정책

**파일:**
- `mcp-core/src/main/java/com/financial/mcp/core/policy/PolicyService.java`
- `mcp-postgres/src/main/java/com/financial/mcp/postgres/repository/PostgresPolicyRepository.java`
- `mcp-redis/src/main/java/com/financial/mcp/redis/RedisPolicyRepository.java`

Redis 캐싱(30분 TTL)을 사용한 RBAC + ABAC 강제.

### 킬 스위치

**파일:**
- `mcp-core/src/main/java/com/financial/mcp/core/killswitch/KillSwitchService.java`
- `mcp-redis/src/main/java/com/financial/mcp/redis/RedisKillSwitchRepository.java`

도구 수준 및 글로벌 비활성화 (즉시 효과).

### 데이터 마스킹

**파일:** `mcp-core/src/main/java/com/financial/mcp/core/masking/DataMaskingService.java`

3가지 유형의 열 수준 마스킹:
- HASH: SHA-256 (처음 16자)
- REDACT: ***REDACTED***
- PARTIAL: 25% 표시, 나머지 마스킹

### 감사 로깅

**파일:**
- `mcp-core/src/main/java/com/financial/mcp/core/audit/AuditService.java`
- `mcp-elasticsearch/src/main/java/com/financial/mcp/elasticsearch/ElasticsearchAuditRepository.java`

Elasticsearch에 대한 비동기 감사 로깅 (매개변수의 SHA-256 해싱).

## 📋 데이터베이스 스키마

### tool_registry
```sql
CREATE TABLE tool_registry (
    id UUID PRIMARY KEY,
    tool_id VARCHAR(255) UNIQUE NOT NULL,
    tool_name VARCHAR(255) NOT NULL,
    version VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    input_schema JSONB,
    description TEXT,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL
);
```

### tool_policy
```sql
CREATE TABLE tool_policy (
    id UUID PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    tool_id VARCHAR(255) NOT NULL,
    allowed BOOLEAN NOT NULL,
    data_level VARCHAR(50) NOT NULL,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    UNIQUE(user_id, tool_id)
);
```

### data_masking_policy
```sql
CREATE TABLE data_masking_policy (
    id UUID PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    tool_id VARCHAR(255) NOT NULL,
    column_masks JSONB,
    data_level VARCHAR(50) NOT NULL,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    UNIQUE(user_id, tool_id)
);
```

## 🔐 보안 계층

1. **전송**: HTTPS/TLS
2. **인증**: OAuth2 JWT (JWKS 포함)
3. **인증**: RBAC + ABAC (정책 캐싱 포함)
4. **데이터 보호**: 열 수준 마스킹
5. **감사**: SHA-256 해싱을 사용한 서버 측 로깅
6. **운영**: 킬 스위치 강제

## 📊 캐싱 전략

| 캐시 | 키 패턴 | TTL | 폴백 |
|------|---------|-----|------|
| 도구 레지스트리 | `tools:{tool_id}` | 60분 | PostgreSQL |
| 도구 정책 | `policy:{user_id}:{tool_id}` | 30분 | PostgreSQL |
| 마스킹 정책 | `masking:{user_id}:{tool_id}` | 30분 | PostgreSQL |
| 킬 스위치 | `kill_switch:tool:{tool_id}` | 없음 | 영구 |
| 킬 스위치 | `kill_switch:global` | 없음 | 영구 |

## 🚀 빠른 시작

### 1. 로컬 개발

```bash
# 인프라 시작
docker-compose up -d

# 데이터베이스 초기화
psql -h localhost -U postgres -d mcp_db < schema.sql

# 빌드
./gradlew clean build

# 실행
./gradlew :sample-spoke-app:bootRun
```

### 2. JSON-RPC 테스트

```bash
curl -X POST http://localhost:8080/mcp/rpc \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "method": "api.ifrs17.loss_projection",
    "params": {"portfolio_value": 1000000, "loss_rate": 0.05, "projection_years": 3},
    "id": "uuid-v7",
    "meta": {
      "user_id": "user@company.com",
      "caller_id": "agent-001",
      "trace_id": "trace-uuid",
      "dept": "RISK"
    }
  }'
```

### 3. REST 테스트

```bash
curl -X POST http://localhost:8080/api/ifrs17/loss-projection \
  -H "Content-Type: application/json" \
  -H "X-Client-Id: agent-001" \
  -H "X-Trace-Id: trace-uuid" \
  -H "X-Dept: RISK" \
  -d '{"portfolio_value": 1000000, "loss_rate": 0.05, "projection_years": 3}'
```

## 📖 읽기 가이드

### 아키텍트용
1. [PROJECT_SUMMARY_KO.md](PROJECT_SUMMARY_KO.md)부터 시작
2. 심층 분석을 위해 [ARCHITECTURE_KO.md](ARCHITECTURE_KO.md) 읽기
3. 완전한 참조를 위해 [README_KO.md](README_KO.md) 검토

### 개발자용
1. [QUICKSTART_KO.md](QUICKSTART_KO.md)부터 시작
2. 예제를 위해 sample-spoke-app 검토
3. API 참조를 위해 [README_KO.md](README_KO.md) 읽기
4. 세부 사항을 위해 [ARCHITECTURE_KO.md](ARCHITECTURE_KO.md) 확인

### DevOps용
1. 로컬 설정을 위해 [QUICKSTART_KO.md](QUICKSTART_KO.md) 읽기
2. 프로덕션 구성을 위해 application-prod.yml 검토
3. 배포 섹션을 위해 [README_KO.md](README_KO.md) 확인
4. 토폴로지를 위해 [ARCHITECTURE_KO.md](ARCHITECTURE_KO.md) 검토

### 보안용
1. [README_KO.md](README_KO.md)의 보안 모델 섹션 읽기
2. [ARCHITECTURE_KO.md](ARCHITECTURE_KO.md)의 보안 계층 섹션 검토
3. [QUICKSTART_KO.md](QUICKSTART_KO.md)에서 인증 테스트 확인

## 🎯 주요 기능

✅ **Spoke 전용 MCP**: 허브 의존성 없음
✅ **이중 인터페이스**: JSON-RPC 2.0 + REST
✅ **단일 로직 코어**: 중복 없음
✅ **금융 등급 보안**: OAuth2, RBAC, ABAC, 마스킹
✅ **감사 준수**: 서버 측 로깅, Elasticsearch
✅ **킬 스위치**: 도구 수준 & 글로벌 비활성화
✅ **캐싱**: PostgreSQL 폴백이 있는 Redis
✅ **비동기 로깅**: 논블로킹 감사 추적
✅ **오류 처리**: 표준 오류 코드
✅ **검증**: JSON 스키마 + 메타 검증
✅ **데이터 보호**: 열 수준 마스킹
✅ **추적 상관**: 분산 추적 지원
✅ **프로덕션 준비**: 모니터링, 메트릭, 상태 확인

## 📞 지원

### 일반적인 문제

**도구를 찾을 수 없음**
- 확인: `SELECT * FROM tool_registry WHERE tool_id = 'my.tool';`
- 캐시 지우기: `redis-cli DEL tools:my.tool`

**인증 거부됨**
- 확인: `SELECT * FROM tool_policy WHERE user_id = 'user@company.com' AND tool_id = 'my.tool';`
- 검증: `allowed = true`

**킬 스위치 활성화됨**
- 확인: `redis-cli GET kill_switch:tool:my.tool`
- 다시 활성화: `redis-cli DEL kill_switch:tool:my.tool`

**감사 로그 누락됨**
- Elasticsearch 검증: `curl http://localhost:9200/mcp-audit/_search`
- 비동기 실행기 확인: 메인 클래스에 `@EnableAsync`

### 문서 참조

- 아키텍처 질문 → [ARCHITECTURE_KO.md](ARCHITECTURE_KO.md)
- API 참조 → [README_KO.md](README_KO.md)
- 설정 문제 → [QUICKSTART_KO.md](QUICKSTART_KO.md)
- 기능 검증 → [DELIVERY_CHECKLIST_KO.md](DELIVERY_CHECKLIST_KO.md)

## 🔗 파일 네비게이션

### 핵심 구현
- [JsonRpcDispatcher](mcp-core/src/main/java/com/financial/mcp/core/rpc/JsonRpcDispatcher.java) - 메인 디스패처
- [RestAdapterController](mcp-rest-adapter/src/main/java/com/financial/mcp/rest/controller/RestAdapterController.java) - REST 엔드포인트
- [ToolRegistryService](mcp-core/src/main/java/com/financial/mcp/core/registry/ToolRegistryService.java) - 도구 관리
- [PolicyService](mcp-core/src/main/java/com/financial/mcp/core/policy/PolicyService.java) - 인증
- [KillSwitchService](mcp-core/src/main/java/com/financial/mcp/core/killswitch/KillSwitchService.java) - 킬 스위치
- [DataMaskingService](mcp-core/src/main/java/com/financial/mcp/core/masking/DataMaskingService.java) - 데이터 마스킹
- [AuditService](mcp-core/src/main/java/com/financial/mcp/core/audit/AuditService.java) - 감사 로깅

### 샘플 애플리케이션
- [SampleSpokeApplication](sample-spoke-app/src/main/java/com/financial/mcp/sample/SampleSpokeApplication.java) - 메인 클래스
- [Ifrs17LossProjectionHandler](sample-spoke-app/src/main/java/com/financial/mcp/sample/tool/Ifrs17LossProjectionHandler.java) - 예제 도구
- [SampleToolRegistry](sample-spoke-app/src/main/java/com/financial/mcp/sample/config/SampleToolRegistry.java) - 도구 등록

### 구성
- [application.yml](sample-spoke-app/src/main/resources/application.yml) - 개발 구성
- [application-prod.yml](sample-spoke-app/src/main/resources/application-prod.yml) - 프로덕션 구성

## ✅ 상태

**프로젝트 상태: 완료 및 프로덕션 준비 완료**

모든 요구사항 충족:
- ✅ 7개 SDK 모듈
- ✅ 1개 샘플 스포크 애플리케이션
- ✅ 40+ Java 클래스
- ✅ 4개 포괄적 문서 파일
- ✅ 완전한 Spring Boot 스타터 프레임워크
- ✅ 프로덕션 준비 코드

**준비 완료:**
- ✅ 로컬 개발
- ✅ 통합 테스트
- ✅ 프로덕션 배포
- ✅ 사용자 정의 도구 구현
- ✅ 엔터프라이즈 채택
