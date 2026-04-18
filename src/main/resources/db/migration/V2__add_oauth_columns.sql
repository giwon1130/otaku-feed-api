-- OAuth 로그인 지원을 위해 password nullable + google_id/kakao_id 추가
ALTER TABLE otaku_users ALTER COLUMN password DROP NOT NULL;
ALTER TABLE otaku_users ADD COLUMN IF NOT EXISTS google_id TEXT UNIQUE;
ALTER TABLE otaku_users ADD COLUMN IF NOT EXISTS kakao_id  TEXT UNIQUE;
