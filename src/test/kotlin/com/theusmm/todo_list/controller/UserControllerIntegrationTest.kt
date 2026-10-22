package com.theusmm.todo_list.controller

import com.theusmm.todo_list.dto.request.UserCreateRequestDto
import com.theusmm.todo_list.dto.response.UserResponseDto
import com.theusmm.todo_list.entity.User
import com.theusmm.todo_list.entity.UserRole
import com.theusmm.todo_list.repository.UserRepository
import com.theusmm.todo_list.service.TokenService
import com.theusmm.todo_list.service.UserService
import com.theusmm.todo_list.util.createUserEntity
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc
    @Autowired
    lateinit var tokenService: TokenService
    @field:MockitoBean
    lateinit var userRepository: UserRepository
    @field:MockitoBean
    lateinit var userService: UserService

    private fun tokenFor(user: User): String {
        Mockito.`when`(userRepository.findByEmail(user.email)).thenReturn(user)
        return tokenService.generateToken(user)
    }

    @Test
    fun `should return bad request when create request is invalid`() {

        mockMvc.perform(
            post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "name": "",
                      "email": "invalidEmail",
                      "password": "123"
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should reject unauthenticated request to protected route`() {

        mockMvc.perform(get("/users/me"))
            .andExpect(status().isForbidden)
    }

    @Test
    fun `should reject regular user from admin route`() {

        val regularUser = createUserEntity(id = 10L, email = "user@email.com", role = UserRole.USER)
        val token = tokenFor(regularUser)

        mockMvc.perform (
            get("/users/10")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
        )
            .andExpect(status().isForbidden)
    }

    @Test
    fun `should allow admin to create admin user`() {

        val admin = createUserEntity(id = 1L, email = "admin@email.com", role = UserRole.ADMIN)
        val token = tokenFor(admin)

        val request = UserCreateRequestDto(name = "new admin", email = "new-admin@email.com", password = "password123")
        val createdUser = UserResponseDto(id = 2L, name = request.name, email = request.email)

        Mockito.`when`(userService.createAdmin(request)).thenReturn(createdUser)

        mockMvc.perform(
            post("/users/admin")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "name": "${request.name}",
                      "email": "${request.email}",
                      "password": "${request.password}"
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(2))
            .andExpect(jsonPath("$.name").value("new admin"))
            .andExpect(jsonPath("$.email").value("new-admin@email.com"))
    }
}