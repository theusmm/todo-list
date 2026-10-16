package com.theusmm.todo_list.service

import com.theusmm.todo_list.dto.request.UserCreateRequestDto
import com.theusmm.todo_list.dto.request.UserUpdateRequestDto
import com.theusmm.todo_list.entity.User
import com.theusmm.todo_list.entity.UserRole
import com.theusmm.todo_list.exception.AccessDeniedException
import com.theusmm.todo_list.exception.ResourceNotFoundException
import com.theusmm.todo_list.repository.UserRepository
import com.theusmm.todo_list.util.createUserEntity
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.Optional
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class UserServiceTest {

    @MockK
    lateinit var userRepository: UserRepository
    @MockK
    lateinit var passwordEncoder: PasswordEncoder
    @InjectMockKs
    lateinit var userService: UserService

    @Test
    fun `should return all users for the requested page`() {

        val pageable = PageRequest.of(0, 10)
        val users = listOf(
            createUserEntity(id = 1L, name = "user 1", email = "user1@email.com"),
            createUserEntity(id = 2L, name = "user 2", email = "user2@email.com")
        )

        val userPage = PageImpl(users, pageable, users.size.toLong())

        every { userRepository.findAll(pageable) } returns userPage

        val result = userService.getAllUsers(pageable)

        assertEquals(2, result.content.size)
        assertEquals("user 1", result.content[0].name)
        assertEquals("user1@email.com", result.content[0].email)
        assertEquals("user 2", result.content[1].name)
        assertEquals("user2@email.com", result.content[1].email)

        verify(exactly = 1) { userRepository.findAll(pageable) }
    }

    @Test
    fun `should return an empty page when there are no users`() {

        val pageable = PageRequest.of(0, 10)
        val emptyPage = PageImpl<User>(emptyList(),pageable, 0)

        every { userRepository.findAll(pageable) } returns emptyPage

        val result = userService.getAllUsers(pageable)

        assertEquals(0, result.content.size)

        verify(exactly = 1) { userRepository.findAll(pageable) }
    }

    @Test
    fun `should return user successfully`() {

        val userId = 10L
        val user = createUserEntity(id = userId, name = "user 1", email = "user1@email.com")

        every { userRepository.findById(userId) } returns Optional.of(user)

        val result = userService.getUserById(userId)

        assertEquals(userId, result.id)
        assertEquals("user 1", result.name)
        assertEquals("user1@email.com", result.email)

        verify(exactly = 1) { userRepository.findById(userId) }
    }

    @Test
    fun `should throw ResourceNotFoundException when user does not exist`() {

        val userId = 10L

        every { userRepository.findById(userId) } returns Optional.empty()

        val exception = assertThrows<ResourceNotFoundException> { userService.getUserById(userId) }

        assertEquals("User not found", exception.message)

        verify(exactly = 1) { userRepository.findById(userId) }
    }

    @Test
    fun `should create user with USER role andencoded password`() {

        val request = UserCreateRequestDto(name = "user 1", email = "user1@email.com", password = "123456")
        val encodedPassword = "encoded-password"
        val savedUser = slot<User>()

        every { userRepository.existsByEmail(request.email) } returns false
        every { passwordEncoder.encode(request.password) } returns encodedPassword
        every { userRepository.save(capture(savedUser)) } answers { firstArg() }

        val result = userService.createUser(request)

        assertEquals("user 1", result.name)
        assertEquals("user1@email.com", result.email)

        assertEquals(UserRole.USER, savedUser.captured.role)
        assertEquals(encodedPassword, savedUser.captured.password)

        verify(exactly = 1) { userRepository.save(any()) }
    }

    @Test
    fun `should create user with ADMIN role and encoded password`() {

        val request = UserCreateRequestDto(name = "admin", email = "admin@email.com", password = "123456")
        val encodedPassword = "encoded-password"
        val savedUser = slot<User>()

        every { userRepository.existsByEmail(request.email) } returns false
        every { passwordEncoder.encode(request.password) } returns encodedPassword
        every { userRepository.save(capture(savedUser)) } answers { firstArg() }

        val result = userService.createAdmin(request)

        assertEquals("admin", result.name)
        assertEquals("admin@email.com", result.email)

        assertEquals(UserRole.ADMIN, savedUser.captured.role)
        assertEquals(encodedPassword, savedUser.captured.password)

        verify(exactly = 1) { userRepository.save(any()) }
    }

    @Test
    fun `should reject user creation when email is already registered`() {

        val request = UserCreateRequestDto(name = "user 1", email = "user1@email.com", password = "123456")

        every { userRepository.existsByEmail(request.email) } returns true

        val exception = assertThrows<AccessDeniedException> { userService.createUser(request) }

        assertEquals("This E-mail is already registered!", exception.message)

        verify(exactly = 0) { passwordEncoder.encode(any()) }
        verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `should update user name and encode the new password`() {

        val userId = 10L
        val existingUser = createUserEntity(id = userId, name = "old name", email = "user1@email.com")
        val request = UserUpdateRequestDto(name = "new name", password = "newPassword123")
        val encodePassword = "encoded-password"
        val savedUser = slot<User>()

        every { userRepository.findById(userId) } returns Optional.of(existingUser)
        every { passwordEncoder.encode(request.password) } returns encodePassword
        every { userRepository.save(capture(savedUser)) } answers { firstArg() }

        val result = userService.updateUser(request, userId)

        assertEquals(userId, result.id)
        assertEquals("new name", result.name)
        assertEquals("user1@email.com", result.email)

        assertEquals("new name", savedUser.captured.name)
        assertEquals(encodePassword, savedUser.captured.password)

        verify(exactly = 1) { userRepository.save(any()) }
    }

    @Test
    fun `should throw ResourceNotFoundException when updating a user that does not exist`() {

        val userId = 10L
        val request = UserUpdateRequestDto(name = "new name", password = "newPassword123")

        every { userRepository.findById(userId) } returns Optional.empty()

        val exception = assertThrows<ResourceNotFoundException> { userService.updateUser(request, userId) }

        assertEquals("User not found", exception.message)

        verify(exactly = 0) { passwordEncoder.encode(any()) }
        verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `should delete user`() {

        val userId = 10L
        val user = createUserEntity(id = userId, name = "user 1")

        every { userRepository.findById(userId) } returns Optional.of(user)
        every { userRepository.delete(user) } returns Unit

        userService.deleteUser(userId)

        verify(exactly = 1) { userRepository.delete(user) }
    }

    @Test
    fun `should throw ResourceNotFoundException when deleting a user that does not exist`() {
        val userId = 10L

        every { userRepository.findById(userId) } returns Optional.empty()

        val exception = assertThrows<ResourceNotFoundException> { userService.deleteUser(userId) }

        assertEquals("User not found", exception.message)

        verify(exactly = 0) { userRepository.delete(any()) }
    }
}