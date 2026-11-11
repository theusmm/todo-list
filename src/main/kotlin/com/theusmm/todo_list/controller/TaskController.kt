package com.theusmm.todo_list.controller

import com.theusmm.todo_list.dto.CustomPageDto
import com.theusmm.todo_list.dto.request.TaskCreateRequestDto
import com.theusmm.todo_list.dto.request.TaskUpdateRequestDto
import com.theusmm.todo_list.dto.response.TaskResponseDto
import com.theusmm.todo_list.entity.User
import com.theusmm.todo_list.service.TaskService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@Tag(name = "Tasks")
@RestController
@RequestMapping("/tasks")
class TaskController(private val service: TaskService) {

    // ================== ALL USERS ==================
    @Operation(
        summary = "Create a task",
        description = "Creates a task for the authenticated user"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Task created"),
            ApiResponse(responseCode = "400", description = "Request validation failed or request data is invalid"),
            ApiResponse(responseCode = "401", description = "Authentication is required or the token is invalid"),
            ApiResponse(responseCode = "500", description = "Unexpected error")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping()
    fun create(
        @Valid @RequestBody task: TaskCreateRequestDto,
        @AuthenticationPrincipal user: User
    ): ResponseEntity<TaskResponseDto> {
        val response = service.createTask(task, user)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @Operation(
        summary = "List my tasks",
        description = "Returns a paginated list of tasks owned by the authenticated user."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Task list returned, possibly empty"),
            ApiResponse(responseCode = "401", description = "Authentication is required or the token is invalid"),
            ApiResponse(responseCode = "500", description = "Unexpected error")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    fun getMyTasks(
        @ParameterObject
        @PageableDefault(page = 0, size = 10, sort = ["id"]) pageable: Pageable,
        @AuthenticationPrincipal user: User
    ): ResponseEntity<CustomPageDto<TaskResponseDto>> {
        return ResponseEntity.ok(service.getLoggedUserTasks(user, pageable))
    }

    @Operation(
        summary = "Update my task",
        description = "Updates a task owned by the authenticated user."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Task updated"),
            ApiResponse(responseCode = "400", description = "Invalid data or task belongs to another user"),
            ApiResponse(responseCode = "401", description = "Authentication is required or the token is invalid"),
            ApiResponse(responseCode = "404", description = "Task not found"),
            ApiResponse(responseCode = "500", description = "Unexpected error")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/me/{taskId}")
    fun updateMyTask(
        @Valid @RequestBody task: TaskUpdateRequestDto,
        @PathVariable taskId: Long,
        @AuthenticationPrincipal loggedUser: User
    ): ResponseEntity<TaskResponseDto>
    {
        val updatedTask = service.updateTask(task, taskId, loggedUser)
        return ResponseEntity.ok(updatedTask)
    }

    @Operation(
        summary = "Delete my task",
        description = "Deletes a task owned by the authenticated user."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Task deleted with no response body"),
            ApiResponse(responseCode = "400", description = "Request validation failed or request data is invalid"),
            ApiResponse(responseCode = "403", description = "User does not have permission to modify this task"),
            ApiResponse(responseCode = "404", description = "Task not found"),
            ApiResponse(responseCode = "500", description = "Unexpected error")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/me/{taskId}")
    fun deleteMyTask(
        @PathVariable taskId: Long,
        @AuthenticationPrincipal loggedUser: User
    ): ResponseEntity<Unit> {
        service.deleteTask(taskId, loggedUser)
        return ResponseEntity.noContent().build()
    }

    // ================== ADMIN ==================

    @Operation(
        summary = "Create a task for a user",
        description = "Creates a task for the specified user. Requires the ADMIN role."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Task created"),
            ApiResponse(responseCode = "400", description = "Request validation failed or request data is invalid"),
            ApiResponse(responseCode = "401", description = "Authentication is required or the token is invalid"),
            ApiResponse(responseCode = "403", description = "User does not have the ADMIN role"),
            ApiResponse(responseCode = "404", description = "User not found"),
            ApiResponse(responseCode = "500", description = "Unexpected error")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/admin/users/{userId}")
    fun createForUser(
        @PathVariable userId: Long,
        @Valid @RequestBody task: TaskCreateRequestDto
    ): ResponseEntity<TaskResponseDto> {
        val response = service.createTaskForUser(userId, task)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @Operation(
        summary = "List all tasks",
        description = "Returns a paginated list of all tasks. Requires the ADMIN role."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Task list returned, possibly empty"),
            ApiResponse(responseCode = "401", description = "Authentication is required or the token is invalid"),
            ApiResponse(responseCode = "403", description = "User does not have the ADMIN role"),
            ApiResponse(responseCode = "500", description = "Unexpected error")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/admin")
    fun getAll(
        @ParameterObject
        @PageableDefault(page = 0, size = 10, sort = ["id"]) pageable: Pageable
    ): ResponseEntity<CustomPageDto<TaskResponseDto>> {
        return ResponseEntity.ok(service.getAllTasks(pageable))
    }

    @Operation(
        summary = "Get a task by ID",
        description = "Returns the task with the specified ID. Requires the ADMIN role."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Task returned"),
            ApiResponse(responseCode = "401", description = "Authentication is required or the token is invalid"),
            ApiResponse(responseCode = "403", description = "User does not have the ADMIN role"),
            ApiResponse(responseCode = "404", description = "Task not found"),
            ApiResponse(responseCode = "500", description = "Unexpected error")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/admin/{taskId}")
    fun getById(@PathVariable taskId: Long): ResponseEntity<TaskResponseDto> {
        return ResponseEntity.ok(service.getTaskById(taskId))
    }

    @Operation(
        summary = "List tasks by user",
        description = "Returns a paginated list of tasks belonging to the specified user. Requires the ADMIN role."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Task list returned, possibly empty"),
            ApiResponse(responseCode = "401", description = "Authentication is required or the token is invalid"),
            ApiResponse(responseCode = "403", description = "User does not have the ADMIN role"),
            ApiResponse(responseCode = "404", description = "User not found"),
            ApiResponse(responseCode = "500", description = "Unexpected error")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/admin/users/{userId}")
    fun getByUserId(
        @PathVariable userId: Long,
        @ParameterObject
        @PageableDefault(page = 0, size = 10, sort = ["id"]) pageable: Pageable
    ): ResponseEntity<CustomPageDto<TaskResponseDto>> {
        return ResponseEntity.ok(service.getTasksByUserId(userId, pageable))
    }

    @Operation(
        summary = "Update a task by id",
        description = "Updates the task with the specified ID. Requires the ADMIN role."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Task updated"),
            ApiResponse(responseCode = "400", description = "Invalid data"),
            ApiResponse(responseCode = "401", description = "Authentication is required or the token is invalid"),
            ApiResponse(responseCode = "403", description = "User does not have the ADMIN role"),
            ApiResponse(responseCode = "404", description = "Task not found"),
            ApiResponse(responseCode = "500", description = "Unexpected error")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/admin/{taskId}")
    fun updateByTaskId(
        @Valid @RequestBody task: TaskUpdateRequestDto,
        @PathVariable taskId: Long,
        @AuthenticationPrincipal loggedUser: User
    ): ResponseEntity<TaskResponseDto>
    {
        val updatedTask = service.updateTask(task, taskId, loggedUser)
        return ResponseEntity.ok(updatedTask)
    }

    @Operation(
        summary = "Delete a task",
        description = "Deletes the task with the specified ID. Requires the ADMIN role."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Task deleted with no response body"),
            ApiResponse(responseCode = "401", description = "Authentication is required or the token is invalid"),
            ApiResponse(responseCode = "403", description = "User does not have the ADMIN role"),
            ApiResponse(responseCode = "404", description = "Task not found"),
            ApiResponse(responseCode = "500", description = "Unexpected error")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/admin/{taskId}")
    fun deleteByTaskId(
        @PathVariable taskId: Long,
        @AuthenticationPrincipal loggedUser: User
    ): ResponseEntity<Unit> {
        service.deleteTask(taskId, loggedUser)
        return ResponseEntity.noContent().build()
    }
}