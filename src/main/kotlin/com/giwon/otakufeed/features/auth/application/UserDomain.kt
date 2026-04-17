package com.giwon.otakufeed.features.auth.application

import java.time.Instant
import java.util.UUID

data class OtakuUser(
    val id: UUID,
    val email: String,
    val passwordHash: String,
    val nickname: String,
    val createdAt: Instant,
)
