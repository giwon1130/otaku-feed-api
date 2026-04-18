package com.giwon.otakufeed.features.auth.application

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class UserRepository(private val jdbc: JdbcTemplate) {

    private val rowMapper = RowMapper { rs, _ ->
        OtakuUser(
            id           = UUID.fromString(rs.getString("id")),
            email        = rs.getString("email"),
            passwordHash = rs.getString("password"),
            nickname     = rs.getString("nickname"),
            googleId     = rs.getString("google_id"),
            kakaoId      = rs.getString("kakao_id"),
            createdAt    = rs.getTimestamp("created_at").toInstant(),
        )
    }

    fun findByEmail(email: String): OtakuUser? =
        jdbc.query("SELECT * FROM otaku_users WHERE email = ?", rowMapper, email).firstOrNull()

    fun findById(id: UUID): OtakuUser? =
        jdbc.query("SELECT * FROM otaku_users WHERE id = ?", rowMapper, id).firstOrNull()

    fun findByGoogleId(googleId: String): OtakuUser? =
        jdbc.query("SELECT * FROM otaku_users WHERE google_id = ?", rowMapper, googleId).firstOrNull()

    fun findByKakaoId(kakaoId: String): OtakuUser? =
        jdbc.query("SELECT * FROM otaku_users WHERE kakao_id = ?", rowMapper, kakaoId).firstOrNull()

    fun existsByEmail(email: String): Boolean =
        (jdbc.queryForObject("SELECT COUNT(*) FROM otaku_users WHERE email = ?", Int::class.java, email) ?: 0) > 0

    fun save(email: String, passwordHash: String, nickname: String): OtakuUser {
        val id = UUID.randomUUID()
        jdbc.update(
            "INSERT INTO otaku_users (id, email, password, nickname) VALUES (?, ?, ?, ?)",
            id, email, passwordHash, nickname,
        )
        return findById(id)!!
    }

    /** OAuth 유저 신규 생성 (password 없음) */
    fun saveOAuthUser(email: String, nickname: String, googleId: String? = null, kakaoId: String? = null): OtakuUser {
        val id = UUID.randomUUID()
        jdbc.update(
            "INSERT INTO otaku_users (id, email, nickname, google_id, kakao_id) VALUES (?, ?, ?, ?, ?)",
            id, email, nickname, googleId, kakaoId,
        )
        return findById(id)!!
    }

    /** 기존 이메일 계정에 Google ID 연결 */
    fun linkGoogleId(userId: UUID, googleId: String) {
        jdbc.update("UPDATE otaku_users SET google_id = ? WHERE id = ?", googleId, userId)
    }

    /** 기존 이메일 계정에 Kakao ID 연결 */
    fun linkKakaoId(userId: UUID, kakaoId: String) {
        jdbc.update("UPDATE otaku_users SET kakao_id = ? WHERE id = ?", kakaoId, userId)
    }
}
