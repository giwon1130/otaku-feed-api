package com.giwon.otakufeed.features.auth.application

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

class AuthException(message: String) : RuntimeException(message)

@Service
class AuthService(
    private val userRepo: UserRepository,
    private val jwt: JwtProvider,
) {
    private val encoder = BCryptPasswordEncoder()

    data class AuthResult(val token: String, val userId: String, val email: String, val nickname: String)

    fun signup(email: String, password: String, nickname: String): AuthResult {
        if (userRepo.existsByEmail(email)) throw AuthException("이미 사용 중인 이메일이에요.")
        if (password.length < 6) throw AuthException("비밀번호는 6자 이상이어야 해요.")
        val user = userRepo.save(
            email        = email.trim().lowercase(),
            passwordHash = encoder.encode(password),
            nickname     = nickname.trim(),
        )
        return AuthResult(jwt.generate(user.id, user.email), user.id.toString(), user.email, user.nickname)
    }

    fun login(email: String, password: String): AuthResult {
        val user = userRepo.findByEmail(email.trim().lowercase())
            ?: throw AuthException("이메일 또는 비밀번호가 틀렸어요.")
        if (!encoder.matches(password, user.passwordHash)) throw AuthException("이메일 또는 비밀번호가 틀렸어요.")
        return AuthResult(jwt.generate(user.id, user.email), user.id.toString(), user.email, user.nickname)
    }

    fun me(token: String): AuthResult {
        val userId = jwt.extractUserId(token)
        val user = userRepo.findById(userId) ?: throw AuthException("유저를 찾을 수 없어요.")
        return AuthResult(token, user.id.toString(), user.email, user.nickname)
    }
}
