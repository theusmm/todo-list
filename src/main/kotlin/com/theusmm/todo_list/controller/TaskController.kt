package com.theusmm.todo_list.controller

import com.theusmm.todo_list.dto.CustomPageDto
import com.theusmm.todo_list.dto.request.TaskCreateRequestDto
import com.theusmm.todo_list.dto.request.TaskUpdateRequestDto
import com.theusmm.todo_list.dto.response.TaskResponseDto
import com.theusmm.todo_list.entity.User
import com.theusmm.todo_list.mapper.toTaskResponseDto
import com.theusmm.todo_list.service.TaskService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/tasks")
class TaskController(private val service: TaskService) {

    // ================== ALL USERS ==================
    @PostMapping()
    fun create(
        @Valid @RequestBody task: TaskCreateRequestDto,
        @AuthenticationPrincipal user: User
    ): ResponseEntity<TaskResponseDto> {
        val response = service.createTask(task, user)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping("/me")
    fun getMyTasks(
        @PageableDefault(page = 0, size = 10, sort = ["id"]) pageable: Pageable,
        @AuthenticationPrincipal user: User
    ): ResponseEntity<CustomPageDto<TaskResponseDto>> {
        return ResponseEntity.ok(service.getLoggedUserTasks(user, pageable))
    }

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

    @DeleteMapping("/me/{taskId}")
    fun deleteMyTask(
        @PathVariable taskId: Long,
        @AuthenticationPrincipal loggedUser: User
    ): ResponseEntity<Unit> {
        service.deleteTask(taskId, loggedUser)
        return ResponseEntity.noContent().build()
    }

    // ================== ADMIN ==================

    @GetMapping("/admin")
    fun getAll(
        @PageableDefault(page = 0, size = 10, sort = ["id"]) pageable: Pageable
    ): ResponseEntity<CustomPageDto<TaskResponseDto>> {
        return ResponseEntity.ok(service.getAllTasks(pageable))
    }

    @GetMapping("/admin/{taskId}")
    fun getById(@PathVariable taskId: Long): ResponseEntity<TaskResponseDto> {
        return ResponseEntity.ok(service.getTaskById(taskId))
    }

    @GetMapping("/admin/user/{userId}")
    fun getByUserId(
        @PathVariable userId: Long,
        @PageableDefault(page = 0, size = 10, sort = ["id"]) pageable: Pageable
    ): ResponseEntity<CustomPageDto<TaskResponseDto>> {
        return ResponseEntity.ok(service.getTasksByUserId(userId, pageable))
    }

    @PutMapping("/admin/{taskId}")
    fun updateByTask(
        @Valid @RequestBody task: TaskUpdateRequestDto,
        @PathVariable taskId: Long,
        @AuthenticationPrincipal loggedUser: User
    ): ResponseEntity<TaskResponseDto>
    {
        val updatedTask = service.updateTask(task, taskId, loggedUser)
        return ResponseEntity.ok(updatedTask)
    }

    @DeleteMapping("/admin/{taskId}")
    fun deleteByTask(
        @PathVariable taskId: Long,
        @AuthenticationPrincipal loggedUser: User
    ): ResponseEntity<Unit> {
        service.deleteTask(taskId, loggedUser)
        return ResponseEntity.noContent().build()
    }
}