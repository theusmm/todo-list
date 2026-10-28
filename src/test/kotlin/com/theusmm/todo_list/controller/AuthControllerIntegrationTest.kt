package com.theusmm.todo_list.controller

import com.theusmm.todo_list.config.SecurityConfig
import com.theusmm.todo_list.config.SecurityFilter
import com.theusmm.todo_list.entity.User
import com.theusmm.todo_list.repository.UserRepository
import com.theusmm.todo_list.service.TokenService
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.verifyNoInteractions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.cache.CacheManager
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.context.annotation.Bean

@WebMvcTest(AuthController::class)
@Import(SecurityConfig::class, SecurityFilter::class, TestCacheConfiguration::class)
class AuthControllerIntegrationTest(@Autowired private val mockMvc: MockMvc) {
    @MockitoBean
    lateinit var userRepository: UserRepository

    @MockitoBean
    lateinit var passwordEncoder: PasswordEncoder

    @MockitoBean
    lateinit var tokenService: TokenService

    @Test
    fun `POST login should return token without authentication`() {
        val email = "ana@email.com"
        val password = "senha123"
        val user = User(
            id = 10L,
            name = "Ana",
            email = email,
            password = "senha-codificada"
        )
        val generatedToken = "token-de-teste"

        given(userRepository.findByEmail(email)).willReturn(user)
        given(passwordEncoder.matches(password, user.password)).willReturn(true)
        given(tokenService.generateToken(user)).willReturn(generatedToken)

        mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
            {
              "email": "$email",
              "password": "$password"
            }
            """.trimIndent()
                )
        )
            .andExpect(status().isOk)
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.token").value(generatedToken))
    }

   @Test
   fun `POST login should  return 400 when request fields are invalid`() {
       mockMvc.perform(
           post("/auth/login")
               .contentType(MediaType.APPLICATION_JSON)
               .content(
                   """
                {
                  "email": "email-invalido",
                  "password": ""
                }
            """.trimIndent()
               )
       )
           .andExpect(status().isBadRequest)

       verifyNoInteractions(userRepository, passwordEncoder, tokenService)
   }
}

@TestConfiguration(proxyBeanMethods = false)
class TestCacheConfiguration {

    @Bean
    fun cacheManager(): CacheManager =
        ConcurrentMapCacheManager(
            "users",
            "tasks",
            "tasksByUserId",
            "tasksByLoggedUser"
        )
}