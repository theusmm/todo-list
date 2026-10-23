package com.theusmm.todo_list.controller

import com.theusmm.todo_list.dto.CustomPageDto
import com.theusmm.todo_list.dto.response.UserResponseDto
import com.theusmm.todo_list.service.UserService
import com.theusmm.todo_list.util.createUserCreateRequestDto
import com.theusmm.todo_list.util.createUserEntity
import com.theusmm.todo_list.util.createUserUpdateRequestDto
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class UserControllerTest {

    @MockK
    lateinit var userService: UserService
    @InjectMockKs
    lateinit var userController: UserController

    @Test
    fun `should create user and return created status`() {
        val request = createUserCreateRequestDto(name = "test user", email = "testUser@email.com")
        val expectedResponse = UserResponseDto(id = 1L, name = "test user", email = "testUser@email.com")

        every { userService.createUser(request) } returns expectedResponse

        val response = userController.createUser(request)

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertEquals(expectedResponse, response.body)

        verify(exactly = 1) { userService.createUser(request) }
    }

    @Test
    fun `should update authenticated user and return ok status`() {
        val userId = 15L
        val loggedUser = createUserEntity(id = userId)
        val request = createUserUpdateRequestDto(name = "updated user")
        val expectedResponse = UserResponseDto(userId, name = "updated user", email = loggedUser.email)

        every { userService.updateUser(request, userId) } returns expectedResponse

        val response = userController.updateMe(request, loggedUser)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(expectedResponse, response.body)

        verify(exactly = 1) { userService.updateUser(request, userId) }
    }

    @Test
    fun `should delete authenticated user and return no content status`() {
        val loggedUser = createUserEntity(id = 15L)

        every { userService.deleteUser(loggedUser.id!!) } returns Unit

        val response = userController.deleteMe(loggedUser)

        assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
        assertNull(response.body)

        verify(exactly = 1) { userService.deleteUser(loggedUser.id!!) }
    }

    @Test
    fun `should create admin and return created status`() {
        val request = createUserCreateRequestDto(name = "admin user", email = "admin@email.com")
        val expectedResponse = UserResponseDto(id = 1L, name = "admin user", email = "admin@email.com")

        every { userService.createAdmin(request) } returns expectedResponse

        val response = userController.createAdmin(request)

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertEquals(expectedResponse, response.body)

        verify(exactly = 1) { userService.createAdmin(request) }
    }

    @Test
    fun `should return all users with ok status`() {
        val pageable = PageRequest.of(0, 10, Sort.by("name"))
        val expectedPage = CustomPageDto(
            content = listOf(
                UserResponseDto(id = 1L, name = "user 1", email = "user1@email.com"),
                UserResponseDto(id = 2L, name = "user 2", email = "user2@email.com")
            ), pageNumber = 0, pageSize = 10, totalElements = 2L, totalPages = 1, isLast = true
        )

        every { userService.getAllUsers(pageable) } returns expectedPage

        val response = userController.getAll(pageable)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(expectedPage, response.body)

        verify(exactly = 1) { userService.getAllUsers(pageable) }
    }

    @Test
    fun `should return user by id with ok status`() {
        val userId = 15L
        val expectedUser = UserResponseDto(userId, name = "test user", email = "testUser@email.com")

        every { userService.getUserById(userId) } returns expectedUser

        val response = userController.getById(userId)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(expectedUser, response.body)

        verify(exactly = 1) { userService.getUserById(userId) }
    }

    @Test
    fun `should update user by id and return ok status`() {
        val userId = 15L
        val loggedUser = createUserEntity(id = userId)
        val request = createUserUpdateRequestDto(name = "updated user")
        val expectedResponse = UserResponseDto(userId, name = "updated user", email = loggedUser.email)

        every { userService.updateUser(request, userId) } returns expectedResponse

        val response = userController.updateByAdmin(request, userId)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(expectedResponse, response.body)

        verify(exactly = 1) { userService.updateUser(request, userId) }
    }

    @Test
    fun `should delete user by id and return no content status`() {
        val userId = 15L

        every { userService.deleteUser(userId) } returns Unit

        val response = userController.deleteByAdmin(userId)

        assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
        assertNull(response.body)

        verify(exactly = 1) { userService.deleteUser(userId) }
    }
}