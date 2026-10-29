package com.theusmm.todo_list.controller

import com.theusmm.todo_list.dto.CustomPageDto
import com.theusmm.todo_list.dto.request.UserCreateRequestDto
import com.theusmm.todo_list.dto.request.UserUpdateRequestDto
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
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

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

    @Test
    fun `should create regular user through HTTP`() {
        val request = UserCreateRequestDto(name = "new user", email = "user@email.com", password = "password123")
        val createdUser = UserResponseDto(id = 20L, name = request.name, email = request.email)

        Mockito.`when`(userService.createUser(request)).thenReturn(createdUser)

        mockMvc.perform(
            post("/users")
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
            .andExpect(jsonPath("$.id").value(20))
            .andExpect(jsonPath("$.name").value("new user"))
            .andExpect(jsonPath("$.email").value("user@email.com"))
    }

    @Test
    fun `should reject regular user from creating admin`() {
        val regularUser = createUserEntity(id = 10L, email = "user@email.com")
        val token = tokenFor(regularUser)

        mockMvc.perform(
            post("/users/admin")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                {
                  "name": "new admin",
                  "email": "new-admin@email.com",
                  "password": "password123"
                }
                """.trimIndent()
                )
        )
            .andExpect(status().isForbidden)
    }

    @Test
    fun `should return users page to admin`() {
        val admin = createUserEntity(id = 1L, email = "admin@email.com", role = UserRole.ADMIN)
        val token = tokenFor(admin)
        val pageable = PageRequest.of(0, 10, Sort.by("name"))
        val page = CustomPageDto(
            content = listOf(UserResponseDto(id = 2L, name = "user", email = "user@email.com")),
            pageNumber = 0,
            pageSize = 10,
            totalElements = 1,
            totalPages = 1,
            isLast = true
        )

        Mockito.`when`(userService.getAllUsers(pageable)).thenReturn(page)

        mockMvc.perform(
            get("/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].id").value(2))
            .andExpect(jsonPath("$.content[0].name").value("user"))
            .andExpect(jsonPath("$.pageNumber").value(0))
            .andExpect(jsonPath("$.pageSize").value(10))
    }

    @Test
    fun `should return user by id to admin`() {
        val admin = createUserEntity(id = 1L, email = "admin@email.com", role = UserRole.ADMIN)
        val token = tokenFor(admin)
        val expectedUser = UserResponseDto(id = 15L, name = "target user", email = "target@email.com")

        Mockito.`when`(userService.getUserById(15L)).thenReturn(expectedUser)

        mockMvc.perform(
            get("/users/15")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(15))
            .andExpect(jsonPath("$.name").value("target user"))
            .andExpect(jsonPath("$.email").value("target@email.com"))
    }

    @Test
    fun `should update authenticated user own profile`() {
        val loggedUser = createUserEntity(id = 15L, email = "user@email.com")
        val token = tokenFor(loggedUser)
        val request = UserUpdateRequestDto(name = "updated user", password = "newpass123")
        val updatedUser = UserResponseDto(id = 15L, name = "updated user", email = "user@email.com")

        Mockito.`when`(userService.updateUser(request, 15L)).thenReturn(updatedUser)

        mockMvc.perform(
            put("/users/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                {
                  "name": "updated user",
                  "password": "newpass123"
                }
                """.trimIndent()
                )
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(15))
            .andExpect(jsonPath("$.name").value("updated user"))
    }

    @Test
    fun `should delete authenticated user own account`() {
        val loggedUser = createUserEntity(id = 15L, email = "user@email.com")
        val token = tokenFor(loggedUser)

        mockMvc.perform(
            delete("/users/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
        )
            .andExpect(status().isNoContent)
            .andExpect(content().string(""))
    }

    @Test
    fun `should update user by id when requested by admin`() {
        val admin = createUserEntity(id = 1L, email = "admin@email.com", role = UserRole.ADMIN)
        val token = tokenFor(admin)
        val request = UserUpdateRequestDto(name = "updated user", password = "newpass123")
        val updatedUser = UserResponseDto(id = 15L, name = "updated user", email = "target@email.com")

        Mockito.`when`(userService.updateUser(request, 15L)).thenReturn(updatedUser)

        mockMvc.perform(
            put("/users/15")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                {
                  "name": "updated user",
                  "password": "newpass123"
                }
                """.trimIndent()
                )
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(15))
            .andExpect(jsonPath("$.name").value("updated user"))
    }

    @Test
    fun `should delete user by id when requested by admin`() {
        val admin = createUserEntity(id = 1L, email = "admin@email.com", role = UserRole.ADMIN)
        val token = tokenFor(admin)

        mockMvc.perform(
            delete("/users/15")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
        )
            .andExpect(status().isNoContent)
            .andExpect(content().string(""))
    }

    @Test
    fun `should return bad request when update request is invalid`() {
        val loggedUser = createUserEntity(id = 15L, email = "user@email.com")
        val token = tokenFor(loggedUser)

        mockMvc.perform(
            put("/users/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                {
                  "name": "",
                  "password": "123"
                }
                """.trimIndent()
                )
        )
            .andExpect(status().isBadRequest)
    }
}