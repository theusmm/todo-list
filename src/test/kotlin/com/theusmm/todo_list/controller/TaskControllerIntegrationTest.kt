package com.theusmm.todo_list.controller

import com.theusmm.todo_list.dto.CustomPageDto
import com.theusmm.todo_list.dto.request.TaskCreateRequestDto
import com.theusmm.todo_list.dto.request.TaskUpdateRequestDto
import com.theusmm.todo_list.dto.response.TaskResponseDto
import com.theusmm.todo_list.entity.TaskPriority
import com.theusmm.todo_list.entity.TaskStatus
import com.theusmm.todo_list.entity.User
import com.theusmm.todo_list.entity.UserRole
import com.theusmm.todo_list.repository.UserRepository
import com.theusmm.todo_list.service.TaskService
import com.theusmm.todo_list.service.TokenService
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate

@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerIntegrationTest {

    @Autowired
    lateinit var  mockMvc: MockMvc

    @Autowired
    lateinit var tokenService: TokenService

    @field:MockitoBean
    lateinit var userRepository: UserRepository

    @field:MockitoBean
    lateinit var taskService: TaskService

    private fun tokenFor(user: User): String {
        Mockito.`when`(userRepository.findByEmail(user.email)).thenReturn(user)
        return tokenService.generateToken(user)
    }

    @Test
    fun `should reject requests to authenticated routes without token`() {
        mockMvc.perform(get("/tasks/me"))
            .andExpect(status().isForbidden)

        mockMvc.perform(get("/tasks"))
            .andExpect(status().isForbidden)
    }

    @Test
    fun `should return bad request when create task request is invalid`() {
        val token = tokenFor(createUserEntity(id = 10L))

        mockMvc.perform(
            post("/tasks")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "title": "",
                      "description": "Invalid task",
                      "priority": "MEDIUM",
                      "dueDate": "${LocalDate.now().plusDays(5)}"
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should allow authenticated user to create task`() {
        val user = createUserEntity(id = 10L, email = "user@email.com")
        val token = tokenFor(user)
        val request = TaskCreateRequestDto(
            title = "New task",
            description = "Task description",
            priority = TaskPriority.MEDIUM,
            dueDate = LocalDate.now().plusDays(5)
        )
        val createdTask = TaskResponseDto(
            id = 1L,
            title = request.title,
            description = request.description,
            status = TaskStatus.PENDING,
            priority = request.priority,
            dueDate = request.dueDate,
            userId = user.id
        )

        Mockito.`when`(taskService.createTask(request, user)).thenReturn(createdTask)

        mockMvc.perform(
            post("/tasks")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "title": "${request.title}",
                      "description": "${request.description}",
                      "priority": "MEDIUM",
                      "dueDate": "${request.dueDate}"
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.userId").value(10))
    }

    @Test
    fun `should reject regular user from admin task route`() {
        val user = createUserEntity(id = 10L, email = "user@email.com", role = UserRole.USER)
        val token = tokenFor(user)

        mockMvc.perform(
            get("/tasks/admin/5")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
        )
            .andExpect(status().isForbidden)
    }

    @Test
    fun `should allow admin to acces admin task route`() {
        val admin = createUserEntity(id = 1L, email = "admin@email.com", role = UserRole.ADMIN)
        val token = tokenFor(admin)
        val task = TaskResponseDto(
            id = 5L,
            title = "admin task",
            description = "task description",
            status = TaskStatus.PENDING,
            priority = TaskPriority.MEDIUM,
            dueDate = LocalDate.now().plusDays(5),
            userId = 20L
        )

        Mockito.`when`(taskService.getTaskById(5L)).thenReturn(task)

        mockMvc.perform(
            get("/tasks/admin/5")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(5))
            .andExpect(jsonPath("$.title").value("admin task"))
    }

    @Test
    fun `should return authenticated user task`() {
        val user = createUserEntity(id = 10L, email = "user@email.com")
        val token = tokenFor(user)
        val pageable = PageRequest.of(0, 10, Sort.by("id"))
        val page = CustomPageDto(
            content = listOf(
                TaskResponseDto(
                    id = 1L,
                    title = "my task",
                    description = "task description",
                    status = TaskStatus.PENDING,
                    priority = TaskPriority.MEDIUM,
                    dueDate = LocalDate.now().plusDays(5),
                    userId = user.id
                )
            ), pageNumber = 0, pageSize = 10, totalElements = 1, totalPages = 1, isLast = true
        )

        Mockito.`when`(taskService.getLoggedUserTasks(user, pageable)).thenReturn(page)

        mockMvc.perform(
            get("/tasks/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].id").value(1))
            .andExpect(jsonPath("$.content[0].title").value("my task"))
            .andExpect(jsonPath("$.pageNumber").value(0))
            .andExpect(jsonPath("$.pageSize").value(10))
    }

    @Test
    fun `should update authenticated user task`() {
        val user = createUserEntity(id = 10L, email = "user@email.com")
        val token = tokenFor(user)
        val request = TaskUpdateRequestDto(
            title = "Updated task",
            description = "Updated description",
            status = TaskStatus.IN_PROGRESS,
            priority = TaskPriority.HIGH,
            dueDate = LocalDate.now().plusDays(3)
        )
        val updatedTask = TaskResponseDto(
            id = 5L,
            title = request.title,
            description = request.description,
            status = request.status,
            priority = request.priority,
            dueDate = request.dueDate,
            userId = user.id
        )

        Mockito.`when`(taskService.updateTask(request, 5L, user)).thenReturn(updatedTask)

        mockMvc.perform(
            put("/tasks/me/5")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                {
                  "title": "Updated task",
                  "description": "Updated description",
                  "status": "IN_PROGRESS",
                  "priority": "HIGH",
                  "dueDate": "${request.dueDate}"
                }
                """.trimIndent()
                )
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(5))
            .andExpect(jsonPath("$.title").value("Updated task"))
            .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
    }

    @Test
    fun `should delete authenticated user task`() {
        val user = createUserEntity(id = 10L, email = "user@email.com")
        val token = tokenFor(user)

        mockMvc.perform(
            delete("/tasks/me/5")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
        )
            .andExpect(status().isNoContent)
    }

    @Test
    fun `should reject regular user from admin task operations`() {
        val user = createUserEntity(id = 10L, email = "user@email.com")
        val token = tokenFor(user)

        mockMvc.perform(
            post("/tasks/admin/users/20")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                {
                  "title": "Admin task",
                  "description": "Task description",
                  "priority": "MEDIUM",
                  "dueDate": "${LocalDate.now().plusDays(5)}"
                }
                """.trimIndent()
                )
        )
            .andExpect(status().isForbidden)

        mockMvc.perform(
            get("/tasks/admin")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
        )
            .andExpect(status().isForbidden)

        mockMvc.perform(
            get("/tasks/admin/users/20")
                .header(HttpHeaders.AUTHORIZATION,"Bearer $token")
        )
            .andExpect(status().isForbidden)

        mockMvc.perform(
            put("/tasks/admin/5")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                {
                  "title": "Updated task",
                  "description": "Updated description",
                  "status": "IN_PROGRESS",
                  "priority": "HIGH",
                  "dueDate": "${LocalDate.now().plusDays(3)}"
                }
                """.trimIndent()
                )
        )
            .andExpect(status().isForbidden)

        mockMvc.perform(
            delete("/tasks/admin/5")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
        )
            .andExpect(status().isForbidden)
    }

    @Test
    fun `should allow admin to create task for a user`() {
        val admin = createUserEntity(id = 1L, email = "admin@email.com", role = UserRole.ADMIN)
        val token = tokenFor(admin)
        val dueDate = LocalDate.now().plusDays(5)
        val request = TaskCreateRequestDto(
            title = "task for user",
            description = "created by admin",
            priority = TaskPriority.MEDIUM,
            dueDate = dueDate
        )
        val createdTask = TaskResponseDto(
            id = 8L,
            title = request.title,
            description = request.description,
            status = TaskStatus.PENDING,
            priority = request.priority,
            dueDate = request.dueDate,
            userId = 20L
        )

        Mockito.`when`(taskService.createTaskForUser(20L, request)).thenReturn(createdTask)

        mockMvc.perform(
            post("/tasks/admin/users/20")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                {
                  "title": "task for user",
                  "description": "created by admin",
                  "priority": "MEDIUM",
                  "dueDate": "$dueDate"
                }
                """.trimIndent()
                )
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(8))
            .andExpect(jsonPath("$.userId").value(20))
    }

    @Test
    fun `should return all tasks to admin`() {
        val admin = createUserEntity(id = 1L, email = "admin@email.com", role = UserRole.ADMIN)
        val token = tokenFor(admin)
        val pageable = PageRequest.of(0, 10, Sort.by("id"))
        val page = CustomPageDto(
            content = listOf(
                TaskResponseDto(
                    id = 3L,
                    title = "admin task",
                    description = "task description",
                    status = TaskStatus.PENDING,
                    priority = TaskPriority.MEDIUM,
                    dueDate = LocalDate.now().plusDays(5),
                    userId = 20L
                )
            ), pageNumber = 0, pageSize = 10, totalElements = 1, totalPages = 1, isLast = true
        )

        Mockito.`when`(taskService.getAllTasks(pageable)).thenReturn(page)

        mockMvc.perform(
            get("/tasks/admin")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].id").value(3))
            .andExpect(jsonPath("$.content[0].title").value("admin task"))
    }

    @Test
    fun `should return tasks by user id to admin`() {
        val admin = createUserEntity(id = 1L, email = "admin@email.com", role = UserRole.ADMIN)
        val token = tokenFor(admin)
        val pageable = PageRequest.of(0, 10, Sort.by("id"))
        val page = CustomPageDto(
            content = listOf(
                TaskResponseDto(
                    id = 4L,
                    title = "user task",
                    description = "task description",
                    status = TaskStatus.PENDING,
                    priority = TaskPriority.LOW,
                    dueDate = LocalDate.now().plusDays(2),
                    userId = 20L
                )
            ), pageNumber = 0, pageSize = 10, totalElements = 1, totalPages = 1, isLast = true
        )

        Mockito.`when`(taskService.getTasksByUserId(20L, pageable)).thenReturn(page)

        mockMvc.perform(
            get("/tasks/admin/users/20")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].userId").value(20))
            .andExpect(jsonPath("$.content[0].title").value("user task"))
    }

    @Test
    fun `should allow admin to update task by id`() {
        val admin = createUserEntity(id = 1L, email = "admin@email.com", role = UserRole.ADMIN)
        val token = tokenFor(admin)
        val request = TaskUpdateRequestDto(
            title = "admin updated task",
            description = "updated by admin",
            status = TaskStatus.COMPLETED,
            priority = TaskPriority.HIGH,
            dueDate = LocalDate.now().plusDays(1)
        )
        val updatedTask = TaskResponseDto(
            id = 5L,
            title = request.title,
            description = request.description,
            status = request.status,
            priority = request.priority,
            dueDate = request.dueDate,
            userId = 20L
        )

        Mockito.`when`(taskService.updateTask(request, 5L, admin)).thenReturn(updatedTask)

        mockMvc.perform(
            put("/tasks/admin/5")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                {
                  "title": "admin updated task",
                  "description": "updated by admin",
                  "status": "COMPLETED",
                  "priority": "HIGH",
                  "dueDate": "${request.dueDate}"
                }
                """.trimIndent()
                )
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(5))
            .andExpect(jsonPath("$.status").value("COMPLETED"))
    }

    @Test
    fun `should allow admin to delete task by id`() {
        val admin = createUserEntity(id = 1L, email = "admin@email.com", role = UserRole.ADMIN)
        val token = tokenFor(admin)

        mockMvc.perform(
            delete("/tasks/admin/5")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
        )
            .andExpect(status().isNoContent)
    }

    @Test
    fun `should return bad request when update task request is invalid`() {
        val user = createUserEntity(id = 10L, email = "user@email.com")
        val token = tokenFor(user)

        mockMvc.perform(
            put("/tasks/me/5")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                {
                  "title": "",
                  "description": "Invalid task",
                  "status": "PENDING",
                  "priority": "MEDIUM",
                  "dueDate": "${LocalDate.now().minusDays(1)}"
                }
                """.trimIndent()
                )
        )
            .andExpect(status().isBadRequest)
    }
}