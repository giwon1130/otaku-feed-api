# 📚 Otaku Feed API 문서

이 폴더는 **사람용** 백엔드 문서야.

**프로젝트 한 줄**: `otaku-feed` 모바일 앱의 인증 + 좋아요/취향 동기화 백엔드.
**기술 스택**: Spring Boot 3 / Kotlin / Java 21 / PostgreSQL / Flyway / JWT / HikariCP / Railway 배포.

**클라이언트 (별도 레포)**: [otaku-feed](https://github.com/giwon1130/otaku-feed) — React Native + Expo.

## 역할별 진입 경로

### ⚙️ 백엔드 개발자
1. [`엔지니어링/아키텍처.md`](엔지니어링/아키텍처.md) — 패키지 구조 + 책임 분리
2. [`엔지니어링/백엔드.md`](엔지니어링/백엔드.md) — 새 endpoint 만들 때 패턴
3. [`엔지니어링/API명세.md`](엔지니어링/API명세.md) — 단일 진실의 원천 (SoT)
4. [`엔지니어링/데이터모델.md`](엔지니어링/데이터모델.md) — 단일 진실의 원천 (SoT)
5. [`엔지니어링/의사결정로그.md`](엔지니어링/의사결정로그.md) — 왜 이렇게 만들었나

### 🚀 DevOps
1. [`운영/배포.md`](운영/배포.md) — Railway 자동 배포
2. [`운영/환경변수.md`](운영/환경변수.md) — 전체 키 목록
3. [`운영/장애대응.md`](운영/장애대응.md) — runbook
4. [`운영/보안.md`](운영/보안.md) — JWT/CORS/BCrypt
5. [`운영/비용최적화.md`](운영/비용최적화.md) — Railway 비용 절감

### 💻 프론트엔드 개발자
- 클라이언트 docs: [otaku-feed/docs/](https://github.com/giwon1130/otaku-feed/tree/main/docs)
- 이 레포에서 읽을 곳: [`엔지니어링/API명세.md`](엔지니어링/API명세.md), [`엔지니어링/데이터모델.md`](엔지니어링/데이터모델.md)

## SoT (단일 진실의 원천) 정책

| 토픽 | 위치 |
|-----|-----|
| 제품/디자인/UX | [클라이언트 docs](https://github.com/giwon1130/otaku-feed/tree/main/docs) |
| **API 명세** | 이 저장소 |
| **데이터 모델** | 이 저장소 |
| **배포 디테일** | 이 저장소 |
| 공유 토픽 (보안/비용/장애대응) | 양쪽에 있되 **이 저장소가 더 디테일** |

## 작성 원칙

1. 첫 줄에 "누가 봄 / 언제 봄"
2. 모든 결정에 "왜"
3. 링크 우선, 복붙 금지
4. 반말 + 한국어
