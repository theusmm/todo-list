package com.theusmm.todo_list.controller

import com.theusmm.todo_list.dto.request.TaskCreateRequestDto
import com.theusmm.todo_list.dto.request.TaskUpdateRequestDto
import com.theusmm.todo_list.dto.response.TaskResponseDto
import com.theusmm.todo_list.mapper.toTaskResponseDto
import com.theusmm.todo_list.service.TaskService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/tasks")
class TaskController(private val service: TaskService) {

    @GetMapping
    fun getAll(): ResponseEntity<List<TaskResponseDto>> {
        val tasks = service.getAllTasks().map { it.toTaskResponseDto() }
        return ResponseEntity.ok(tasks)
    }

    @GetMapping("/{taskId}")
    fun getById(@PathVariable taskId: Long): ResponseEntity<TaskResponseDto> {
        val task = service.getTaskById(taskId)
        return ResponseEntity.ok(task.toTaskResponseDto())
    }

    @GetMapping("/user/{userId}")
    fun getByUserId(@PathVariable userId: Long): ResponseEntity<List<TaskResponseDto>> {
        val tasks = service.getTasksByUserId(userId).map { it.toTaskResponseDto() }
        return ResponseEntity.ok(tasks)
    }

    @PostMapping("/user/{userId}")
    fun create(
        @Valid @RequestBody task: TaskCreateRequestDto,
        @PathVariable userId: Long): ResponseEntity<TaskResponseDto>
    {
        val newTask = service.createTask(task, userId)
        return ResponseEntity.status(HttpStatus.CREATED).body(newTask.toTaskResponseDto())
    }

    @PutMapping("/{taskId}")
    fun update(
        @Valid @RequestBody task: TaskUpdateRequestDto,
        @PathVariable taskId: Long): ResponseEntity<TaskResponseDto>
    {
        val updatedTask = service.updateTask(task, taskId)
        return ResponseEntity.ok(updatedTask.toTaskResponseDto())
    }

    @DeleteMapping("/{taskId}")
    fun delete(@PathVariable taskId: Long): ResponseEntity<Unit> {
        service.deleteTask(taskId)
        return ResponseEntity.noContent().build()
    }
}
