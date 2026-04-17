-- ── users ──────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS otaku_users (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    email       TEXT        NOT NULL UNIQUE,
    password    TEXT        NOT NULL,
    nickname    TEXT        NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ── user_prefs ─────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS otaku_user_prefs (
    user_id          UUID     PRIMARY KEY REFERENCES otaku_users(id) ON DELETE CASCADE,
    favorite_genres  TEXT[]   NOT NULL DEFAULT '{}',
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ── user_swipes ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS otaku_user_swipes (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID        NOT NULL REFERENCES otaku_users(id) ON DELETE CASCADE,
    anime_id    INTEGER     NOT NULL,
    result      TEXT        NOT NULL CHECK (result IN ('like', 'dislike', 'skip')),
    swiped_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (user_id, anime_id)
);

CREATE INDEX IF NOT EXISTS idx_swipes_user_id ON otaku_user_swipes(user_id);
CREATE INDEX IF NOT EXISTS idx_swipes_result  ON otaku_user_swipes(user_id, result);
