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

data class BulkSwipeRequest(
    @field:NotNull val swipes: List<SwipeRequest> = emptyList(),
)

// id 필드는 클라가 안 씀 → egress 절감 위해 제거 (UUID string ~40B × N rows).
// 로깅/디버그가 필요하면 X-Request-Id 헤더 등 별도 채널 사용.
data class SwipeResponse(
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
            SwipeResponse(it.animeId, it.result.name, it.swipedAt.toString())
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
        return ResponseEntity.ok(SwipeResponse(saved.animeId, saved.result.name, saved.swipedAt.toString()))
    }

    /**
     * 여러 swipe를 한 번에 저장 (모바일 측 debounce + 일괄 푸시 패턴 대응).
     * - 클라가 빠르게 N번 swipe → 3초 idle 후 1회 호출
     * - 서버는 batchUpdate로 단일 RTT
     * - 응답은 200 + count만 (개별 row 안 돌려줌 → egress 절약)
     */
    @PostMapping("/swipes/bulk")
    fun saveSwipesBulk(
        @RequestHeader("Authorization") authorization: String,
        @Valid @RequestBody req: BulkSwipeRequest,
    ): ResponseEntity<Map<String, Int>> {
        val userId = extractUserId(authorization)
        val items = req.swipes.mapNotNull { s ->
            val r = runCatching { SwipeResult.valueOf(s.result) }.getOrNull() ?: return@mapNotNull null
            s.animeId to r
        }
        swipeRepo.bulkUpsert(userId, items)
        return ResponseEntity.ok(mapOf("saved" to items.size))
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
