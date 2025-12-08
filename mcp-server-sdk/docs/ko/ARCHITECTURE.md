# MCP Spoke Server SDK - 아키텍처 심층 분석

## 시스템 아키텍처

### 고수준 설계

```
┌─────────────────────────────────────────────────────────────────┐
│                     외부 클라이언트                              │
│  (LLM 에이전트, API 게이트웨이, 직접 호출자)                    │
└────────────────┬──────────────────────────────────────────────┬─┘
                 │                                              │
         ┌───────▼────────┐                          ┌──────────▼──────┐
         │  JSON-RPC 2.0  │                          │   REST 어댑터   │
         │  /mcp/rpc      │                          │   /api/**       │
         └───────┬────────┘                          └──────────┬──────┘
                 │                                              │
                 └──────────────────┬───────────────────────────┘
                                    │
                    ┌───────────────▼────────────────┐
                    │  RestToJsonRpcConverter        │
                    │  (프로토콜 어댑터)             │
                    └───────────────┬────────────────┘
                                    │
                    ┌───────────────▼────────────────┐
                    │  JsonRpcDispatcher             │
                    │  (핵심 비즈니스 로직)          │
                    └───────────────┬────────────────┘
                                    │
        ┌───────────────────────────┼───────────────────────────┐
        │                           │                           │
        ▼                           ▼                           ▼
┌──────────────────┐      ┌──────────────────┐      ┌──────────────────┐
│ 검증             │      │ 인증             │      │ 킬 스위치         │
│ 파이프라인       │      │ & 정책           │      │ 강제             │
└──────────────────┘      └──────────────────┘      └──────────────────┘
        │                           │                           │
        └───────────────────────────┼───────────────────────────┘
                                    │
                    ┌───────────────▼────────────────┐
                    │  JsonRpcHandler                │
                    │  (도구 구현)                   │
                    └───────────────┬────────────────┘
                                    │
        ┌───────────────────────────┼───────────────────────────┐
        │                           │                           │
        ▼                           ▼                           ▼
┌──────────────────┐      ┌──────────────────┐      ┌──────────────────┐
│ 데이터 마스킹    │      │ 비동기 감사      │      │ 응답             │
│ 서비스           │      │ 로깅             │      │ 직렬화           │
└──────────────────┘      └──────────────────┘      └──────────────────┘
        │                           │                           │
        └───────────────────────────┼───────────────────────────┘
                                    │
                    ┌───────────────▼────────────────┐
                    │  JsonRpcResponse               │
                    │  (마스킹 & 감사됨)             │
                    └────────────────────────────────┘
```

## 요청 처리 파이프라인

### 단계 1: 프로토콜 파싱

```java
// 입력: 원본 HTTP 요청
POST /mcp/rpc
Content-Type: application/json

{
  "jsonrpc": "2.0",
  "method": "api.ifrs17.loss_projection",
  "params": {...},
  "id": "uuid-v7",
  "meta": {...}
}

// 출력: JsonRpcRequest 객체
JsonRpcRequest request = objectMapper.readValue(body, JsonRpcRequest.class);
```

**검증:**
- 유효한 JSON 구문
- 모든 필수 필드 존재
- 올바른 데이터 타입

### 단계 2: JSON-RPC 구조 검증

```java
request.validate();
// 확인:
// - jsonrpc == "2.0"
// - method가 null/공백이 아님
// - id가 null/공백이 아님 (UUID v7 형식)
// - meta가 null이 아님
```

**강제:**
- 배치 호출: 거부됨
- 알림 호출 (id 없음): 거부됨
- 잘못된 jsonrpc 버전: 거부됨

### 단계 3: 메타 필드 검증

```java
meta.validate();
// 확인:
// - user_id: null/공백이 아님
// - caller_id: null/공백이 아님
// - trace_id: null/공백이 아님 (UUID v7)
// - dept: null/공백이 아님
```

**목적:**
- 시스템 간 추적 상관
- 감사를 위한 사용자 식별
- 부서 수준 접근 제어

### 단계 4: 글로벌 킬 스위치 확인

```java
killSwitchService.validateGlobalNotDisabled();
// 쿼리: Redis 키 "kill_switch:global"
// 비활성화된 경우: McpException(TOOL_DISABLED) 발생
```

**효과:**
- 모든 요청을 즉시 차단
- 중요 사건에 사용됨
- 모든 인스턴스에서 동기화됨

### 단계 5: 도구 레지스트리 조회

```java
String toolId = extractToolId(request.getMethod()); // "ifrs17.loss_projection"
ToolRegistry tool = toolRegistryService.getToolRegistry(toolId);
// 조회 순서:
// 1. Redis 캐시 (키: "tools:ifrs17.loss_projection")
// 2. PostgreSQL 폴백
// 3. 캐시 미스: TOOL_NOT_FOUND 발생
```

**캐시 전략:**
- TTL: 60분
- 무효화: `repository.save()`를 통한 수동
- 폴백: PostgreSQL 쿼리

### 단계 6: 도구 킬 스위치 확인

```java
killSwitchService.validateToolNotDisabled(toolId);
// 쿼리: Redis 키 "kill_switch:tool:ifrs17.loss_projection"
// 비활성화된 경우: McpException(TOOL_DISABLED) 발생
```

**사용 사례:**
- 도구의 보안 취약점
- 유지보수 기간
- 데이터 품질 문제

### 단계 7: 인증 검증

```java
policyService.validatePolicy(request.getMeta(), toolId);
// 조회 순서:
// 1. Redis 캐시 (키: "policy:user_id:tool_id")
// 2. PostgreSQL 폴백
// 3. 캐시 미스: POLICY_DENIED 발생
```

**정책 확인:**
- 사용자가 도구에 대해 `allowed=true`를 가져야 함
- 데이터 수준이 사용자의 권한과 일치해야 함
- RBAC + ABAC 강제

### 단계 8: 입력 스키마 검증

```java
JsonNode schema = tool.getInputSchema();
schemaValidator.validate(request.getParams(), schema);
// 확인:
// - 필수 필드 존재
// - 필드 타입이 스키마와 일치
// - 추가 필드 없음 (선택사항)
```

**스키마 형식:**
```json
{
  "type": "object",
  "required": ["portfolio_value", "loss_rate"],
  "properties": {
    "portfolio_value": {"type": "number"},
    "loss_rate": {"type": "number"}
  }
}
```

### 단계 9: 핸들러 실행

```java
Object result = handler.handle(request);
// 도구별 구현에 위임
// 예: Ifrs17LossProjectionHandler
```

**오류 처리:**
- 예외 포착 및 오류 코드로 매핑
- 감사를 위한 지연 시간 추적
- 부분 실패 로깅

### 단계 10: 데이터 마스킹

```java
DataMaskingPolicy maskingPolicy = policyService.getDataMaskingPolicy(
    request.getMeta().getUserId(),
    toolId
);
Object maskedResult = maskingService.maskData(result, maskingPolicy);
// 열 수준 마스킹 적용:
// - HASH: SHA-256 해시
// - REDACT: ***REDACTED***
// - PARTIAL: 25% 표시, 나머지 마스킹
```

**마스킹 규칙:**
- 핸들러 실행 후 적용됨
- 사용자별 역할 기반
- 핸들러에 투명함

### 단계 11: 비동기 감사 로깅

```java
auditService.logCall(
    request.getMeta(),
    toolId,
    request.getMethod(),
    request.getParams(),
    resultCode,
    latencyMs,
    errorMessage
);
// @Async를 통한 비동기 실행
// Elasticsearch로 전송
// 논블로킹
```

**감사 로그 필드:**
- trace_id: 상관 ID
- user_id: 사용자 식별자
- tool_id: 호출된 도구
- params_hash: 매개변수의 SHA-256 (원본 아님)
- result_code: SUCCESS 또는 오류 코드
- latency_ms: 요청 지속 시간
- timestamp: Unix 밀리초
- error_message: 실패한 경우

### 단계 12: 응답 직렬화

```java
JsonRpcResponse response = JsonRpcResponse.success(request.getId(), maskedResult);
// 반환:
// {
//   "jsonrpc": "2.0",
//   "result": {...마스킹됨...},
//   "id": "uuid-v7"
// }
```

## 오류 처리 전략

### 예외 매핑

```
McpException
├── INVALID_PARAMS (400)
│   └── JSON 파싱, 스키마 검증, 메타 검증
├── POLICY_DENIED (403)
│   └── 인증 실패, 사용자 미허가
├── DATA_NOT_FOUND (404)
│   └── 도구를 찾을 수 없음, 리소스를 찾을 수 없음
├── TOOL_DISABLED (503)
│   └── 킬 스위치 활성화, 도구 비활성화
├── MCP_TIMEOUT (504)
│   └── 요청 시간 초과
├── UNAUTHORIZED (401)
│   └── JWT 검증 실패
└── MCP_INTERNAL_ERROR (500)
    └── 예상치 못한 서버 오류
```

### 오류 응답 형식

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

### 디스패처의 오류 처리

```java
try {
    // 검증 파이프라인
    request.validate();
    killSwitchService.validateGlobalNotDisabled();
    // ... 더 많은 검증 ...
    
    // 실행
    Object result = handler.handle(request);
    
    // 마스킹 & 감사
    Object masked = maskingService.maskData(result, policy);
    auditService.logCall(..., "SUCCESS", ...);
    
    return JsonRpcResponse.success(request.getId(), masked);
    
} catch (McpException e) {
    auditService.logCall(..., e.getCode(), e.getMessage());
    return JsonRpcResponse.error(request.getId(), e.getCode(), e.getMessage(), e.isRetryable());
    
} catch (IllegalArgumentException e) {
    auditService.logCall(..., "INVALID_PARAMS", e.getMessage());
    return JsonRpcResponse.error(request.getId(), "INVALID_PARAMS", e.getMessage(), false);
    
} catch (Exception e) {
    log.error("Unexpected error", e);
    auditService.logCall(..., "MCP_INTERNAL_ERROR", e.getMessage());
    return JsonRpcResponse.error(request.getId(), "MCP_INTERNAL_ERROR", "Internal server error", false);
}
```

## 데이터 흐름: REST 어댑터

### REST 요청 변환

```
REST 요청
├── 경로: /api/ifrs17/loss-projection
├── 헤더:
│   ├── Authorization: Bearer <JWT>
│   ├── X-Client-Id: agent-001
│   ├── X-Trace-Id: trace-uuid
│   └── X-Dept: RISK
└── 본문: {"portfolio_value": 1000000, ...}
         │
         ▼
RestToJsonRpcConverter
├── JWT에서 user_id 추출
├── X-Client-Id에서 caller_id 추출
├── X-Trace-Id에서 trace_id 추출
├── X-Dept에서 dept 추출
├── 경로를 메서드로 변환: "api.ifrs17.loss_projection"
├── id에 UUID v7 생성
└── JsonRpcRequest 빌드
         │
         ▼
JsonRpcDispatcher (JSON-RPC 경로와 동일)
         │
         ▼
REST 응답
└── 본문: {"field1": "value1", ...} (결과만, 래퍼 없음)
```

### REST 오류 응답

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

## 캐싱 전략

### 도구 레지스트리 캐시

```
키: tools:{tool_id}
TTL: 60분
무효화: repository.save()를 통한 수동
폴백: PostgreSQL 쿼리

예:
redis> GET tools:ifrs17.loss_projection
{
  "toolId": "ifrs17.loss_projection",
  "toolName": "IFRS17 Loss Projection",
  "version": "1.0.0",
  "status": "ACTIVE",
  "inputSchema": {...}
}
```

### 정책 캐시

```
키: policy:{user_id}:{tool_id}
TTL: 30분
무효화: repository.savePolicy()를 통한 수동
폴백: PostgreSQL 쿼리

예:
redis> GET policy:user@company.com:ifrs17.loss_projection
{
  "userId": "user@company.com",
  "toolId": "ifrs17.loss_projection",
  "allowed": true,
  "dataLevel": "CONFIDENTIAL"
}
```

### 마스킹 정책 캐시

```
키: masking:{user_id}:{tool_id}
TTL: 30분
무효화: repository.saveMaskingPolicy()를 통한 수동
폴백: PostgreSQL 쿼리

예:
redis> GET masking:user@company.com:ifrs17.loss_projection
{
  "userId": "user@company.com",
  "toolId": "ifrs17.loss_projection",
  "columnMasks": {
    "portfolio_value": "PARTIAL",
    "loss_rate": "HASH"
  },
  "dataLevel": "CONFIDENTIAL"
}
```

### 킬 스위치 캐시

```
키: kill_switch:tool:{tool_id}
키: kill_switch:global
TTL: 없음 (지워질 때까지 영구)
무효화: killSwitchService.enableTool()를 통한 수동

예:
redis> GET kill_switch:tool:ifrs17.loss_projection
{
  "targetId": "ifrs17.loss_projection",
  "disabled": true,
  "reason": "Security vulnerability detected",
  "disabledAt": 1702000000000
}
```

## 감사 로깅 아키텍처

### 비동기 로깅 흐름

```
JsonRpcDispatcher
├── 시작 시간 기록
├── 요청 실행
├── 종료 시간 기록
├── 지연 시간 계산
└── auditService.logCall() 호출 (비동기)
         │
         ▼
@Async auditService.logCall()
├── 매개변수 해시 (SHA-256)
├── AuditLog 객체 빌드
└── repository.save() 호출 (비동기)
         │
         ▼
ElasticsearchAuditRepository
├── JSON으로 직렬화
└── Elasticsearch에 인덱싱
         │
         ▼
Elasticsearch 인덱스: mcp-audit
└── 검색 가능한 감사 추적
```

### 감사 로그 인덱싱

```
인덱스: mcp-audit
매핑:
{
  "properties": {
    "trace_id": {"type": "keyword"},
    "user_id": {"type": "keyword"},
    "tool_id": {"type": "keyword"},
    "result_code": {"type": "keyword"},
    "latency_ms": {"type": "long"},
    "timestamp": {"type": "date"},
    "dept": {"type": "keyword"}
  }
}
```

### 감사 쿼리

```
# 사용자별 모든 호출
GET mcp-audit/_search
{
  "query": {
    "term": {"user_id": "user@company.com"}
  }
}

# 실패한 호출
GET mcp-audit/_search
{
  "query": {
    "term": {"result_code": "POLICY_DENIED"}
  }
}

# 느린 호출 (>1000ms)
GET mcp-audit/_search
{
  "query": {
    "range": {"latency_ms": {"gte": 1000}}
  }
}
```

## 보안 계층

### 계층 1: 전송 보안

- HTTPS/TLS 모든 통신
- 인증서 고정 (선택사항)
- 서비스 간 상호 TLS

### 계층 2: 인증

- OAuth2 JWT 검증
- JWKS 엔드포인트 검증
- 토큰 만료 확인
- 서명 검증 (RSA)

### 계층 3: 인증

- RBAC: 역할 기반 도구 접근
- ABAC: 속성 기반 데이터 수준
- 정책 캐싱 (폴백 포함)
- 기본 거부

### 계층 4: 데이터 보호

- 열 수준 마스킹
- SHA-256 해싱
- 부분 편집
- 역할 기반 가시성

### 계층 5: 감사 & 준수

- 서버 측 감사 로깅
- 매개변수 해싱 (원본 값 아님)
- 추적 상관
- Elasticsearch 인덱싱

### 계층 6: 운영 안전

- 킬 스위치 강제
- 도구 수준 비활성화
- 글로벌 비활성화
- 즉시 효과

## 성능 특성

### 지연 시간 분석 (일반적)

```
JSON 파싱:           1-2ms
검증:                2-5ms
캐시 조회:           1-3ms
인증:                2-4ms
스키마 검증:         1-2ms
핸들러 실행:         50-500ms (다양함)
데이터 마스킹:       1-3ms
감사 로깅:           <1ms (비동기)
직렬화:              1-2ms
─────────────────────────────
합계 (p50):          60-520ms
합계 (p99):          100-1000ms
```

### 처리량

- 단일 인스턴스: 100-500 req/s (핸들러에 따라 다름)
- 수평 확장: 인스턴스와 선형
- Redis 병목: 가능성 낮음 (높은 처리량)
- PostgreSQL 병목: 규모에서 가능 (연결 풀링 사용)

### 리소스 사용

- 메모리: ~500MB 기본 + 핸들러별
- CPU: 낮음 (대부분 I/O 바운드)
- 네트워크: 페이로드 크기에 따라 다름
- 디스크: Elasticsearch 감사 로그 저장소

## 배포 토폴로지

### 단일 인스턴스

```
┌─────────────────────────────────┐
│  MCP Spoke Server               │
│  ├── Spring Boot 3.x            │
│  ├── JSON-RPC 디스패처          │
│  └── REST 어댑터                │
└────────┬────────────────────────┘
         │
    ┌────┴────┬────────┬──────────┐
    │          │        │          │
    ▼          ▼        ▼          ▼
PostgreSQL  Redis  Elasticsearch  OAuth2
```

### 고가용성

```
┌──────────────────────────────────────────────────┐
│  로드 밸런서 (라운드 로빈)                        │
└────────┬──────────────────────────────────────┬──┘
         │                                      │
    ┌────▼────────┐                    ┌───────▼────┐
    │ MCP Spoke 1  │                    │ MCP Spoke 2 │
    └────┬────────┘                    └───────┬────┘
         │                                      │
    ┌────┴──────────────────────────────────────┴──┐
    │                                              │
    ▼                                              ▼
PostgreSQL (Primary)                         Redis 클러스터
    │                                              │
    ▼                                              ▼
PostgreSQL (Replica)                    Elasticsearch 클러스터
```

## 테스트 전략

### 단위 테스트

- 검증 파이프라인
- 오류 매핑
- 데이터 마스킹 로직
- 감사 로깅

### 통합 테스트

- JSON-RPC 디스패처
- REST 어댑터
- 데이터베이스 저장소
- 캐시 동작

### 엔드투엔드 테스트

- 전체 요청 흐름
- 인증 강제
- 킬 스위치 동작
- 감사 로깅

### 부하 테스트

- 처리량: 1000 req/s
- 지연 시간: p99 < 1000ms
- 캐시 히트율: >90%
- 오류율: <0.1%

## 모니터링 & 관찰성

### 메트릭

- 요청 수 (도구별, 사용자별)
- 요청 지연 시간 (p50, p95, p99)
- 오류율 (오류 코드별)
- 캐시 히트율
- 인증 거부
- 킬 스위치 활성화

### 로그

- 요청/응답 (디버그 수준)
- 오류 (오류 수준)
- 인증 결정 (정보 수준)
- 킬 스위치 이벤트 (경고 수준)

### 추적

- trace_id를 통한 분산 추적
- 서비스 간 상관
- 지연 시간 분석
- 오류 전파

### 알림

- 오류율 > 1%
- 지연 시간 p99 > 2000ms
- 킬 스위치 활성화
- 캐시 미스율 > 10%
- 인증 거부 급증
