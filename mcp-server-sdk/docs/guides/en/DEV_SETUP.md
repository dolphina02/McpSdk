# MCP Spoke Server SDK - 개발 환경 설정 가이드

## 개발 vs 프로덕션 모드

### 개발 모드 (Dev Mode)

개발 모드에서는 JWT 검증을 완전히 우회하고 모든 요청을 허용합니다.

**활성화:**
```bash
./gradlew :sample-spoke-app:bootRun --args='--spring.profiles.active=dev'
```

**또는 application.yml에서:**
```yaml
mcp:
  security:
    dev-mode: true
```

**특징:**
- ✅ JWT 검증 우회
- ✅ 모든 요청 자동 허용
- ✅ 자동 토큰 생성 엔드포인트 활성화
- ✅ 데이터베이스 자동 생성 (create-drop)
- ✅ 상세 로깅 활성화
- ✅ 빠른 개발 사이클

### 프로덕션 모드 (Production Mode)

프로덕션 모드에서는 OAuth2 JWT 검증을 활성화합니다.

**활성화:**
```bash
./gradlew :sample-spoke-app:bootRun --args='--spring.profiles.active=prod'
```

**또는 application.yml에서:**
```yaml
mcp:
  security:
    dev-mode: false  # 또는 생략 (기본값)
```

**특징:**
- ✅ OAuth2 JWT 검증 활성화
- ✅ JWKS 엔드포인트 검증
- ✅ 토큰 만료 확인
- ✅ 서명 검증
- ✅ 데이터베이스 검증 모드 (validate)
- ✅ 프로덕션 로깅

## 개발 환경 JWT 토큰 생성

### 1. 기본 토큰 생성

```bash
curl http://localhost:8080/dev/token
```

**응답:**
```json
{
  "token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "user_id": "user@company.com",
  "dept": "RISK",
  "roles": ["ADMIN", "USER"],
  "expires_in": 86400,
  "message": "Development token - Valid for 24 hours"
}
```

### 2. 커스텀 토큰 생성

```bash
# 특정 사용자, 부서, 역할로 토큰 생성
curl -X POST 'http://localhost:8080/dev/token?user_id=alice@company.com&dept=COMPLIANCE&roles=USER,AUDITOR'
```

**매개변수:**
- `user_id`: 사용자 ID (기본값: user@company.com)
- `dept`: 부서 (기본값: RISK)
- `roles`: 쉼표로 구분된 역할 (기본값: ADMIN,USER)

### 3. 토큰 저장 및 사용

```bash
# 토큰을 변수에 저장
TOKEN=$(curl -s http://localhost:8080/dev/token | jq -r '.token')

# 요청에서 사용
curl -X POST http://localhost:8080/mcp/rpc \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{...}'
```

### 4. 도움말 보기

```bash
curl http://localhost:8080/dev/token/help
```

## 개발 환경 설정 파일

### application-dev.yml

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/mcp_db
    username: postgres
    password: postgres
  
  jpa:
    hibernate:
      ddl-auto: create-drop  # 자동 테이블 생성/삭제
  
  redis:
    host: localhost
    port: 6379

logging:
  level:
    com.financial.mcp: DEBUG
    org.springframework.security: DEBUG

mcp:
  security:
    dev-mode: true  # JWT 검증 우회
```

### application-prod.yml

```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
  
  jpa:
    hibernate:
      ddl-auto: validate  # 스키마 검증만
  
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI}
          jwk-set-uri: ${SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI}

logging:
  level:
    com.financial.mcp: INFO

mcp:
  security:
    dev-mode: false  # JWT 검증 활성화
```

## 개발 환경 테스트 워크플로우

### 1. 애플리케이션 시작

```bash
# 개발 모드로 시작
./gradlew :sample-spoke-app:bootRun --args='--spring.profiles.active=dev'
```

### 2. 토큰 생성

```bash
# 기본 토큰 생성
TOKEN=$(curl -s http://localhost:8080/dev/token | jq -r '.token')
echo "Token: $TOKEN"
```

### 3. API 테스트

```bash
# JSON-RPC 호출
curl -X POST http://localhost:8080/mcp/rpc \
  -H "Authorization: Bearer $TOKEN" \
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

### 4. 다양한 사용자로 테스트

```bash
# 다른 사용자로 토큰 생성
TOKEN=$(curl -s -X POST 'http://localhost:8080/dev/token?user_id=bob@company.com&dept=COMPLIANCE' | jq -r '.token')

# 해당 사용자로 API 호출
curl -X POST http://localhost:8080/mcp/rpc \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{...}'
```

## 개발 환경 보안 설정 상세

### DevOAuth2SecurityConfig

개발 모드에서 활성화되는 설정:

```java
@ConditionalOnProperty(name = "mcp.security.dev-mode", havingValue = "true")
public class DevOAuth2SecurityConfig {
    // 모든 요청 허용
    // JWT 검증 우회
    // CSRF 비활성화
}
```

### DevJwtTokenProvider

개발 환경용 JWT 토큰 생성기:

```java
@ConditionalOnProperty(name = "mcp.security.dev-mode", havingValue = "true")
public class DevJwtTokenProvider {
    // RSA 키 쌍 자동 생성
    // JWT 토큰 생성
    // 24시간 유효기간
}
```

### DevTokenController

토큰 생성 엔드포인트:

```java
@ConditionalOnProperty(name = "mcp.security.dev-mode", havingValue = "true")
@RestController
@RequestMapping("/dev/token")
public class DevTokenController {
    // GET /dev/token - 기본 토큰
    // POST /dev/token - 커스텀 토큰
    // GET /dev/token/help - 도움말
}
```

## 프로덕션 환경 설정

### OAuth2 제공자 설정

프로덕션에서는 실제 OAuth2 제공자를 사용해야 합니다:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://your-auth-provider.com
          jwk-set-uri: https://your-auth-provider.com/.well-known/jwks.json
```

### 지원되는 OAuth2 제공자

- **Auth0**: https://auth0.com
- **Okta**: https://www.okta.com
- **Azure AD**: https://azure.microsoft.com/en-us/services/active-directory/
- **Google Cloud Identity**: https://cloud.google.com/identity
- **Keycloak**: https://www.keycloak.org

### 환경 변수 설정

```bash
export SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI=https://your-auth-provider.com
export SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI=https://your-auth-provider.com/.well-known/jwks.json
```

## 개발 vs 프로덕션 비교

| 기능 | 개발 모드 | 프로덕션 모드 |
|------|---------|------------|
| JWT 검증 | ❌ 우회 | ✅ 활성화 |
| 토큰 생성 엔드포인트 | ✅ 활성화 | ❌ 비활성화 |
| 모든 요청 허용 | ✅ 예 | ❌ 아니오 |
| 데이터베이스 자동 생성 | ✅ create-drop | ❌ validate |
| 상세 로깅 | ✅ DEBUG | ❌ INFO |
| CSRF 보호 | ❌ 비활성화 | ✅ 활성화 |
| 보안 헤더 | ❌ 없음 | ✅ 있음 |

## 문제 해결

### 토큰 생성 엔드포인트가 없음

**원인:** 개발 모드가 활성화되지 않음

**해결책:**
```bash
# 개발 프로필로 실행
./gradlew :sample-spoke-app:bootRun --args='--spring.profiles.active=dev'
```

### JWT 검증 오류

**원인:** 프로덕션 모드에서 유효하지 않은 토큰 사용

**해결책:**
1. 개발 모드로 전환하거나
2. 유효한 OAuth2 토큰 사용

### 토큰이 만료됨

**원인:** 개발 토큰의 24시간 유효기간 초과

**해결책:**
```bash
# 새 토큰 생성
TOKEN=$(curl -s http://localhost:8080/dev/token | jq -r '.token')
```

## 보안 주의사항

⚠️ **개발 모드는 개발 환경에서만 사용하세요!**

- 개발 모드에서는 모든 보안 검사가 우회됩니다
- 프로덕션 환경에서 절대 사용하지 마세요
- 개발 토큰은 실제 사용자 인증을 제공하지 않습니다
- 프로덕션 배포 전에 `mcp.security.dev-mode=false`로 설정하세요

## 다음 단계

1. 개발 환경에서 API 테스트
2. 자신의 도구 구현
3. 프로덕션 OAuth2 제공자 설정
4. 프로덕션 배포
