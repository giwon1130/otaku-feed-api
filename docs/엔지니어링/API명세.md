# API 명세

> **누가 봄**: BE, FE
> **언제 봄**: 새 endpoint 호출/구현 시
> **단일 진실의 원천**: 이 문서. 클라 측 호출 코드는 `otaku-feed/src/api/otakuApi.ts`.

베이스 URL:
- 로컬: `http://localhost:8092`
- 프로덕션: `https://otaku-feed-api-production.up.railway.app`

모든 응답은 **gzip 압축** (1KB 이상). RN fetch 자동 디코드.

## 헤더 규칙

| 헤더 | 용도 |
|-----|-----|
| `Authorization: Bearer <jwt>` | 보호된 endpoint 필수 |
| `Content-Type: application/json` | POST/PUT 요청 |
| `Accept-Encoding: gzip` | 자동 (모바일 클라가 알아서) |

JWT 만료/잘못 → **401**. 인증 헤더 누락 → **400**. 권한 없음 → **403** (현재 미사용).

---

## 인증 (`/auth/*`)

### `POST /auth/signup`
**body**: `{ email, password, nickname }`
**응답** 200: `AuthResponse`
**에러**: 400 (이메일 중복/검증 실패)

### `POST /auth/login`
**body**: `{ email, password }`
**응답** 200: `AuthResponse`
**에러**: 401 (이메일 없음/비밀번호 틀림)

### `GET /auth/me`
**보호**: ✅
**응답** 200: `AuthResponse`

### `POST /auth/oauth/google`
**body**: `{ idToken }` (Google ID token)
**응답** 200: `AuthResponse` (자동 가입 또는 로그인)

### `POST /auth/oauth/kakao`
**body**: `{ accessToken }` (Kakao access token)
**응답** 200: `AuthResponse`

### `AuthResponse`
```json
{
  "token": "eyJhbGc...",
  "userId": "uuid",
  "email": "user@example.com",
  "nickname": "임기원"
}
```

JWT 만료 30일 (`otaku-feed.jwt.expiration-hours: 720`).

---

## Swipes (`/swipes/*`)

모두 보호됨.

### `GET /swipes`
**쿼리**: `?result=like` (선택, like/dislike/skip)
**응답** 200: `SwipeResponse[]`

### `POST /swipes`
**body**: `{ animeId: number, result: 'like'|'dislike'|'skip' }`
**응답** 200: `SwipeResponse`
**동작**: upsert (animeId 같으면 result 갱신 + swiped_at = now())

### `POST /swipes/bulk`
**body**: `{ swipes: [{ animeId, result }] }`
**응답** 200: `{ saved: N }`
**동작**: `batchUpdate`로 N개 단일 RTT. 응답엔 개별 row 안 돌려줌 (egress 절감).

### `DELETE /swipes/{animeId}`
**path**: `animeId` (number)
**응답** 200: `{ deleted: boolean }`

### `SwipeResponse`
```json
{
  "animeId": 123,
  "result": "like",
  "swipedAt": "2026-04-26T05:30:00Z"
}
```

⚠️ id 필드 제거됨 (ADR-005). 클라가 안 쓰니까 egress 절감.

---

## Prefs (`/prefs`)

### `GET /prefs`
**보호**: ✅
**응답** 200: `{ favoriteGenres: string[] }`

### `PUT /prefs`
**보호**: ✅
**body**: `{ favoriteGenres: string[] }`
**응답** 200: `{ favoriteGenres: string[] }`

⚠️ 클라이언트는 `loadPrefs`/`savePrefs`로 5분 TTL 캐시. UI에서 자주 호출돼도 서버 호출 안 됨.

---

## 헬스 (`/health`)

### `GET /health`
**보호**: X
**응답** 200: `{ status: 'UP', application: 'otaku-feed-api' }`

⚠️ 클라이언트 `apiHealth()` keepalive로 사용 (로그인 시만, 60초 polling). Railway cold start 회피용.

---

## 에러 응답 (Spring 기본)

```json
{
  "timestamp": "2026-04-26T...",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid or expired token",
  "path": "/swipes/bulk"
}
```

## Rate Limit

현재 없음. 클라이언트가 자율 제한:
- AniList 직접 호출 (백엔드 안 거침)
- 백엔드 호출 빈도 자체가 작음 (5분 TTL + 배치 + keepalive 조건부)

향후 rate limit 도입 시: Bucket4j 또는 nginx 레벨.

## 신규 endpoint 추가

[`백엔드.md`](백엔드.md) "새 endpoint 추가 절차" 참고.

문서 갱신 순서:
1. 이 파일에 endpoint 추가
2. 클라이언트 `otaku-feed/docs/엔지니어링/API명세.md`에도 cross-link
3. `otaku-feed/src/api/otakuApi.ts`에 client 메서드 추가
4. 클라이언트 사용처 (storage.ts 등)에서 호출

---

## 클라이언트 사용 예 (참고)

`otaku-feed/src/api/otakuApi.ts`:

```ts
export async function apiSaveSwipesBulk(
  swipes: { animeId: number; result: 'like' | 'dislike' | 'skip' }[],
): Promise<void> {
  await request<unknown>('POST', '/swipes/bulk', { swipes })
}
```

자세한 사용 패턴: [클라이언트 docs/엔지니어링/API명세.md](https://github.com/giwon1130/otaku-feed/blob/main/docs/엔지니어링/API명세.md)
