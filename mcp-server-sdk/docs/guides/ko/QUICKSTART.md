# MCP Spoke Server SDK - 빠른 시작 가이드

## 필수 조건

- Java 17+
- Gradle 8+
- Docker & Docker Compose (로컬 서비스용)
- Git

## 로컬 개발 설정

### 1. 인프라 서비스 시작

```bash
# docker-compose.yml 생성
cat > docker-compose.yml << 'EOF'
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: mcp_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.11.0
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
    ports:
      - "9200:9200"
    volumes:
      - elasticsearch_data:/usr/share/elasticsearch/data

volumes:
  postgres_data:
  elasticsearch_data:
EOF

# 서비스 시작
docker-compose up -d
```

### 2. 데이터베이스 초기화

```bash
# PostgreSQL에 연결
psql -h localhost -U postgres -d mcp_db

# 테이블 생성
CREATE TABLE tool_registry (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tool_id VARCHAR(255) UNIQUE NOT NULL,
    tool_name VARCHAR(255) NOT NULL,
    version VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    input_schema JSONB,
    description TEXT,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL
);

CREATE TABLE tool_policy (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id VARCHAR(255) NOT NULL,
    tool_id VARCHAR(255) NOT NULL,
    allowed BOOLEAN NOT NULL,
    data_level VARCHAR(50) NOT NULL,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    UNIQUE(user_id, tool_id)
);

CREATE TABLE data_masking_policy (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id VARCHAR(255) NOT NULL,
    tool_id VARCHAR(255) NOT NULL,
    column_masks JSONB,
    data_level VARCHAR(50) NOT NULL,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    UNIQUE(user_id, tool_id)
);

\q
```

### 3. 프로젝트 빌드

```bash
cd mcp-server-sdk

# 모든 모듈 빌드
./gradlew clean build

# 샘플 앱만 빌드
./gradlew :sample-spoke-app:bootJar
```

### 4. 샘플 애플리케이션 실행

```bash
# 개발 프로필로 실행 (JWT 검증 비활성화, 모든 요청 허용)
./gradlew :sample-spoke-app:bootRun --args='--spring.profiles.active=dev'
```

애플리케이션이 `http://localhost:8080`에서 시작됩니다.

## API 테스트

### 1. 테스트 JWT 토큰 생성 (개발 모드)

개발 모드는 자동 JWT 토큰 생성 엔드포인트를 제공합니다:

```bash
# 기본 테스트 토큰 가져오기
curl http://localhost:8080/dev/token

# 응답:
# {
#   "token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
#   "type": "Bearer",
#   "user_id": "user@company.com",
#   "dept": "RISK",
#   "roles": ["ADMIN", "USER"],
#   "expires_in": 86400,
#   "message": "Development token - Valid for 24 hours"
# }
```

### 1a. 커스텀 토큰 생성

```bash
# 특정 사용자를 위한 토큰 생성
curl -X POST 'http://localhost:8080/dev/token?user_id=alice@company.com&dept=COMPLIANCE&roles=USER'

# 응답:
# {
#   "token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
#   "type": "Bearer",
#   "user_id": "alice@company.com",
#   "dept": "COMPLIANCE",
#   "roles": ["USER"],
#   "expires_in": 86400
# }
```

### 1b. 토큰 생성 도움말 보기

```bash
curl http://localhost:8080/dev/token/help
```

### 1c. 토큰을 변수에 저장

```bash
# 토큰을 가져와서 변수에 저장
TOKEN=$(curl -s http://localhost:8080/dev/token | jq -r '.token')
echo "Token: $TOKEN"
```

### 2. JSON-RPC 엔드포인트 테스트

```bash
# 먼저 토큰 가져오기
TOKEN=$(curl -s http://localhost:8080/dev/token | jq -r '.token')

# JSON-RPC 엔드포인트 호출
curl -X POST http://localhost:8080/mcp/rpc \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "jsonrpc": "2.0",
    "method": "api.ifrs17.loss_projection",
    "params": {
      "portfolio_value": 1000000,
      "loss_rate": 0.05,
      "projection_years": 3
    },
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "meta": {
      "user_id": "user@company.com",
      "caller_id": "agent-001",
      "trace_id": "trace-uuid-v7",
      "dept": "RISK"
    }
  }'
```

또는 토큰을 저장하지 않고:

```bash
curl -X POST http://localhost:8080/mcp/rpc \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $(curl -s http://localhost:8080/dev/token | jq -r '.token')" \
  -d '{
    "jsonrpc": "2.0",
    "method": "api.ifrs17.loss_projection",
    "params": {
      "portfolio_value": 1000000,
      "loss_rate": 0.05,
      "projection_years": 3
    },
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "meta": {
      "user_id": "user@company.com",
      "caller_id": "agent-001",
      "trace_id": "trace-uuid-v7",
      "dept": "RISK"
    }
  }'
```

**예상 응답:**
```json
{
  "jsonrpc": "2.0",
  "result": {
    "portfolio_value": 1000000,
    "loss_rate": 0.05,
    "projection_years": 3,
    "projected_loss": 150000,
    "remaining_value": 850000,
    "confidence_level": 0.95
  },
  "id": "550e8400-e29b-41d4-a716-446655440000"
}
```

### 3. REST 엔드포인트 테스트

```bash
# 먼저 토큰 가져오기
TOKEN=$(curl -s http://localhost:8080/dev/token | jq -r '.token')

# REST 엔드포인트 호출
curl -X POST http://localhost:8080/api/ifrs17/loss-projection \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Client-Id: agent-001" \
  -H "X-Trace-Id: trace-uuid-v7" \
  -H "X-Dept: RISK" \
  -d '{
    "portfolio_value": 1000000,
    "loss_rate": 0.05,
    "projection_years": 3
  }'
```

또는 한 줄로:

```bash
curl -X POST http://localhost:8080/api/ifrs17/loss-projection \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $(curl -s http://localhost:8080/dev/token | jq -r '.token')" \
  -H "X-Client-Id: agent-001" \
  -H "X-Trace-Id: trace-uuid-v7" \
  -H "X-Dept: RISK" \
  -d '{
    "portfolio_value": 1000000,
    "loss_rate": 0.05,
    "projection_years": 3
  }'
```

**예상 응답:**
```json
{
  "portfolio_value": 1000000,
  "loss_rate": 0.05,
  "projection_years": 3,
  "projected_loss": 150000,
  "remaining_value": 850000,
  "confidence_level": 0.95
}
```

### 4. 인증 거부 테스트

```bash
# 먼저 접근을 거부하는 정책 생성
psql -h localhost -U postgres -d mcp_db << 'EOF'
INSERT INTO tool_policy (user_id, tool_id, allowed, data_level, created_at, updated_at)
VALUES ('denied_user@company.com', 'ifrs17.loss_projection', false, 'PUBLIC', 
        EXTRACT(EPOCH FROM NOW())::BIGINT * 1000, 
        EXTRACT(EPOCH FROM NOW())::BIGINT * 1000);
EOF

# 거부된 사용자로 호출
curl -X POST http://localhost:8080/mcp/rpc \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "method": "api.ifrs17.loss_projection",
    "params": {"portfolio_value": 1000000, "loss_rate": 0.05, "projection_years": 3},
    "id": "test-id",
    "meta": {
      "user_id": "denied_user@company.com",
      "caller_id": "agent-001",
      "trace_id": "trace-uuid",
      "dept": "RISK"
    }
  }'
```

**예상 응답:**
```json
{
  "jsonrpc": "2.0",
  "error": {
    "code": "POLICY_DENIED",
    "message": "User not authorized to access tool: ifrs17.loss_projection",
    "retryable": false
  },
  "id": "test-id"
}
```

### 5. 킬 스위치 테스트

```bash
# 도구 비활성화
curl -X POST http://localhost:8080/admin/killswitch/disable \
  -H "Content-Type: application/json" \
  -d '{
    "tool_id": "ifrs17.loss_projection",
    "reason": "Security vulnerability"
  }'

# 비활성화된 도구 호출 시도
curl -X POST http://localhost:8080/mcp/rpc \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "method": "api.ifrs17.loss_projection",
    "params": {"portfolio_value": 1000000, "loss_rate": 0.05, "projection_years": 3},
    "id": "test-id",
    "meta": {
      "user_id": "user@company.com",
      "caller_id": "agent-001",
      "trace_id": "trace-uuid",
      "dept": "RISK"
    }
  }'
```

**예상 응답:**
```json
{
  "jsonrpc": "2.0",
  "error": {
    "code": "TOOL_DISABLED",
    "message": "Tool is disabled by kill switch: ifrs17.loss_projection",
    "retryable": false
  },
  "id": "test-id"
}
```

## 자신의 도구 구현

### 단계 1: 핸들러 생성

```java
// src/main/java/com/financial/mcp/sample/tool/MyToolHandler.java
package com.financial.mcp.sample.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.mcp.core.rpc.JsonRpcHandler;
import com.financial.mcp.core.rpc.JsonRpcRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MyToolHandler implements JsonRpcHandler {
    private final ObjectMapper objectMapper;

    @Override
    public Object handle(JsonRpcRequest request) {
        if ("api.my.namespace.my_tool".equals(request.getMethod())) {
            return handleMyTool(request.getParams());
        }
        throw new IllegalArgumentException("Unknown method: " + request.getMethod());
    }

    private Object handleMyTool(Object params) {
        Map<String, Object> paramsMap = objectMapper.convertValue(params, Map.class);
        
        // 비즈니스 로직
        String input = (String) paramsMap.get("input");
        
        Map<String, Object> result = new HashMap<>();
        result.put("input", input);
        result.put("output", "Processed: " + input);
        
        return result;
    }
}
```

### 단계 2: 도구 등록

```java
// src/main/java/com/financial/mcp/sample/config/MyToolRegistry.java
package com.financial.mcp.sample.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.mcp.core.registry.ToolRegistry;
import com.financial.mcp.core.registry.ToolRegistryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MyToolRegistry implements CommandLineRunner {
    private final ToolRegistryRepository repository;
    private final ObjectMapper objectMapper;

    @Override
    public void run(String... args) throws Exception {
        String schemaJson = """
                {
                  "type": "object",
                  "required": ["input"],
                  "properties": {
                    "input": {"type": "string"}
                  }
                }
                """;

        JsonNode schema = objectMapper.readTree(schemaJson);

        ToolRegistry tool = ToolRegistry.builder()
                .toolId("my.namespace.my_tool")
                .toolName("My Tool")
                .version("1.0.0")
                .status("ACTIVE")
                .inputSchema(schema)
                .description("My custom tool")
                .createdAt(System.currentTimeMillis())
                .updatedAt(System.currentTimeMillis())
                .build();

        repository.save(tool);
    }
}
```

### 단계 3: 접근 권한 부여

```bash
psql -h localhost -U postgres -d mcp_db << 'EOF'
INSERT INTO tool_policy (user_id, tool_id, allowed, data_level, created_at, updated_at)
VALUES ('user@company.com', 'my.namespace.my_tool', true, 'PUBLIC', 
        EXTRACT(EPOCH FROM NOW())::BIGINT * 1000, 
        EXTRACT(EPOCH FROM NOW())::BIGINT * 1000);
EOF
```

### 단계 4: 테스트

```bash
curl -X POST http://localhost:8080/mcp/rpc \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "method": "api.my.namespace.my_tool",
    "params": {"input": "hello"},
    "id": "test-id",
    "meta": {
      "user_id": "user@company.com",
      "caller_id": "agent-001",
      "trace_id": "trace-uuid",
      "dept": "RISK"
    }
  }'
```

## 감사 로그 보기

### Elasticsearch 쿼리

```bash
# 모든 로그
curl http://localhost:9200/mcp-audit/_search

# 특정 사용자의 로그
curl http://localhost:9200/mcp-audit/_search -d '{
  "query": {
    "term": {"user_id": "user@company.com"}
  }
}'

# 실패한 호출
curl http://localhost:9200/mcp-audit/_search -d '{
  "query": {
    "term": {"result_code": "POLICY_DENIED"}
  }
}'

# 느린 호출 (>500ms)
curl http://localhost:9200/mcp-audit/_search -d '{
  "query": {
    "range": {"latency_ms": {"gte": 500}}
  }
}'
```

## 문제 해결

### 도구를 찾을 수 없음

```
오류: TOOL_NOT_FOUND
```

**해결책:**
1. 도구가 등록되어 있는지 확인: `SELECT * FROM tool_registry WHERE tool_id = 'my.tool';`
2. Redis 캐시 확인: `redis-cli GET tools:my.tool`
3. 메서드 형식 확인: `api.namespace.tool_id`

### 인증 거부됨

```
오류: POLICY_DENIED
```

**해결책:**
1. 정책 확인: `SELECT * FROM tool_policy WHERE user_id = 'user@company.com' AND tool_id = 'my.tool';`
2. `allowed = true` 확인
3. Redis 캐시 지우기: `redis-cli DEL policy:user@company.com:my.tool`

### 잘못된 매개변수

```
오류: INVALID_PARAMS
```

**해결책:**
1. 스키마 확인: `SELECT input_schema FROM tool_registry WHERE tool_id = 'my.tool';`
2. 모든 필수 필드가 있는지 확인
3. 필드 타입이 스키마와 일치하는지 확인

### 킬 스위치 활성화됨

```
오류: TOOL_DISABLED
```

**해결책:**
1. 킬 스위치 확인: `redis-cli GET kill_switch:tool:my.tool`
2. 다시 활성화: `redis-cli DEL kill_switch:tool:my.tool`

## 다음 단계

1. [ARCHITECTURE_KO.md](ARCHITECTURE_KO.md) 읽기 (심층 분석)
2. [README_KO.md](README_KO.md) 읽기 (완전한 참조)
3. 위의 패턴을 따라 도구 구현
4. OAuth2 제공자 설정
5. Elasticsearch 감사 보존 정책 설정
6. 모니터링 및 알림 설정

## 프로덕션 체크리스트

- [ ] OAuth2 제공자 구성됨
- [ ] PostgreSQL 복제됨
- [ ] Redis 클러스터 배포됨
- [ ] Elasticsearch 클러스터 배포됨
- [ ] SSL/TLS 인증서 설치됨
- [ ] 모니터링 및 알림 구성됨
- [ ] 감사 로그 보존 정책 설정됨
- [ ] 백업 전략 구현됨
- [ ] 부하 테스트 완료됨
- [ ] 보안 감사 완료됨
