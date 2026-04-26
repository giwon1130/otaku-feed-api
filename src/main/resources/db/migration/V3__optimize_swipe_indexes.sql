-- ── swipe 정렬 인덱스 추가 ───────────────────────────────────────────────────
-- 기존 idx_swipes_user_id, idx_swipes_result는 user_id 필터엔 좋지만
-- ORDER BY swiped_at DESC를 인덱스로 못 풀어서 user별 swipe가 늘어나면
-- "filter → sort by swiped_at" 비용이 커짐.
--
-- (user_id, swiped_at DESC)와 (user_id, result, swiped_at DESC)를 추가하면
-- WHERE + ORDER가 인덱스 한 번 스캔으로 끝남.
--
-- IF NOT EXISTS 사용 — Flyway repeatable 안전 (이미 있으면 스킵).

CREATE INDEX IF NOT EXISTS idx_swipes_user_swiped_at
    ON otaku_user_swipes (user_id, swiped_at DESC);

CREATE INDEX IF NOT EXISTS idx_swipes_user_result_swiped_at
    ON otaku_user_swipes (user_id, result, swiped_at DESC);

-- 기존 단순 인덱스는 위 composite으로 prefix 매칭이 가능해서 중복.
-- 단, 다른 쿼리 패턴이 생길 가능성을 고려해 일단 유지 (drop은 다음 PR에서).
