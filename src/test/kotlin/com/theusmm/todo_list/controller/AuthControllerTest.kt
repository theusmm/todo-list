package com.theusmm.todo_list.controller

import com.theusmm.todo_list.dto.request.LoginRequestDto
import com.theusmm.todo_list.repository.UserRepository
import com.theusmm.todo_list.service.TokenService
import com.theusmm.todo_list.util.createUserEntity
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.crypto.password.PasswordEncoder
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class AuthControllerTest {

    @MockK
    lateinit var userRepository: UserRepository

    @MockK
    lateinit var passwordEncoder: PasswordEncoder

    @MockK
    lateinit var tokenService: TokenService

    @InjectMockKs
    lateinit var authController: AuthController

    @Test
    fun `should return 401 when email is not registered`() {
        val request = LoginRequestDto(email = "missing@email.com", password = "password123")

        every { userRepository.findByEmail(request.email) } returns null

        val response = authController.login(request)

        assertEquals(401, response.statusCode.value())

        verify(exactly = 0) { passwordEncoder.matches(any(), any()) }
        verify(exactly = 0) { tokenService.generateToken(any()) }
    }

    @Test
    fun `should return 401 when password is incorrect`() {
        val request = LoginRequestDto(email = "user@email.com", password = "wrongPassword")
        val user = createUserEntity(id = 10L, name = "user", email = request.email)

        every { userRepository.findByEmail(request.email) } returns user
        every { passwordEncoder.matches(request.password, user.password) } returns false

        val response = authController.login(request)

        assertEquals(401, response.statusCode.value())

        verify(exactly = 1) { passwordEncoder.matches(request.password, user.password) }
        verify(exactly = 0) { tokenService.generateToken(any()) }
    }

    @Test
    fun `should return token when credentials are valid`() {
        val request = LoginRequestDto(email = "user@email.com", password = "possword123")
        val user = createUserEntity(id = 10L, name = "user", email = request.email)
        val generatedToken = "test-token"

        every { userRepository.findByEmail(request.email) } returns user
        every { passwordEncoder.matches(request.password, user.password) } returns true
        every { tokenService.generateToken(user) } returns generatedToken

        val response = authController.login(request)

        assertEquals(200, response.statusCode.value())
        assertEquals(generatedToken, response.body?.token)

        verify(exactly = 1) { tokenService.generateToken(user) }
    }
}