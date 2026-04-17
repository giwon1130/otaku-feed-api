package com.giwon.otakufeed.features.anime.presentation

import com.giwon.otakufeed.features.anime.application.PrefsRepository
import com.giwon.otakufeed.features.anime.application.SwipeRepository
import com.giwon.otakufeed.features.anime.application.SwipeResult
import com.giwon.otakufeed.features.auth.application.JwtProvider
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

// ── DTOs ─────────────────────────────────────────────────────────────────────

data class SwipeRequest(
    @field:NotNull @field:Min(1) val animeId: Int,
    @field:NotBlank val result: String,   // like | dislike | skip
)

data class SwipeResponse(
    val id: String,
    val animeId: Int,
    val result: String,
    val swipedAt: String,
)

data class PrefsRequest(val favoriteGenres: List<String> = emptyList())

data class PrefsResponse(val favoriteGenres: List<String>)

// ── Controller ────────────────────────────────────────────────────────────────

@RestController
class AnimeController(
    private val swipeRepo: SwipeRepository,
    private val prefsRepo: PrefsRepository,
    private val jwt: JwtProvider,
) {
    // ── 스와이프 ──────────────────────────────────────────────────────────────

    @GetMapping("/swipes")
    fun getSwipes(
        @RequestHeader("Authorization") authorization: String,
        @RequestParam(required = false) result: String?,
    ): ResponseEntity<List<SwipeResponse>> {
        val userId = extractUserId(authorization)
        val swipes = if (result != null) {
            val r = runCatching { SwipeResult.valueOf(result) }.getOrElse {
                return ResponseEntity.badRequest().build()
            }
            swipeRepo.findByUserIdAndResult(userId, r)
        } else {
            swipeRepo.findByUserId(userId)
        }
        return ResponseEntity.ok(swipes.map {
            SwipeResponse(it.id.toString(), it.animeId, it.result.name, it.swipedAt.toString())
        })
    }

    @PostMapping("/swipes")
    fun saveSwipe(
        @RequestHeader("Authorization") authorization: String,
        @Valid @RequestBody req: SwipeRequest,
    ): ResponseEntity<SwipeResponse> {
        val userId = extractUserId(authorization)
        val result = runCatching { SwipeResult.valueOf(req.result) }.getOrElse {
            return ResponseEntity.badRequest().build()
        }
        val saved = swipeRepo.upsert(userId, req.animeId, result)
        return ResponseEntity.ok(SwipeResponse(saved.id.toString(), saved.animeId, saved.result.name, saved.swipedAt.toString()))
    }

    @DeleteMapping("/swipes/{animeId}")
    fun deleteSwipe(
        @RequestHeader("Authorization") authorization: String,
        @PathVariable animeId: Int,
    ): ResponseEntity<Map<String, Any>> {
        val userId = extractUserId(authorization)
        val deleted = swipeRepo.delete(userId, animeId)
        return ResponseEntity.ok(mapOf("deleted" to (deleted > 0)))
    }

    // ── 취향 설정 ─────────────────────────────────────────────────────────────

    @GetMapping("/prefs")
    fun getPrefs(@RequestHeader("Authorization") authorization: String): ResponseEntity<PrefsResponse> {
        val userId = extractUserId(authorization)
        val prefs = prefsRepo.find(userId)
        return ResponseEntity.ok(PrefsResponse(prefs.favoriteGenres))
    }

    @PutMapping("/prefs")
    fun savePrefs(
        @RequestHeader("Authorization") authorization: String,
        @RequestBody req: PrefsRequest,
    ): ResponseEntity<PrefsResponse> {
        val userId = extractUserId(authorization)
        val saved = prefsRepo.upsert(userId, req.favoriteGenres)
        return ResponseEntity.ok(PrefsResponse(saved.favoriteGenres))
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private fun extractUserId(authorization: String): UUID {
        val token = authorization.removePrefix("Bearer ").trim()
        return jwt.extractUserId(token)
    }
}
