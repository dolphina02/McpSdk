# MCP Spoke Server SDK - 문서

<div align="center">

**언어:** [🇬🇧 English](README.md) | [🇰🇷 한글](README_KO.md)

</div>

MCP Spoke Server SDK의 완전한 문서 (영문 및 한글).

## 🎯 이 SDK는 무엇인가?

**MCP Spoke Server SDK**는 **MCP Spoke 서버**를 구축하기 위한 **Spring Boot Starter 라이브러리**입니다. MCP Spoke 서버는 LLM 에이전트에 도구/기능을 노출하는 서버 측 컴포넌트입니다.

### 주요 포인트
- **목적**: Spring Boot 3.x로 프로덕션급 MCP 서버 구축
- **MCP 레이어**: **MCP 서버 (Spoke) 레이어** 구현 - 서버 측 컴포넌트
- **이중 인터페이스**: JSON-RPC 2.0 (네이티브 MCP 프로토콜) + REST (API 게이트웨이용)
- **단일 코어**: 모든 비즈니스 로직이 JSON-RPC 디스패처에 있음 (중복 없음)
- **금융 등급**: 보안, 감사, 준수 내장

### 아키텍처 레이어
```
┌─────────────────────────────────────────────────────────┐
│  LLM / AI 에이전트 / 클라이언트 애플리케이션            │
└────────────────────┬────────────────────────────────────┘
                     │ (JSON-RPC 2.0 또는 REST)
                     │
        ┌────────────▼────────────┐
        │  MCP Spoke 서버          │  ◄── 이 SDK
        │  (당신의 구현)            │
        └────────────┬────────────┘
                     │
        ┌────────────▼────────────┐
        │  비즈니스 로직            │
        │  (도구/기능)             │
        └─────────────────────────┘
```

### 구축하는 것
- **MCP Spoke 서버**: LLM 에이전트에 도구를 노출하는 독립형 서버
- **도구 구현**: MCP 도구로 래핑된 커스텀 비즈니스 로직
- **보안 API**: OAuth2, RBAC, ABAC, 감사 로깅 포함
- **프로덕션 준비**: 캐싱, 킬 스위치, 모니터링 포함

## 📚 문서

### 핵심 문서
- **[README](docs/ko/README.md)** - 완전한 참고 가이드 (1000+ 줄)
- **[INDEX](docs/ko/INDEX.md)** - 완전한 인덱스 및 네비게이션 가이드
- **[PROJECT_SUMMARY](docs/ko/PROJECT_SUMMARY.md)** - 생성된 내용 개요
- **[ARCHITECTURE](docs/ko/ARCHITECTURE.md)** - 시스템 설계 심화
- **[DELIVERY_CHECKLIST](docs/ko/DELIVERY_CHECKLIST.md)** - 모든 기능 검증
- **[FEATURES.md](FEATURES.md)** - 프로덕션 필수 기능 (멱등성, 감사 DLQ, 도구 버전 관리)

### 가이드
- **[QUICKSTART](docs/guides/ko/QUICKSTART.md)** - 로컬 개발 환경 설정
- **[DEV_SETUP](docs/guides/ko/DEV_SETUP.md)** - 개발 환경 설정
- **[BUILD_CHECK](docs/guides/ko/BUILD_CHECK.md)** - 빌드 환경 확인
- **[BUILD_ENVIRONMENT_SUMMARY](docs/guides/ko/BUILD_ENVIRONMENT_SUMMARY.md)** - 빌드 환경 요약

## 🎯 빠른 네비게이션

### 아키텍트용
1. [PROJECT_SUMMARY](docs/ko/PROJECT_SUMMARY.md)부터 시작
2. [ARCHITECTURE](docs/ko/ARCHITECTURE.md) 읽기
3. [README](docs/ko/README.md) 참고

### 개발자용
1. [QUICKSTART](docs/guides/ko/QUICKSTART.md)부터 시작
2. sample-spoke-app 예제 검토
3. [README](docs/ko/README.md) API 참고
4. [ARCHITECTURE](docs/ko/ARCHITECTURE.md) 상세 확인

### DevOps용
1. [QUICKSTART](docs/guides/ko/QUICKSTART.md) 로컬 설정
2. application-prod.yml 프로덕션 설정 검토
3. [README](docs/ko/README.md) 배포 섹션
4. [ARCHITECTURE](docs/ko/ARCHITECTURE.md) 토폴로지 검토

### 보안 담당자용
1. [README](docs/ko/README.md) 보안 모델 섹션
2. [ARCHITECTURE](docs/ko/ARCHITECTURE.md) 보안 레이어 섹션
3. [QUICKSTART](docs/guides/ko/QUICKSTART.md) 권한 테스트

## 📋 문서 구조

```
docs/
├── en/                          # 영문 문서
│   ├── README.md               # 완전한 참고
│   ├── INDEX.md                # 네비게이션 가이드
│   ├── PROJECT_SUMMARY.md      # 개요
│   ├── ARCHITECTURE.md         # 심화
│   └── DELIVERY_CHECKLIST.md   # 기능 검증
├── ko/                          # 한글 문서
│   ├── README.md               # 완전한 참고
│   ├── INDEX.md                # 네비게이션 가이드
│   ├── PROJECT_SUMMARY.md      # 개요
│   ├── ARCHITECTURE.md         # 심화
│   └── DELIVERY_CHECKLIST.md   # 기능 검증
├── guides/
│   ├── en/                     # 영문 가이드
│   │   ├── QUICKSTART.md       # 로컬 설정
│   │   ├── DEV_SETUP.md        # 개발 환경
│   │   ├── BUILD_CHECK.md      # 빌드 확인
│   │   └── BUILD_ENVIRONMENT_SUMMARY.md
│   └── ko/                     # 한글 가이드
│       ├── QUICKSTART.md       # 로컬 설정
│       ├── DEV_SETUP.md        # 개발 환경
│       ├── BUILD_CHECK.md      # 빌드 확인
│       └── BUILD_ENVIRONMENT_SUMMARY.md
└── README.md                   # 문서 인덱스
```

## 🔑 주요 기능

✅ **Spoke 전용 MCP**: 허브 의존성 없음
✅ **이중 인터페이스**: JSON-RPC 2.0 + REST
✅ **단일 로직 코어**: 중복 없음
✅ **금융 등급 보안**: OAuth2, RBAC, ABAC, 마스킹
✅ **감사 준수**: 서버 측 로깅, Elasticsearch
✅ **킬 스위치**: 도구 수준 및 글로벌 비활성화
✅ **캐싱**: Redis + PostgreSQL 폴백
✅ **비동기 로깅**: 논블로킹 감사 추적
✅ **오류 처리**: 표준 오류 코드
✅ **검증**: JSON 스키마 + 메타 검증
✅ **데이터 보호**: 열 수준 마스킹
✅ **추적 상관관계**: 분산 추적 지원
✅ **프로덕션 준비**: 모니터링, 메트릭, 상태 확인

## 🚀 시작하기

1. [QUICKSTART](docs/guides/ko/QUICKSTART.md) 읽기
2. 설정 단계 따라하기
3. API 테스트
4. 도구 구현

## 📞 지원

문제나 질문이 있으면:
1. 관련 문서 확인
2. sample-spoke-app 예제 검토
3. 문제 해결 섹션 확인

## 📄 라이선스

Proprietary - Financial Grade MCP SDK
