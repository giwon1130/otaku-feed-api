package com.giwon.otakufeed.features.auth.application

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

@Component
class JwtProvider(
    @Value("\${otaku-feed.jwt.secret}") secret: String,
    @Value("\${otaku-feed.jwt.expiration-hours:720}") private val expirationHours: Long,
) {
    private val key: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray())

    fun generate(userId: UUID, email: String): String {
        val now = Date()
        val exp = Date(now.time + expirationHours * 3_600_000L)
        return Jwts.builder()
            .subject(userId.toString())
            .claim("email", email)
            .issuedAt(now)
            .expiration(exp)
            .signWith(key)
            .compact()
    }

    fun parse(token: String): Claims =
        Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload

    /**
     * 토큰에서 userId 추출. 잘못된/만료된 토큰이면 401로 응답.
     * (이전엔 파싱 예외가 그대로 올라가서 500 Internal Server Error로 보였음)
     */
    fun extractUserId(token: String): UUID =
        runCatching { UUID.fromString(parse(token).subject) }
            .getOrElse { throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token") }

    fun isValid(token: String): Boolean = runCatching { parse(token) }.isSuccess
}
