package com.theusmm.todo_list.controller

import com.theusmm.todo_list.dto.CustomPageDto
import com.theusmm.todo_list.dto.request.TaskCreateRequestDto
import com.theusmm.todo_list.dto.request.TaskUpdateRequestDto
import com.theusmm.todo_list.dto.response.TaskResponseDto
import com.theusmm.todo_list.mapper.toTaskResponseDto
import com.theusmm.todo_list.service.TaskService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/tasks")
class TaskController(private val service: TaskService) {

    @GetMapping
    fun getAll(
        @PageableDefault(page = 0, size = 10, sort = ["id"]) pageable: Pageable
    ): ResponseEntity<CustomPageDto<TaskResponseDto>> {

        val responsePage = service.getAllTasks(pageable)
        return ResponseEntity.ok(responsePage)
    }

    @GetMapping("/{taskId}")
    fun getById(@PathVariable taskId: Long): ResponseEntity<TaskResponseDto> {
        val task = service.getTaskById(taskId)
        return ResponseEntity.ok(task)
    }

    @GetMapping("/user/{userId}")
    fun getByUserId(
        @PathVariable userId: Long,
        @PageableDefault(page = 0, size = 10, sort = ["id"]) pageable: Pageable
    ): ResponseEntity<CustomPageDto<TaskResponseDto>> {

        val responsePage = service.getTasksByUserId(userId, pageable)
        return ResponseEntity.ok(responsePage)
    }

    @PostMapping("/user/{userId}")
    fun create(
        @Valid @RequestBody task: TaskCreateRequestDto,
        @PathVariable userId: Long): ResponseEntity<TaskResponseDto>
    {
        val newTask = service.createTask(task, userId)
        return ResponseEntity.status(HttpStatus.CREATED).body(newTask)
    }

    @PutMapping("/{taskId}")
    fun update(
        @Valid @RequestBody task: TaskUpdateRequestDto,
        @PathVariable taskId: Long): ResponseEntity<TaskResponseDto>
    {
        val updatedTask = service.updateTask(task, taskId)
        return ResponseEntity.ok(updatedTask)
    }

    @DeleteMapping("/{taskId}")
    fun delete(@PathVariable taskId: Long): ResponseEntity<Unit> {
        service.deleteTask(taskId)
        return ResponseEntity.noContent().build()
    }
}
