# MCP Spoke Server SDK (Spring Boot Starter)

Spring Boot 3.x로 구축한 프로덕션 등급, 금융 등급의 Model Context Protocol (MCP) Spoke Server SDK입니다. 이 SDK는 보안, 감사 가능, 확장 가능한 MCP 서버를 구축하기 위한 완전한 프레임워크를 제공하며, 이중 JSON-RPC 2.0 및 REST 인터페이스를 갖추고 있습니다.

## 이 SDK는 무엇인가?

### 목적
이것은 **MCP Spoke 서버**를 구축하기 위한 **Spring Boot Starter 라이브러리**입니다. MCP Spoke 서버는 LLM 에이전트 및 AI 시스템에 도구/기능을 노출하는 서버 측 컴포넌트입니다.

### MCP 레이어
이 SDK는 **MCP 서버 (Spoke) 레이어**를 구현합니다:

```
┌─────────────────────────────────────────────────────────┐
│  LLM / AI 에이전트 / 클라이언트 애플리케이션            │
└────────────────────┬────────────────────────────────────┘
                     │ (JSON-RPC 2.0 또는 REST)
                     │
        ┌────────────▼────────────┐
        │  MCP Spoke 서버         │  ◄── 이 SDK
        │  (당신의 구현)          │
        └────────────┬────────────┘
                     │
        ┌────────────▼────────────┐
        │  당신의 비즈니스 로직    │
        │  (도구/기능)            │
        └────────────────────────┘
```

### 이 SDK로 구축하는 것
- **MCP Spoke 서버**: LLM 에이전트에 도구를 노출하는 독립형 서버
- **도구 구현**: MCP 도구로 래핑된 커스텀 비즈니스 로직
- **보안 API**: OAuth2, RBAC, ABAC, 감사 로깅 포함
- **프로덕션 준비**: 캐싱, 킬 스위치, 모니터링 포함

### 주요 특징
- **Spoke 전용**: 허브 컴포넌트 없음 (허브는 별도로 관리)
- **이중 인터페이스**: JSON-RPC 2.0 (네이티브 MCP) + REST (API 게이트웨이용)
- **단일 코어**: 모든 비즈니스 로직이 JSON-RPC 디스패처에 있음 (중복 없음)
- **금융 등급**: 보안, 감사, 준수 내장

## 아키텍처 개요

### 핵심 설계 원칙

- **Spoke 전용 MCP**: 허브 의존성 없음. 단일 비즈니스 로직 코어 (이중 프로토콜 인터페이스 포함)
- **프로토콜 분리**: JSON-RPC 2.0이 정규 프로토콜. REST는 프로토콜 어댑터로만 작동
- **단일 진실 공급원**: 모든 비즈니스 로직은 JSON-RPC 디스패처에 위치. REST 엔드포인트는 내부적으로 JSON-RPC로 변환
- **금융 등급 보안**: OAuth2 JWT 검증, RBAC+ABAC 인증, 열 수준 데이터 마스킹
- **감사 준수**: 서버 측 감사 로깅 (SHA-256 해싱), 비동기 Elasticsearch 인덱싱
- **킬 스위치 강제**: 도구 수준 및 글로벌 비활성화 (Redis + PostgreSQL 동기화)

### 모듈 구조

```
mcp-server-sdk/
├── mcp-core/                    # 핵심 JSON-RPC 엔진 & 강제 계층
│   ├── rpc/                     # JSON-RPC 2.0 디스패처 & 요청/응답 모델
│   ├── validation/              # JSON 스키마 검증기
│   ├── registry/                # 도구 레지스트리 서비스 (PostgreSQL + Redis 캐시)
│   ├── policy/                  # RBAC/ABAC 정책 서비스
│   ├── killswitch/              # 킬 스위치 강제
│   ├── masking/                 # 열 수준 데이터 마스킹
│   ├── audit/                   # 감사 로깅 서비스
│   ├── error/                   # 표준 오류 코드
│   └── meta/                    # MCP 메타데이터 모델
├── mcp-rest-adapter/            # REST → JSON-RPC 프로토콜 어댑터
├── mcp-security/                # OAuth2 JWT 검증
├── mcp-redis/                   # Redis 캐시 구현
├── mcp-postgres/                # PostgreSQL 저장소 구현
├── mcp-elasticsearch/           # Elasticsearch 감사 로깅
├── mcp-autoconfigure/           # Spring Boot 자동 구성
└── sample-spoke-app/            # 예제 구현
```

## 실행 흐름

### 요청 검증 파이프라인 (엄격한 순서)

```
1. JSON 파싱
   ↓
2. JSON-RPC 구조 검증 (jsonrpc=2.0, method, params, id, meta)
   ↓
3. 메타 필수 필드 검증 (user_id, caller_id, trace_id, dept)
   ↓
4. 글로벌 킬 스위치 확인
   ↓
5. 도구 레지스트리 조회 (PostgreSQL → Redis 캐시)
   ↓
6. 도구 킬 스위치 확인
   ↓
7. 인증 검증 (Redis 우선 → RDB 폴백)
   ↓
8. 입력 JSON 스키마 검증
   ↓
9. 핸들러 실행
   ↓
10. 데이터 마스킹 (역할 기반 열 마스킹)
    ↓
11. Elasticsearch에 비동기 감사 로깅
    ↓
12. 마스킹된 응답 반환
```

### JSON-RPC 2.0 요청 형식

```json
{
  "jsonrpc": "2.0",
  "method": "namespace.tool_id",
  "params": {
    "field1": "value1",
    "field2": 123
  },
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "meta": {
    "user_id": "user@company.com",
    "caller_id": "agent-001",
    "trace_id": "trace-uuid-v7",
    "dept": "RISK"
  }
}
```

### JSON-RPC 2.0 응답 형식

**성공:**
```json
{
  "jsonrpc": "2.0",
  "result": {
    "field1": "value1",
    "field2": 123
  },
  "id": "550e8400-e29b-41d4-a716-446655440000"
}
```

**오류:**
```json
{
  "jsonrpc": "2.0",
  "error": {
    "code": "POLICY_DENIED",
    "message": "User not authorized to access tool: ifrs17.loss_projection",
    "retryable": false
  },
  "id": "550e8400-e29b-41d4-a716-446655440000"
}
```

## 보안 모델

### 인증

- **OAuth2 Resource Server**: Spring Security OAuth2 통합
- **JWT 검증**: Nimbus JOSE 라이브러리 (JWKS 엔드포인트 지원)
- **토큰 검사**: 자동 만료 및 서명 검증

### 인증

- **RBAC**: 도구 정책을 통한 역할 기반 접근 제어
- **ABAC**: 데이터 수준을 통한 속성 기반 접근 제어 (PUBLIC, INTERNAL, CONFIDENTIAL)
- **정책 캐싱**: Redis 캐시 (30분 TTL), PostgreSQL 폴백

### 데이터 보호

- **열 수준 마스킹**: 역할 기반 동적 마스킹 (HASH, REDACT, PARTIAL)
- **SHA-256 해싱**: 민감한 필드 저장 전 해싱
- **감사 추적**: 모든 작업이 매개변수 해시(원본 매개변수 아님)와 함께 로깅됨

## 킬 스위치 동작

### 도구 수준 킬 스위치

```java
killSwitchService.disableTool("ifrs17.loss_projection", "Security vulnerability detected");
```

**효과:**
- 도구에 대한 모든 호출 차단 (JSON-RPC 및 REST)
- `TOOL_DISABLED` 오류 코드 반환
- Redis + PostgreSQL 간 동기화
- 즉시 효과 (캐시 지연 없음)

### 글로벌 킬 스위치

```java
killSwitchService.disableGlobal("Critical infrastructure issue");
```

**효과:**
- 모든 MCP 호출 차단 (모든 도구)
- `TOOL_DISABLED` 오류 코드 반환
- 모든 인스턴스에서 즉시 효과

### 상태 쿼리

```java
KillSwitchStatus status = killSwitchService.getToolStatus("ifrs17.loss_projection");
if (status != null && status.isDisabled()) {
    // 도구가 비활성화됨
}
```

## 표준 오류 코드

| 코드 | 의미 | 재시도 가능 | HTTP |
|------|------|-----------|------|
| `INVALID_PARAMS` | 요청 검증 실패 | 아니오 | 400 |
| `POLICY_DENIED` | 인증 실패 | 아니오 | 403 |
| `DATA_NOT_FOUND` | 리소스를 찾을 수 없음 | 아니오 | 404 |
| `TOOL_DISABLED` | 킬 스위치로 도구 비활성화됨 | 아니오 | 503 |
| `MCP_TIMEOUT` | 요청 시간 초과 | 예 | 504 |
| `MCP_INTERNAL_ERROR` | 서버 오류 | 아니오 | 500 |
| `UNAUTHORIZED` | 인증 실패 | 아니오 | 401 |
| `TOOL_NOT_FOUND` | 도구 미등록 | 아니오 | 404 |

## REST API 어댑터

REST 어댑터는 HTTP 요청을 JSON-RPC로 내부 변환합니다. 모든 비즈니스 로직은 JSON-RPC 코어에 유지됩니다.

### REST 엔드포인트 패턴

```
POST /api/{namespace}/{tool_id}
```

### REST 요청 헤더

```
Authorization: Bearer <JWT_TOKEN>
X-Client-Id: agent-001
X-Trace-Id: trace-uuid-v7
X-Dept: RISK
X-User-Id: user@company.com (선택사항, JWT에서 추출되지 않으면)
```

### REST 요청 본문

```json
{
  "field1": "value1",
  "field2": 123
}
```

### REST 응답

```json
{
  "field1": "value1",
  "field2": 123
}
```

### 오류 응답

```json
{
  "jsonrpc": "2.0",
  "error": {
    "code": "POLICY_DENIED",
    "message": "User not authorized",
    "retryable": false
  },
  "id": "..."
}
```

## 감사 로깅

### 감사 로그 구조

```json
{
  "trace_id": "trace-uuid-v7",
  "user_id": "user@company.com",
  "caller_id": "agent-001",
  "tool_id": "ifrs17.loss_projection",
  "method": "api.ifrs17.loss_projection",
  "params_hash": "sha256_hash_of_params",
  "result_code": "SUCCESS",
  "latency_ms": 145,
  "timestamp": 1702000000000,
  "error_message": null,
  "dept": "RISK"
}
```

### 감사 기능

- **비동기**: Spring `@Async`를 통한 논블로킹 로깅
- **Elasticsearch**: 준수 및 포렌식을 위해 인덱싱됨
- **매개변수 해싱**: 매개변수의 SHA-256 해시 (원본 값 아님)
- **지연 시간 추적**: 요청 지속 시간 (밀리초)
- **오류 캡처**: 실패한 호출에 대한 오류 메시지 로깅
- **추적 상관**: 모든 로그가 trace_id를 통해 연결됨

## 도구 레지스트리

### 도구 등록

```java
ToolRegistry tool = ToolRegistry.builder()
    .toolId("ifrs17.loss_projection")
    .toolName("IFRS17 Loss Projection")
    .version("1.0.0")
    .status("ACTIVE")
    .inputSchema(jsonSchema)
    .description("Calculate IFRS17 loss projections")
    .createdAt(System.currentTimeMillis())
    .updatedAt(System.currentTimeMillis())
    .build();

repository.save(tool);
```

### 도구 상태 값

- `ACTIVE`: 도구 사용 가능
- `DISABLED`: 도구 비활성화 (대신 킬 스위치 사용)

### 입력 스키마

요청 검증을 위한 JSON 스키마 형식:

```json
{
  "type": "object",
  "required": ["portfolio_value", "loss_rate", "projection_years"],
  "properties": {
    "portfolio_value": {"type": "number"},
    "loss_rate": {"type": "number"},
    "projection_years": {"type": "integer"}
  }
}
```

## 정책 관리

### 도구 정책

```java
ToolPolicy policy = ToolPolicy.builder()
    .userId("user@company.com")
    .toolId("ifrs17.loss_projection")
    .allowed(true)
    .dataLevel("CONFIDENTIAL")
    .createdAt(System.currentTimeMillis())
    .updatedAt(System.currentTimeMillis())
    .build();

policyRepository.savePolicy(policy);
```

### 데이터 마스킹 정책

```java
DataMaskingPolicy maskingPolicy = DataMaskingPolicy.builder()
    .userId("user@company.com")
    .toolId("ifrs17.loss_projection")
    .columnMasks(Map.of(
        "portfolio_value", "PARTIAL",
        "loss_rate", "HASH"
    ))
    .dataLevel("CONFIDENTIAL")
    .build();

policyRepository.saveMaskingPolicy(maskingPolicy);
```

### 마스킹 유형

- `HASH`: SHA-256 해시 (처음 16자)
- `REDACT`: `***REDACTED***`로 교체
- `PARTIAL`: 처음 25% 표시, 나머지 마스킹

## 데이터베이스 스키마

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

## 구성

### application.yml

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/mcp_db
    username: postgres
    password: postgres
  
  redis:
    host: localhost
    port: 6379
  
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://your-auth-provider.com
          jwk-set-uri: https://your-auth-provider.com/.well-known/jwks.json

server:
  port: 8080
```

## 사용 예제

### 1. JsonRpcHandler 구현

```java
@Component
public class MyToolHandler implements JsonRpcHandler {
    @Override
    public Object handle(JsonRpcRequest request) {
        // 비즈니스 로직
        return result;
    }
}
```

### 2. 도구 등록

```java
@Component
public class ToolInitializer implements CommandLineRunner {
    @Override
    public void run(String... args) {
        ToolRegistry tool = ToolRegistry.builder()
            .toolId("my.tool")
            .toolName("My Tool")
            .version("1.0.0")
            .status("ACTIVE")
            .inputSchema(schema)
            .build();
        
        repository.save(tool);
    }
}
```

### 3. JSON-RPC를 통해 호출

```bash
curl -X POST http://localhost:8080/mcp/rpc \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>" \
  -d '{
    "jsonrpc": "2.0",
    "method": "api.my.tool",
    "params": {"field": "value"},
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "meta": {
      "user_id": "user@company.com",
      "caller_id": "agent-001",
      "trace_id": "trace-uuid",
      "dept": "RISK"
    }
  }'
```

### 4. REST를 통해 호출

```bash
curl -X POST http://localhost:8080/api/my/tool \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT>" \
  -H "X-Client-Id: agent-001" \
  -H "X-Trace-Id: trace-uuid" \
  -H "X-Dept: RISK" \
  -d '{"field": "value"}'
```

## 성능 고려 사항

- **Redis 캐싱**: 도구 레지스트리 및 정책이 30-60분 동안 캐시됨
- **비동기 감사 로깅**: Elasticsearch에 대한 논블로킹 인덱싱
- **연결 풀링**: Redis용 Lettuce, PostgreSQL용 HikariCP
- **배치 호출**: 지원되지 않음 (MCP 사양 기준)
- **알림**: 지원되지 않음 (MCP 사양 기준)

## 프로덕션 배포

### 필수 조건

- Java 17+
- PostgreSQL 12+
- Redis 6+
- Elasticsearch 8+
- OAuth2 제공자 (Auth0, Okta 등)

### 환경 변수

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://prod-db:5432/mcp
SPRING_DATASOURCE_USERNAME=mcp_user
SPRING_DATASOURCE_PASSWORD=<secure_password>
SPRING_REDIS_HOST=prod-redis
SPRING_REDIS_PORT=6379
SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI=https://auth.company.com
SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI=https://auth.company.com/.well-known/jwks.json
```

### Docker 배포

```dockerfile
FROM eclipse-temurin:17-jre-alpine
COPY sample-spoke-app/build/libs/sample-spoke-app-1.0.0.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## 문제 해결

### 도구를 찾을 수 없음

- 도구가 `tool_registry` 테이블에 등록되어 있는지 확인
- Redis 캐시 확인: `redis-cli get tools:tool_id`
- 메서드 형식 확인: `namespace.tool_id`

### 인증 거부됨

- `tool_policy` 테이블에서 사용자 + 도구 조합 확인
- 사용자가 `allowed=true`를 가지고 있는지 확인
- Redis 캐시 확인: `redis-cli get policy:user_id:tool_id`

### 킬 스위치 활성화됨

- 쿼리: `SELECT * FROM kill_switch WHERE target_id = 'tool_id'`
- 또는: `redis-cli get kill_switch:tool:tool_id`
- 다시 활성화: `killSwitchService.enableTool(toolId)`

### 감사 로그 누락됨

- Elasticsearch가 실행 중인지 확인
- 인덱스 확인: `curl http://localhost:9200/mcp-audit/_search`
- 비동기 실행기 확인: 메인 클래스에 `@EnableAsync`

## 라이선스

독점 - 금융 등급 MCP SDK
