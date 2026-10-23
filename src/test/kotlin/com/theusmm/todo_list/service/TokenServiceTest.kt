package com.theusmm.todo_list.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.theusmm.todo_list.entity.UserRole
import com.theusmm.todo_list.util.createUserEntity
import org.junit.jupiter.api.BeforeEach
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TokenServiceTest {

    private lateinit var tokenService: TokenService

    private val secret = "test-secret-key-is-long-enough-for-hmc256"

    @BeforeEach
    fun setup() {
        tokenService = TokenService()

        val secretField = TokenService::class.java.getDeclaredField("secret")
        secretField.isAccessible = true
        secretField.set(tokenService, secret)
    }

    @Test
    fun `should generate token with user information and expiration`() {
        val user = createUserEntity(email = "admin@email.com", role = UserRole.ADMIN)
        val token = tokenService.generateToken(user)
        val decodedToken = JWT.require(Algorithm.HMAC256(secret))
            .withIssuer("todo-list-api")
            .build()
            .verify(token)

        assertEquals("admin@email.com", decodedToken.subject)
        assertEquals("ADMIN", decodedToken.getClaim("role").asString())
        assertEquals("todo-list-api", decodedToken.issuer)
        assertTrue(decodedToken.expiresAt.after(Date()))
    }

    @Test
    fun `should return subject when token is valid`() {
        val user = createUserEntity(email  = "user@email.com")
        val token = tokenService.generateToken(user)
        val result = tokenService.validateToken(token)

        assertEquals("user@email.com", result)
    }

    @Test
    fun `should return empty string when token is invalid`() {
        val result = tokenService.validateToken("invalid-token")
        assertEquals("", result)
    }

    @Test
    fun `should return empty string when token has invalid signature`() {
        val tokenWithDifferentSecret = JWT.create()
            .withIssuer("todo-list-api")
            .withSubject("user@email.com")
            .sign(Algorithm.HMAC256("another-test-secret-that-is-also-long-enough"))

        val result = tokenService.validateToken(tokenWithDifferentSecret)

        assertEquals("", result)
    }

    @Test
    fun `should return empty string when token has invalid issuer`() {
        val tokenWithDifferentIssuer = JWT.create()
            .withIssuer("another-issuer")
            .withSubject("user@email.com")
            .sign(Algorithm.HMAC256(secret))

        val result = tokenService.validateToken(tokenWithDifferentIssuer)

        assertEquals("", result)
    }

    @Test
    fun `should return empty string when token is expired`() {
        val expiratedToken = JWT.create()
            .withIssuer("todo-list-api")
            .withSubject("user@email.com")
            .withExpiresAt(Date.from(java.time.Instant.now().minusSeconds(60)))
            .sign(Algorithm.HMAC256(secret))

        val result = tokenService.validateToken(expiratedToken)

        assertEquals("", result)
    }
}