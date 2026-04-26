package com.giwon.otakufeed.features.anime.application

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class SwipeRepository(private val jdbc: JdbcTemplate) {

    private val rowMapper = RowMapper { rs, _ ->
        UserSwipe(
            id       = UUID.fromString(rs.getString("id")),
            userId   = UUID.fromString(rs.getString("user_id")),
            animeId  = rs.getInt("anime_id"),
            result   = SwipeResult.valueOf(rs.getString("result")),
            swipedAt = rs.getTimestamp("swiped_at").toInstant(),
        )
    }

    fun findByUserId(userId: UUID): List<UserSwipe> =
        jdbc.query("SELECT * FROM otaku_user_swipes WHERE user_id = ? ORDER BY swiped_at DESC", rowMapper, userId)

    fun findByUserIdAndResult(userId: UUID, result: SwipeResult): List<UserSwipe> =
        jdbc.query("SELECT * FROM otaku_user_swipes WHERE user_id = ? AND result = ? ORDER BY swiped_at DESC", rowMapper, userId, result.name)

    fun upsert(userId: UUID, animeId: Int, result: SwipeResult): UserSwipe {
        jdbc.update("""
            INSERT INTO otaku_user_swipes (user_id, anime_id, result)
            VALUES (?, ?, ?)
            ON CONFLICT (user_id, anime_id)
            DO UPDATE SET result = EXCLUDED.result, swiped_at = now()
        """, userId, animeId, result.name)
        return jdbc.query(
            "SELECT * FROM otaku_user_swipes WHERE user_id = ? AND anime_id = ?",
            rowMapper, userId, animeId,
        ).first()
    }

    /**
     * 여러 swipe를 한 트랜잭션·한 round-trip으로 upsert.
     * - JdbcTemplate.batchUpdate() → 단일 prepared statement에 N개 입력 바인딩
     * - DB 입장에선 N개 row INSERT/UPDATE지만 네트워크 RTT는 1회
     * - 모바일 ↔ 서버 간 RPC 비용(Railway egress + container CPU 시간)을 절감
     */
    fun bulkUpsert(userId: UUID, items: List<Pair<Int, SwipeResult>>) {
        if (items.isEmpty()) return
        jdbc.batchUpdate(
            """
            INSERT INTO otaku_user_swipes (user_id, anime_id, result)
            VALUES (?, ?, ?)
            ON CONFLICT (user_id, anime_id)
            DO UPDATE SET result = EXCLUDED.result, swiped_at = now()
            """,
            items.map { (animeId, result) -> arrayOf<Any>(userId, animeId, result.name) },
        )
    }

    fun delete(userId: UUID, animeId: Int): Int =
        jdbc.update("DELETE FROM otaku_user_swipes WHERE user_id = ? AND anime_id = ?", userId, animeId)
}
