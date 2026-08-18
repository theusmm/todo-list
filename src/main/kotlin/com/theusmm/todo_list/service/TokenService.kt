package com.theusmm.todo_list.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTCreationException
import com.auth0.jwt.exceptions.JWTVerificationException
import com.theusmm.todo_list.entity.User
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

@Service
class TokenService {

    @Value("\${api.security.token.secret}")
    private lateinit var secret: String

    fun generateToken(user: User): String {
        try {
            val algorithm = Algorithm.HMAC256(secret)
            return JWT.create()
                .withIssuer("todo-list-api")
                .withSubject(user.email)
                .withExpiresAt(genExpirationDate())
                .sign(algorithm)
        } catch (exception: JWTCreationException) {
            throw RuntimeException("Error while Generating TOKEN", exception)
        }
    }

    fun validateToken(token: String): String {
        return try {
            val algorithm = Algorithm.HMAC256(secret)
            JWT.require(algorithm)
                .withIssuer("todo-list-api")
                .build()
                .verify(token)
                .subject
        } catch (exception: JWTVerificationException) {
            ""
        }
    }

    private fun genExpirationDate(): Instant {
        return LocalDateTime.now().plusMinutes(5).toInstant(ZoneOffset.of("-03:00"))
    }
}