# MCP Spoke Server SDK - 빌드 환경 체크

## 시스템 요구사항

### 필수 요구사항
- ✅ Java 17+ (현재: Java 17.0.12 LTS)
- ✅ Git
- ⚠️ Gradle 8.5+ (Gradle Wrapper 사용)

## 빌드 환경 설정

### 1. Gradle Wrapper 설정

프로젝트에 Gradle Wrapper 설정이 포함되어 있습니다. 

**첫 실행 시 Gradle 자동 다운로드:**

**Windows:**
```bash
# 첫 번째 실행 시 Gradle 8.5 자동 다운로드
gradlew.bat build
```

**Linux/Mac:**
```bash
# 첫 번째 실행 시 Gradle 8.5 자동 다운로드
./gradlew build
```

### 2. Gradle Wrapper 수동 설정 (선택사항)

만약 자동 다운로드가 실패하면 수동으로 설정할 수 있습니다:

**Windows:**
```bash
# PowerShell 스크립트 실행
.\setup-gradle.ps1
```

**Linux/Mac:**
```bash
# Bash 스크립트 실행
bash setup-gradle.sh
```

### 3. 첫 빌드 시 필요한 작업

첫 빌드 시 Gradle Wrapper가 자동으로 Gradle 8.5를 다운로드합니다.

```bash
# Windows
gradlew.bat --version

# Linux/Mac
./gradlew --version
```

### 3. 프로젝트 구조 확인

```
mcp-server-sdk/
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── gradlew.bat              (Windows용 Gradle Wrapper)
├── gradlew                  (Linux/Mac용 Gradle Wrapper)
├── build.gradle.kts         (루트 빌드 설정)
├── settings.gradle.kts      (프로젝트 설정)
└── [모듈들]
```

## 빌드 명령어

### 전체 프로젝트 빌드

```bash
# Windows
gradlew.bat clean build

# Linux/Mac
./gradlew clean build
```

### 특정 모듈만 빌드

```bash
# mcp-core 모듈만 빌드
gradlew.bat :mcp-core:build

# sample-spoke-app만 빌드
gradlew.bat :sample-spoke-app:build
```

### 샘플 애플리케이션 실행

```bash
# 개발 모드로 실행
gradlew.bat :sample-spoke-app:bootRun --args='--spring.profiles.active=dev'

# 프로덕션 모드로 실행
gradlew.bat :sample-spoke-app:bootRun --args='--spring.profiles.active=prod'
```

### JAR 파일 생성

```bash
# 샘플 앱 JAR 생성
gradlew.bat :sample-spoke-app:bootJar

# 생성된 JAR 위치
# sample-spoke-app/build/libs/sample-spoke-app-1.0.0.jar
```

## 의존성 확인

### 주요 의존성

- **Spring Boot**: 3.2.0
- **Spring Security**: 3.2.0
- **PostgreSQL Driver**: 42.7.1
- **Redis (Lettuce)**: 최신
- **Elasticsearch Java Client**: 8.11.0
- **Nimbus JOSE JWT**: 9.37.3
- **Lombok**: 최신
- **JSON-RPC4J**: 1.5.3

### 의존성 확인

```bash
# 의존성 트리 확인
gradlew.bat dependencies

# 특정 모듈의 의존성 확인
gradlew.bat :mcp-core:dependencies
```

## 빌드 문제 해결

### 문제 1: "gradle: 명령을 찾을 수 없음"

**해결책:**
```bash
# Gradle Wrapper 사용
gradlew.bat build  # Windows
./gradlew build    # Linux/Mac
```

### 문제 2: Java 버전 불일치

**확인:**
```bash
java -version
```

**필요한 버전:** Java 17+

**해결책:**
- Java 17 이상 설치
- JAVA_HOME 환경 변수 설정

### 문제 3: 메모리 부족

**해결책:**
```bash
# 메모리 증가
set GRADLE_OPTS=-Xmx2g  # Windows
export GRADLE_OPTS=-Xmx2g  # Linux/Mac
```

### 문제 4: 네트워크 문제 (의존성 다운로드 실패)

**해결책:**
```bash
# 캐시 삭제 후 재시도
gradlew.bat clean build --refresh-dependencies
```

## 빌드 성공 확인

### 빌드 완료 메시지

```
BUILD SUCCESSFUL in Xs
```

### 생성된 아티팩트

```
mcp-core/build/libs/mcp-core-1.0.0.jar
mcp-rest-adapter/build/libs/mcp-rest-adapter-1.0.0.jar
mcp-security/build/libs/mcp-security-1.0.0.jar
mcp-redis/build/libs/mcp-redis-1.0.0.jar
mcp-postgres/build/libs/mcp-postgres-1.0.0.jar
mcp-elasticsearch/build/libs/mcp-elasticsearch-1.0.0.jar
mcp-autoconfigure/build/libs/mcp-autoconfigure-1.0.0.jar
sample-spoke-app/build/libs/sample-spoke-app-1.0.0.jar
```

## IDE 설정

### IntelliJ IDEA

1. File → Open → mcp-server-sdk 폴더 선택
2. Gradle 자동 감지 및 설정
3. Build → Build Project

### Eclipse

1. File → Import → Gradle → Existing Gradle Project
2. mcp-server-sdk 폴더 선택
3. Project → Build Project

### VS Code

1. Extension: Gradle for Java 설치
2. Gradle 자동 감지
3. Gradle: Build 실행

## 다음 단계

1. ✅ 빌드 환경 확인 완료
2. 📦 프로젝트 빌드
3. 🚀 샘플 애플리케이션 실행
4. 🧪 API 테스트

## 빌드 체크리스트

- [ ] Java 17+ 설치 확인
- [ ] Gradle Wrapper 설정 확인
- [ ] 첫 빌드 실행
- [ ] 모든 모듈 빌드 성공
- [ ] 샘플 앱 실행 테스트
- [ ] API 엔드포인트 테스트
