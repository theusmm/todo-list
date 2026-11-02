package com.theusmm.todo_list.config

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.theusmm.todo_list.entity.UserRole
import com.theusmm.todo_list.repository.UserRepository
import com.theusmm.todo_list.service.TokenService
import com.theusmm.todo_list.util.createUserEntity
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.servlet.FilterChain
import java.time.Instant
import java.util.Date
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder

class SecurityFilterTest {

    private val tokenService = mockk<TokenService>()
    private val userRepository = mockk<UserRepository>()
    private val securityFilter = SecurityFilter(tokenService, userRepository)
    private val testSecret = "test-secret-key-is-long-enough-for-hmc256"

    @BeforeEach
    fun clearContextBeforeTest() {
        SecurityContextHolder.clearContext()
    }

    @AfterEach
    fun clearContextAfterTest() {
        SecurityContextHolder.clearContext()
    }

    private fun tokenServiceWithTestSecret(): TokenService {
        val service = TokenService()

        val secretField = TokenService::class.java.getDeclaredField("secret")
        secretField.isAccessible = true
        secretField.set(service, testSecret)

        return service
    }

    @Test
    fun `should continue chain without authentication when authorization header is absent`() {
        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()
        val chain = mockk<FilterChain>(relaxed = true)

        securityFilter.doFilter(request, response, chain)

        assertNull(SecurityContextHolder.getContext().authentication)
        verify(exactly = 0) { tokenService.validateToken(any()) }
        verify(exactly = 0) { userRepository.findByEmail(any()) }
        verify(exactly = 1) { chain.doFilter(request, response) }
    }

    @Test
    fun `should ignore authorization header that does not use bearer scheme`() {
        val request = MockHttpServletRequest().apply {
            addHeader("Authorization", "Basic abc123")
        }
        val response = MockHttpServletResponse()
        val chain = mockk<FilterChain>(relaxed = true)

        securityFilter.doFilter(request, response, chain)

        assertNull(SecurityContextHolder.getContext().authentication)
        verify(exactly = 0) { tokenService.validateToken(any()) }
        verify(exactly = 0) { userRepository.findByEmail(any()) }
        verify(exactly = 1) { chain.doFilter(request, response) }
    }

    @Test
    fun `should continue chain without authentication when token validation returns empty email`() {
        val request = MockHttpServletRequest().apply {
            addHeader("Authorization", "Bearer invalid-token")
        }
        val response = MockHttpServletResponse()
        val chain = mockk<FilterChain>(relaxed = true)

        every { tokenService.validateToken("invalid-token") } returns ""

        securityFilter.doFilter(request, response, chain)

        assertNull(SecurityContextHolder.getContext().authentication)
        verify(exactly = 1) { tokenService.validateToken("invalid-token") }
        verify(exactly = 0) { userRepository.findByEmail(any()) }
        verify(exactly = 1) { chain.doFilter(request, response) }
    }

    @Test
    fun `should continue chain without authentication when token email has no user`() {
        val email = "missing@email.com"
        val request = MockHttpServletRequest().apply {
            addHeader("Authorization", "Bearer valid-token")
        }
        val response = MockHttpServletResponse()
        val chain = mockk<FilterChain>(relaxed = true)

        every { tokenService.validateToken("valid-token") } returns email
        every { userRepository.findByEmail(email) } returns null

        securityFilter.doFilter(request, response, chain)

        assertNull(SecurityContextHolder.getContext().authentication)
        verify(exactly = 1) { tokenService.validateToken("valid-token") }
        verify(exactly = 1) { userRepository.findByEmail(email) }
        verify(exactly = 1) { chain.doFilter(request, response) }
    }

    @Test
    fun `should authenticate user and add role authority when token and user are valid`() {
        val email = "admin@email.com"
        val user = createUserEntity(
            id = 1L,
            email = email,
            role = UserRole.ADMIN
        )
        val request = MockHttpServletRequest().apply {
            addHeader("Authorization", "Bearer valid-token")
        }
        val response = MockHttpServletResponse()
        val chain = mockk<FilterChain>(relaxed = true)

        every { tokenService.validateToken("valid-token") } returns email
        every { userRepository.findByEmail(email) } returns user

        securityFilter.doFilter(request, response, chain)

        val authentication = assertNotNull(
            SecurityContextHolder.getContext().authentication
        )

        assertSame(user, authentication.principal)
        assertEquals("ROLE_ADMIN", authentication.authorities.single().authority)
        verify(exactly = 1) { tokenService.validateToken("valid-token") }
        verify(exactly = 1) { userRepository.findByEmail(email) }
        verify(exactly = 1) { chain.doFilter(request, response) }
    }

    @Test
    fun `should continue chain without authentication when token is malformed`() {
        val realTokenService = tokenServiceWithTestSecret()
        val filter = SecurityFilter(realTokenService, userRepository)
        val request = MockHttpServletRequest().apply {
            addHeader("Authorization", "Bearer this-is-not-a-jwt")
        }
        val response = MockHttpServletResponse()
        val chain = mockk<FilterChain>(relaxed = true)

        filter.doFilter(request, response, chain)

        assertNull(SecurityContextHolder.getContext().authentication)
        verify(exactly = 0) { userRepository.findByEmail(any()) }
        verify(exactly = 1) { chain.doFilter(request, response) }
    }

    @Test
    fun `should continue chain without authentication when token is expired`() {
        val realTokenService = tokenServiceWithTestSecret()
        val filter = SecurityFilter(realTokenService, userRepository)

        val expiredToken = JWT.create()
            .withIssuer("todo-list-api")
            .withSubject("user@email.com")
            .withExpiresAt(Date.from(Instant.now().minusSeconds(60)))
            .sign(Algorithm.HMAC256(testSecret))

        val request = MockHttpServletRequest().apply {
            addHeader("Authorization", "Bearer $expiredToken")
        }
        val response = MockHttpServletResponse()
        val chain = mockk<FilterChain>(relaxed = true)

        filter.doFilter(request, response, chain)

        assertNull(SecurityContextHolder.getContext().authentication)
        verify(exactly = 0) { userRepository.findByEmail(any()) }
        verify(exactly = 1) { chain.doFilter(request, response) }
    }
}