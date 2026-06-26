package com.theusmm.todo_list.service

import com.theusmm.todo_list.dto.CustomPageDto
import com.theusmm.todo_list.dto.request.TaskCreateRequestDto
import com.theusmm.todo_list.dto.request.TaskUpdateRequestDto
import com.theusmm.todo_list.dto.response.TaskResponseDto
import com.theusmm.todo_list.entity.Task
import com.theusmm.todo_list.mapper.toCreateEntity
import com.theusmm.todo_list.mapper.toTaskResponseDto
import com.theusmm.todo_list.mapper.toUpdateEntity
import com.theusmm.todo_list.repository.TaskRepository
import jakarta.transaction.Transactional
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class TaskService(
    private val repository: TaskRepository,
    private val userService: UserService
) {
    @Cacheable(cacheNames = ["tasks"], key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    fun getAllTasks(pageable: Pageable): CustomPageDto<TaskResponseDto> {
        val tasksPage = repository.findAll(pageable).map { it.toTaskResponseDto() }
        return CustomPageDto(tasksPage)
    }

fun getTaskById(taskId: Long): TaskResponseDto {
        val task = checkIfTaskExists(taskId)
        val responseTask = task.toTaskResponseDto()
        return responseTask
    }

    @Cacheable(
        cacheNames = ["tasksByUserId"],
        key = "#userId + '-' + #pageable.PageNumber + '-' + #pageable.pageSize"
    )
    fun getTasksByUserId(userId: Long, pageable: Pageable): CustomPageDto<TaskResponseDto> {
        userService.getUserById(userId)
        val responseTask = repository.findByUserId(userId, pageable).map { it.toTaskResponseDto() }
        return CustomPageDto(responseTask)
    }

    @CacheEvict(cacheNames = ["tasks", "tasksByUserId"], allEntries = true)
    @Transactional
    fun createTask(task: TaskCreateRequestDto, userId: Long): TaskResponseDto {
        val user = userService.checkIfUserExists(userId)
        val newTask = task.toCreateEntity(user)

        user.addTask(newTask)
        val savedTask = repository.save(newTask).toTaskResponseDto()
        return savedTask
    }

    @CacheEvict(cacheNames = ["tasks", "tasksByUserId"], allEntries = true)
    @Transactional
    fun updateTask(task: TaskUpdateRequestDto, taskId: Long): TaskResponseDto {
        val updatedTask = checkIfTaskExists(taskId)
        task.toUpdateEntity(updatedTask)

        val savedTask = repository.save(updatedTask).toTaskResponseDto()
        return savedTask
    }

    @CacheEvict(cacheNames = ["tasks", "tasksByUserId"], allEntries = true)
    @Transactional
    fun deleteTask(taskId: Long) {
        val task = checkIfTaskExists(taskId)
        repository.delete(task)
    }

    fun checkIfTaskExists(taskId: Long): Task = repository.findById(taskId)
        .orElseThrow { RuntimeException("Task not found") }
}