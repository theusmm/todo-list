package com.theusmm.todo_list.controller

import com.theusmm.todo_list.dto.request.TaskCreateRequestDto
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
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
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
            title = "Admin task",
            description = "Task description",
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
            .andExpect(jsonPath("$.title").value("Admin task"))
    }
}