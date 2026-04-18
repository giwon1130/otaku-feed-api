package com.giwon.otakufeed.features.anime.application

import org.springframework.jdbc.core.ConnectionCallback
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class PrefsRepository(private val jdbc: JdbcTemplate) {

    fun find(userId: UUID): UserPrefs {
        val genres = jdbc.query(
            "SELECT favorite_genres FROM otaku_user_prefs WHERE user_id = ?",
            { rs, _ -> (rs.getArray("favorite_genres").array as Array<String>).toList() },
            userId,
        ).firstOrNull() ?: emptyList()
        return UserPrefs(userId, genres)
    }

    fun upsert(userId: UUID, genres: List<String>): UserPrefs {
        val arr = jdbc.execute(ConnectionCallback { con -> con.createArrayOf("text", genres.toTypedArray()) })
        jdbc.update("""
            INSERT INTO otaku_user_prefs (user_id, favorite_genres)
            VALUES (?, ?)
            ON CONFLICT (user_id)
            DO UPDATE SET favorite_genres = EXCLUDED.favorite_genres, updated_at = now()
        """, userId, arr)
        return UserPrefs(userId, genres)
    }
}
