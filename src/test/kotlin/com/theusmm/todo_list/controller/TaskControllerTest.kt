package com.theusmm.todo_list.controller

import com.theusmm.todo_list.dto.CustomPageDto
import com.theusmm.todo_list.dto.response.TaskResponseDto
import com.theusmm.todo_list.entity.TaskPriority
import com.theusmm.todo_list.entity.TaskStatus
import com.theusmm.todo_list.entity.UserRole
import com.theusmm.todo_list.service.TaskService
import com.theusmm.todo_list.util.createTaskCreateRequestDto
import com.theusmm.todo_list.util.createTaskUpdateRequestDto
import com.theusmm.todo_list.util.createUserEntity
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
import java.time.LocalDate
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class TaskControllerTest {

    @MockK
    lateinit var taskService: TaskService
    @InjectMockKs
    lateinit var taskController: TaskController

    @Test
    fun `should create task for authenticated user and return created status`() {
        val loggedUser = createUserEntity(id = 10L)
        val request = createTaskCreateRequestDto(title = "new task")
        val expectedResponse = TaskResponseDto(
            id = 1L,
            title = "new task",
            description = request.description,
            status = TaskStatus.PENDING,
            priority = request.priority,
            dueDate = request.dueDate,
            userId = loggedUser.id
            )

        every { taskService.createTask(request, loggedUser) } returns expectedResponse

        val response = taskController.create(request, loggedUser)

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertEquals(expectedResponse, response.body)

        verify(exactly = 1) { taskService.createTask(request, loggedUser) }
    }

    @Test
    fun `should return authenticated user tasks with ok status`() {
        val loggedUser = createUserEntity(id = 15L)
        val pageable = PageRequest.of(0, 10, Sort.by("id"))
        val expectedPage = CustomPageDto(
            content = listOf(
                TaskResponseDto(
                    id = 1L,
                    title = "task 1",
                    description = "description 1",
                    status = TaskStatus.PENDING,
                    priority = TaskPriority.MEDIUM,
                    dueDate = LocalDate.now().plusDays(5),
                    userId = loggedUser.id
                ),
                TaskResponseDto(
                    id = 1L,
                    title = "task 2",
                    description = "description 2",
                    status = TaskStatus.PENDING,
                    priority = TaskPriority.MEDIUM,
                    dueDate = LocalDate.now().plusDays(10),
                    userId = loggedUser.id
                )
            ), pageNumber = 0, pageSize = 10, totalElements = 2L, totalPages = 1, isLast = true
        )

        every { taskService.getLoggedUserTasks(loggedUser, pageable) } returns expectedPage

        val response = taskController.getMyTasks(pageable, loggedUser)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(expectedPage, response.body)

        verify(exactly = 1) { taskService.getLoggedUserTasks(loggedUser, pageable) }
    }

    @Test
    fun `should update authenticated user task and return ok status`() {
        val taskId = 5L
        val loggedUser = createUserEntity(id = 10L)
        val request = createTaskUpdateRequestDto(title = "updated task")
        val expectedResponse = TaskResponseDto(
            id = taskId,
            title = "updated task",
            description = request.description,
            status = request.status,
            priority = request.priority,
            dueDate = request.dueDate,
            userId = loggedUser.id
        )

        every { taskService.updateTask(request, taskId, loggedUser) } returns expectedResponse

        val response = taskController.updateMyTask(request, taskId, loggedUser)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(expectedResponse, response.body)

        verify(exactly = 1) { taskService.updateTask(request, taskId, loggedUser) }
    }

    @Test
    fun `should delete authenticated user task and return no content status`() {
        val taskId = 3L
        val loggedUser = createUserEntity(id = 10L)

        every { taskService.deleteTask(taskId, loggedUser) } returns Unit

        val response = taskController.deleteMyTask(taskId, loggedUser)

        assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
        assertNull(response.body)

        verify(exactly = 1) { taskService.deleteTask(taskId, loggedUser) }
    }

    @Test
    fun `should create task for specified user and return created status`() {
        val userId = 15L
        val request = createTaskCreateRequestDto(title = "target user task")
        val expectedResponse = TaskResponseDto(
            id = 1L,
            title = request.title,
            description = request.description,
            status = TaskStatus.PENDING,
            priority = request.priority,
            dueDate = request.dueDate,
            userId = userId
        )

        every { taskService.createTaskForUser(userId, request) } returns expectedResponse

        val response = taskController.createForUser(userId, request)

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertEquals(expectedResponse, response.body)

        verify(exactly = 1) { taskService.createTaskForUser(userId, request) }
    }

    @Test
    fun `should return all tasks with ok status`() {
        val pageable = PageRequest.of(0, 10, Sort.by("id"))
        val expectedPage = CustomPageDto(
            content = listOf(
                TaskResponseDto(
                    id = 1L,
                    title = "task 1",
                    description = "description 1",
                    status = TaskStatus.PENDING,
                    priority = TaskPriority.MEDIUM,
                    dueDate = LocalDate.now().plusDays(5),
                    userId = 12L
                ),
                TaskResponseDto(
                    id = 2L,
                    title = "task 2",
                    description = "description 2",
                    status = TaskStatus.PENDING,
                    priority = TaskPriority.MEDIUM,
                    dueDate = LocalDate.now().plusDays(10),
                    userId = 15L
                )
            ), pageNumber = 0, pageSize = 10, totalElements = 2L, totalPages = 1, isLast = true
        )

        every { taskService.getAllTasks(pageable) } returns expectedPage

        val response = taskController.getAll(pageable)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(expectedPage, response.body)

        verify(exactly = 1) { taskService.getAllTasks(pageable) }
    }

    @Test
    fun `should return task by id with ok status`() {
        val taskId = 2L
        val expectedTask = TaskResponseDto(
            id = taskId,
            title = "test task",
            description = "test description",
            status = TaskStatus.PENDING,
            priority = TaskPriority.MEDIUM,
            dueDate = LocalDate.now().plusDays(5),
            userId = 6L
        )

        every { taskService.getTaskById(taskId) } returns expectedTask

        val response = taskController.getById(taskId)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(expectedTask, response.body)

        verify(exactly = 1) { taskService.getTaskById(taskId) }
    }

    @Test
    fun `should return tasks by user id with ok status`() {
        val userId = 15L
        val pageable = PageRequest.of(0, 10, Sort.by("id"))
        val expectedPage = CustomPageDto(
            content = listOf(
                TaskResponseDto(
                    id = 1L,
                    title = "task 1",
                    description = "description 1",
                    status = TaskStatus.PENDING,
                    priority = TaskPriority.MEDIUM,
                    dueDate = LocalDate.now().plusDays(5),
                    userId = userId
                ),
                TaskResponseDto(
                    id = 1L,
                    title = "task 2",
                    description = "description 2",
                    status = TaskStatus.PENDING,
                    priority = TaskPriority.MEDIUM,
                    dueDate = LocalDate.now().plusDays(10),
                    userId = userId
                )
            ), pageNumber = 0, pageSize = 10, totalElements = 2L, totalPages = 1, isLast = true
        )

        every { taskService.getTasksByUserId(userId, pageable) } returns expectedPage

        val response = taskController.getByUserId(userId, pageable)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(expectedPage, response.body)

        verify(exactly = 1) { taskService.getTasksByUserId(userId, pageable) }
    }

    @Test
    fun `should update task by id and return ok status`() {
        val taskId = 5L
        val adminUser = createUserEntity(id = 10L, role = UserRole.USER)
        val request = createTaskUpdateRequestDto(title = "updated task")
        val expectedResponse = TaskResponseDto(
            id = taskId,
            title = "updated task",
            description = request.description,
            status = request.status,
            priority = request.priority,
            dueDate = request.dueDate,
            userId = 15L
        )

        every { taskService.updateTask(request, taskId, adminUser) } returns expectedResponse

        val response = taskController.updateByTaskId(request, taskId, adminUser)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(expectedResponse, response.body)

        verify(exactly = 1) { taskService.updateTask(request, taskId, adminUser) }
    }

    @Test
    fun `should delete task by id and return no content status`() {
        val taskId = 3L
        val adminUser = createUserEntity(id = 10L, role = UserRole.ADMIN)

        every { taskService.deleteTask(taskId, adminUser) } returns Unit

        val response = taskController.deleteByTaskId(taskId, adminUser)

        assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
        assertNull(response.body)

        verify(exactly = 1) { taskService.deleteTask(taskId, adminUser) }
    }
}