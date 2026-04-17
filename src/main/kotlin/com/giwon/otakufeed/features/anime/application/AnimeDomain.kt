package com.giwon.otakufeed.features.anime.application

import java.time.Instant
import java.util.UUID

data class UserSwipe(
    val id: UUID,
    val userId: UUID,
    val animeId: Int,
    val result: SwipeResult,
    val swipedAt: Instant,
)

enum class SwipeResult { like, dislike, skip }

data class UserPrefs(
    val userId: UUID,
    val favoriteGenres: List<String>,
)
