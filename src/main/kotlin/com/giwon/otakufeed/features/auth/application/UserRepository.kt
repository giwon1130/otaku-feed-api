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
            createdAt    = rs.getTimestamp("created_at").toInstant(),
        )
    }

    fun findByEmail(email: String): OtakuUser? =
        jdbc.query("SELECT * FROM otaku_users WHERE email = ?", rowMapper, email).firstOrNull()

    fun findById(id: UUID): OtakuUser? =
        jdbc.query("SELECT * FROM otaku_users WHERE id = ?", rowMapper, id).firstOrNull()

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
}
