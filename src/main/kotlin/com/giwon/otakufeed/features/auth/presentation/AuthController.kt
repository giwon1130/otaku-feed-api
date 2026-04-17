package com.giwon.otakufeed.features.auth.presentation

import com.giwon.otakufeed.features.auth.application.AuthException
import com.giwon.otakufeed.features.auth.application.AuthService
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

data class SignupRequest(
    @field:Email   val email: String,
    @field:NotBlank val password: String,
    @field:NotBlank val nickname: String,
)

data class LoginRequest(
    @field:Email    val email: String,
    @field:NotBlank val password: String,
)

data class AuthResponse(
    val token: String,
    val userId: String,
    val email: String,
    val nickname: String,
)

@RestController
@RequestMapping("/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping("/signup")
    fun signup(@Valid @RequestBody req: SignupRequest): ResponseEntity<AuthResponse> {
        val result = authService.signup(req.email, req.password, req.nickname)
        return ResponseEntity.ok(result.toResponse())
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody req: LoginRequest): ResponseEntity<AuthResponse> {
        val result = authService.login(req.email, req.password)
        return ResponseEntity.ok(result.toResponse())
    }

    @GetMapping("/me")
    fun me(@RequestHeader("Authorization") authorization: String): ResponseEntity<AuthResponse> {
        val token = authorization.removePrefix("Bearer ").trim()
        val result = authService.me(token)
        return ResponseEntity.ok(result.toResponse())
    }

    private fun AuthService.AuthResult.toResponse() =
        AuthResponse(token, userId, email, nickname)
}

@RestControllerAdvice
class AuthExceptionHandler {
    @ExceptionHandler(AuthException::class)
    fun handle(e: AuthException) =
        ResponseEntity.badRequest().body(mapOf("error" to e.message))
}
