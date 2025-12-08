# 빌드 환경 체크 요약

## ✅ 현재 환경 상태

### 시스템 정보
- **OS**: Windows
- **Java**: 17.0.12 LTS ✅
- **Gradle**: Wrapper 설정됨 (8.5)

### 필수 요구사항 확인
- ✅ Java 17+ 설치됨
- ✅ Git 설치됨 (프로젝트 존재)
- ⚠️ Gradle: Wrapper 사용 (별도 설치 불필요)

## 🚀 빌드 시작하기

### 1단계: Gradle Wrapper 설정 (첫 실행 시만)

**Windows:**
```bash
cd mcp-server-sdk
.\setup-gradle.ps1
```

**Linux/Mac:**
```bash
cd mcp-server-sdk
bash setup-gradle.sh
```

### 2단계: 프로젝트 빌드

```bash
# 전체 프로젝트 빌드
gradlew.bat clean build

# 또는 특정 모듈만 빌드
gradlew.bat :sample-spoke-app:build
```

### 3단계: 샘플 애플리케이션 실행

```bash
# 개발 모드로 실행 (JWT 검증 우회)
gradlew.bat :sample-spoke-app:bootRun --args='--spring.profiles.active=dev'
```

### 4단계: API 테스트

```bash
# 토큰 생성
curl http://localhost:8080/dev/token

# API 호출
curl -X POST http://localhost:8080/mcp/rpc \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{...}'
```

## 📋 빌드 체크리스트

- [ ] Java 17+ 설치 확인: `java -version`
- [ ] Gradle Wrapper 설정: `.\setup-gradle.ps1` (Windows) 또는 `bash setup-gradle.sh` (Linux/Mac)
- [ ] 프로젝트 빌드: `gradlew.bat clean build`
- [ ] 샘플 앱 실행: `gradlew.bat :sample-spoke-app:bootRun --args='--spring.profiles.active=dev'`
- [ ] 토큰 생성 테스트: `curl http://localhost:8080/dev/token`
- [ ] API 호출 테스트

## 📚 상세 가이드

- **BUILD_CHECK.md** - 빌드 환경 상세 가이드
- **DEV_SETUP.md** - 개발 환경 설정 가이드
- **QUICKSTART.md** - 빠른 시작 가이드
- **ARCHITECTURE.md** - 시스템 아키텍처

## 🔧 문제 해결

### Gradle Wrapper 설정 실패

**해결책:**
1. 인터넷 연결 확인
2. 방화벽 설정 확인
3. 수동 다운로드:
   - https://services.gradle.org/distributions/gradle-8.5-bin.zip
   - 프로젝트 루트에 추출

### Java 버전 오류

**확인:**
```bash
java -version
```

**필요:** Java 17 이상

**해결책:**
- Java 17 LTS 설치
- JAVA_HOME 환경 변수 설정

### 빌드 메모리 부족

**해결책:**
```bash
# Windows
set GRADLE_OPTS=-Xmx2g

# Linux/Mac
export GRADLE_OPTS=-Xmx2g
```

## 📊 프로젝트 구조

```
mcp-server-sdk/
├── gradle/wrapper/              # Gradle Wrapper 설정
├── gradlew.bat                  # Windows Gradle Wrapper
├── gradlew                       # Linux/Mac Gradle Wrapper
├── setup-gradle.ps1             # Windows 설정 스크립트
├── setup-gradle.sh              # Linux/Mac 설정 스크립트
├── build.gradle.kts             # 루트 빌드 설정
├── settings.gradle.kts          # 프로젝트 설정
├── mcp-core/                    # 핵심 모듈
├── mcp-rest-adapter/            # REST 어댑터
├── mcp-security/                # 보안 모듈
├── mcp-redis/                   # Redis 모듈
├── mcp-postgres/                # PostgreSQL 모듈
├── mcp-elasticsearch/           # Elasticsearch 모듈
├── mcp-autoconfigure/           # 자동 설정
└── sample-spoke-app/            # 샘플 애플리케이션
```

## 🎯 다음 단계

1. ✅ 빌드 환경 확인 완료
2. 📦 Gradle Wrapper 설정
3. 🔨 프로젝트 빌드
4. 🚀 샘플 애플리케이션 실행
5. 🧪 API 테스트
6. 🛠️ 자신의 도구 구현

## 💡 팁

- 첫 빌드는 의존성 다운로드로 인해 시간이 걸릴 수 있습니다 (5-10분)
- 이후 빌드는 캐시로 인해 빠릅니다 (1-2분)
- IDE (IntelliJ, Eclipse, VS Code)에서도 Gradle 프로젝트로 열 수 있습니다

## 📞 지원

문제가 발생하면:
1. BUILD_CHECK.md 참조
2. DEV_SETUP.md 참조
3. QUICKSTART.md 참조
