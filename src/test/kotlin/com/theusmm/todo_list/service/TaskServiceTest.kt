package com.theusmm.todo_list.service

import com.theusmm.todo_list.entity.Task
import com.theusmm.todo_list.entity.UserRole
import com.theusmm.todo_list.exception.BusinessException
import com.theusmm.todo_list.exception.ResourceNotFoundException
import com.theusmm.todo_list.repository.TaskRepository
import com.theusmm.todo_list.repository.UserRepository
import com.theusmm.todo_list.util.createTaskCreateRequestDto
import com.theusmm.todo_list.util.createTaskEntity
import com.theusmm.todo_list.util.createTaskUpdateRequestDto
import com.theusmm.todo_list.util.createUserEntity
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.util.*
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class TaskServiceTest {

    @MockK
    lateinit var taskRepository: TaskRepository
    @MockK
    lateinit var userService: UserService
    @MockK
    lateinit var userRepository: UserRepository
    @InjectMockKs
    lateinit var taskService: TaskService

    @Test
    fun `should create task associated to the authenticated user`() {
        val loggedUser = createUserEntity(id = 12L)
        val request = createTaskCreateRequestDto(title = "create task", description = "authenticated user")

        every { taskRepository.save(any()) } answers { firstArg() }

        val response = taskService.createTask(request, loggedUser)

        assertEquals("create task", response.title)
        assertEquals("authenticated user", response.description)

        verify(exactly = 1) {
            taskRepository.save(match { savedTask ->
                savedTask.title == "create task" &&
                savedTask.description == "authenticated user" &&
                savedTask.user.id == loggedUser.id
            })
        }
    }

    @Test
    fun `should create task for existing user`() {
        val userId = 20L
        val targetUser = createUserEntity(id = userId, name = "target")
        val request = createTaskCreateRequestDto(title = "target user task")

        every { userRepository.findById(userId) } returns Optional.of(targetUser)
        every { taskRepository.save(any()) } answers { firstArg() }

        val response = taskService.createTaskForUser(request, userId)

        assertEquals("target user task", response.title)

        verify(exactly = 1) { taskRepository.save(match { savedTask ->
            savedTask.title == "target user task" &&
            savedTask.user.id == targetUser.id
        }) }

        verify(exactly = 1) { userRepository.findById(userId) }
    }

    @Test
    fun `should return tasks from the authenticated user`() {
        val loggedUser = createUserEntity(id = 15L)
        val pageable = PageRequest.of(0, 10)
        val tasks = listOf(
            createTaskEntity(id = 1L, title = "first task", user = loggedUser),
            createTaskEntity(id = 2L, title = "second task", user = loggedUser)
        )
        val taskPage = PageImpl(tasks, pageable, tasks.size.toLong())

        every { taskRepository.findByUserId(loggedUser.id!!, pageable) } returns taskPage

        val result = taskService.getLoggedUserTasks(loggedUser, pageable)

        assertEquals(2, result.content.size)
        assertEquals("first task", result.content[0].title)
        assertEquals("second task", result.content[1].title)

        verify(exactly = 1) { taskRepository.findByUserId(loggedUser.id!!, pageable) }
    }

    @Test
    fun `should throw ResourceNotFoundException when target user does not exist`() {
        val userId = 15L
        val request = createTaskCreateRequestDto()

        every { userRepository.findById(userId) } returns Optional.empty()

        val exception = assertThrows<ResourceNotFoundException> { taskService.createTaskForUser(request, userId) }

        assertEquals("User not found", exception.message)

        verify(exactly = 0) { taskRepository.save(any()) }
    }

    @Test
    fun `should return empty page when user does not have tasks`() {
        val loggedUser = createUserEntity(id = 14L)
        val pageable = PageRequest.of(0, 10)
        val emptyPage = PageImpl<Task>(emptyList(), pageable, 0)

        every { taskRepository.findByUserId(loggedUser.id!!, pageable) } returns emptyPage

        val result = taskService.getLoggedUserTasks(loggedUser, pageable)

        assertEquals(0, result.content.size)

        verify(exactly = 1) { taskRepository.findByUserId(loggedUser.id!!, pageable) }
    }

    @Test
    fun `should let user update their own task`() {
        val owner = createUserEntity(id = 10L, name = "owner")
        val task = createTaskEntity(id = 5L, title = "old title", user = owner)

        val request = createTaskUpdateRequestDto(title = "new title")

        every { taskRepository.findById(5L) } returns Optional.of(task)
        every { taskRepository.save(any()) } answers { firstArg() }

        val response = taskService.updateTask(request, 5L, owner)

        assertEquals("new title", response.title)

        verify(exactly = 1) {
            taskRepository.save(match { savedTask ->
                savedTask.id == 5L &&
                savedTask.title == "new title" &&
                savedTask.user.id == owner.id
            })
        }
    }

    @Test
    fun `should let admin update other users task`() {
        val owner = createUserEntity(id = 10L, name = "owner")
        val admin = createUserEntity(id = 30L, name = "admin", email = "admin@email.com", role = UserRole.ADMIN)
        val task = createTaskEntity(id = 5L, title = "old title", user = owner)

        val request =  createTaskUpdateRequestDto(title = "new title")

        every { taskRepository.findById(5L) } returns Optional.of(task)
        every { taskRepository.save(any()) } answers { firstArg() }

        val response = taskService.updateTask(request, 5L, admin)

        assertEquals("new title", response.title)

        verify(exactly = 1) {
            taskRepository.save(match { savedTask ->
                savedTask.id == 5L &&
                savedTask.title == "new title" &&
                savedTask.user.id == owner.id
            })
        }
    }

    @Test
    fun `should not let user update task if it is not owner nor admin`() {
        val owner = createUserEntity(id = 10L, name = "owner")
        val invader = createUserEntity(id = 20L, name = "invader")
        val task = createTaskEntity(id = 5L, user = owner)

        val request = createTaskUpdateRequestDto(title = "can not update")

        every { taskRepository.findById(5L) } returns Optional.of(task)

        assertThrows<BusinessException> { taskService.updateTask(request, 5L, invader) }

        verify(exactly = 0) { taskRepository.save(any()) }
    }

    @Test
    fun `should throw ResourceNotFoundException when update an unexistent task`() {
        val taskId = 500L
        val user = createUserEntity()

        val request = createTaskUpdateRequestDto()

        every { taskRepository.findById(taskId) } returns Optional.empty()

        assertThrows<ResourceNotFoundException> { taskService.updateTask(request, taskId, user) }

        verify(exactly = 0) { taskRepository.save(any()) }
    }

    @Test
    fun `should let user delete their own task`() {
        val owner = createUserEntity(id = 10L, name = "owner")
        val task = createTaskEntity(id = 5L, user = owner)

        every { taskRepository.findById(5L) } returns Optional.of(task)
        every { taskRepository.delete(task) } returns Unit

        taskService.deleteTask(5L, owner)

        verify(exactly = 1) { taskRepository.delete(task) }
    }

    @Test
    fun `should let admin delete other users task`() {
        val owner = createUserEntity(id = 10L, name = "owner")
        val admin = createUserEntity(id = 30L, name = "admin", email = "admin@email.com", role = UserRole.ADMIN)
        val task = createTaskEntity(id = 5L, user = owner)

        every { taskRepository.findById(5L) } returns Optional.of(task)
        every { taskRepository.delete(task) } returns Unit

        taskService.deleteTask(5L, admin)

        verify(exactly = 1) { taskRepository.delete(task) }
    }

    @Test
    fun `should not let user delete task if it is not owner nor admin`() {
        val owner = createUserEntity(id = 10L, name = "owner")
        val invader = createUserEntity(id = 20L, name = "invader")
        val task = createTaskEntity(id = 5L, user = owner)

        every { taskRepository.findById(5L) } returns Optional.of(task)

        assertThrows<BusinessException> { taskService.deleteTask(5L, invader) }

        verify(exactly = 0) { taskRepository.delete(any()) }
    }

    @Test
    fun `should throw ResourceNotFoundException when delete an unexistent task`() {
        val taskId = 500L
        val user = createUserEntity()

        every { taskRepository.findById(taskId) } returns Optional.empty()

        assertThrows<ResourceNotFoundException> { taskService.deleteTask(taskId, user) }

        verify(exactly = 0) { taskRepository.delete(any()) }
    }

    @Test
    fun `should return all tasks for the requested page`() {
        val user = createUserEntity(id = 10L)
        val pageable = PageRequest.of(0, 10)
        val tasks = listOf(
            createTaskEntity(id = 1L, title = "first task", user = user),
            createTaskEntity(id = 2L, title = "second task", user = user)
        )

        val tasksPage = PageImpl(tasks, pageable, tasks.size.toLong())

        every { taskRepository.findAll(pageable) } returns tasksPage

        val result = taskService.getAllTasks(pageable)

        assertEquals(2, result.content.size)
        assertEquals("first task", result.content[0].title)
        assertEquals("second task", result.content[1].title)

        verify(exactly = 1) { taskRepository.findAll(pageable) }
    }

    @Test
    fun `should return an empty page when there are no tasks`() {
        val pageable = PageRequest.of(0, 10)
        val emptyPage = PageImpl<Task>(emptyList(), pageable, 0)

        every { taskRepository.findAll(pageable) } returns emptyPage
        val result = taskService.getAllTasks(pageable)

        assertEquals(0, result.content.size)

        verify(exactly = 1) { taskRepository.findAll(pageable) }
    }

    @Test
    fun `should return task successfully`() {
        val taskId = 5L
        val user = createUserEntity(id = 10L)
        val task = createTaskEntity(id = taskId, title = "task title", user = user)

        every { taskRepository.findById(taskId) } returns Optional.of(task)

        val result = taskService.getTaskById(taskId)

        assertEquals(taskId, result.id)
        assertEquals("task title", result.title)

        verify(exactly = 1) { taskRepository.findById(taskId) }
    }

    @Test
    fun `should throw ResourceNotFoundException when task does not exist`() {
        val taskId = 500L

        every { taskRepository.findById(taskId) } returns Optional.empty()

        val exception = assertThrows<ResourceNotFoundException> { taskService.getTaskById(taskId) }

        assertEquals("Task not found", exception.message)

        verify(exactly = 1) { taskRepository.findById(taskId) }
    }

    @Test
    fun `should successfully return user tasks`() {
        val userId = 10L
        val pageable = PageRequest.of(0, 10)
        val user = createUserEntity(id = userId)
        val tasks = listOf(createTaskEntity(id = 1L, title = "user task", user = user))
        val taskPage = PageImpl(tasks, pageable, tasks.size.toLong())

        every { userService.getUserById(userId) } returns mockk()
        every { taskRepository.findByUserId(userId, pageable) } returns taskPage

        val result = taskService.getTasksByUserId(userId, pageable)

        assertEquals(1, result.content.size)
        assertEquals("user task", result.content[0].title)

        verify(exactly = 1) { userService.getUserById(userId) }
        verify(exactly = 1) { taskRepository.findByUserId(userId, pageable) }
    }

    @Test
    fun `should return empty page when user has no tasks`() {
        val userId = 10L
        val pageable = PageRequest.of(0, 10)
        val emptyPage = PageImpl<Task>(emptyList(), pageable, 0)

        every { userService.getUserById(userId) } returns mockk()
        every { taskRepository.findByUserId(userId, pageable) } returns emptyPage

        val result = taskService.getTasksByUserId(userId, pageable)

        assertEquals(0, result.content.size)

        verify(exactly = 1) { userService.getUserById(userId) }
        verify(exactly = 1) { taskRepository.findByUserId(userId, pageable) }
    }

    @Test
    fun `should not search tasks when user does not exist`() {
        val userId = 10L
        val pageable = PageRequest.of(0, 10)

        every { userService.getUserById(userId) } throws ResourceNotFoundException("User not found")

        assertThrows<ResourceNotFoundException> { taskService.getTasksByUserId(userId, pageable) }

        verify(exactly = 0) { taskRepository.findByUserId(any(), any()) }
    }
}