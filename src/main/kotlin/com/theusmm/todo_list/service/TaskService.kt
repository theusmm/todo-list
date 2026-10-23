package com.theusmm.todo_list.service

import com.theusmm.todo_list.dto.CustomPageDto
import com.theusmm.todo_list.dto.request.TaskCreateRequestDto
import com.theusmm.todo_list.dto.request.TaskUpdateRequestDto
import com.theusmm.todo_list.dto.response.TaskResponseDto
import com.theusmm.todo_list.entity.Task
import com.theusmm.todo_list.entity.User
import com.theusmm.todo_list.entity.UserRole
import com.theusmm.todo_list.exception.BusinessException
import com.theusmm.todo_list.exception.ResourceNotFoundException
import com.theusmm.todo_list.mapper.toCreateEntity
import com.theusmm.todo_list.mapper.toResponseDto
import com.theusmm.todo_list.mapper.toUpdateEntity
import com.theusmm.todo_list.repository.TaskRepository
import com.theusmm.todo_list.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class TaskService(
    private val repository: TaskRepository,
    private val userService: UserService,
    private val userRepository: UserRepository,
) {

    // ================== ALL USERS ==================

    @CacheEvict(cacheNames = ["tasks", "tasksByUserId", "tasksByLoggedUser"], allEntries = true)
    @Transactional
    fun createTask(task: TaskCreateRequestDto, loggedUser: User): TaskResponseDto {
        val newTask = task.toCreateEntity(loggedUser)
        return repository.save(newTask).toResponseDto()
    }

    @CacheEvict(cacheNames = ["tasks", "tasksByUserId", "tasksByLoggedUser"], allEntries = true)
    @Transactional
    fun createTaskForUser(userId: Long, task: TaskCreateRequestDto): TaskResponseDto {
        val targetUser = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }

        val newTask = task.toCreateEntity(targetUser)
        return repository.save(newTask).toResponseDto()
    }

    @Cacheable(
        cacheNames = ["tasksByLoggedUser"],
        key = "#loggedUser.email + '-' + #pageable.pageNumber + '-' + #pageable.pageSize"
    )
    fun getLoggedUserTasks(loggedUser: User, pageable: Pageable): CustomPageDto<TaskResponseDto> {
        val tasksPage = repository.findByUserId(loggedUser.id!!, pageable)
        return CustomPageDto(tasksPage.map { it.toResponseDto() })
    }

    @CacheEvict(cacheNames = ["tasks", "tasksByUserId", "tasksByLoggedUser"], allEntries = true)
    @Transactional
    fun updateTask(task: TaskUpdateRequestDto, taskId: Long, loggedUser: User): TaskResponseDto {

        val updateTask = checkIfTaskExists(taskId)
        validateTaskOwnerOrAdmin(updateTask, loggedUser)
        task.toUpdateEntity(updateTask)

        return repository.save(updateTask).toResponseDto()
    }

    @CacheEvict(cacheNames = ["tasks", "tasksByUserId", "tasksByLoggedUser"], allEntries = true)
    @Transactional
    fun deleteTask(taskId: Long, loggedUser: User) {

        val deleteTask = checkIfTaskExists(taskId)
        validateTaskOwnerOrAdmin(deleteTask, loggedUser)

        repository.delete(deleteTask)
    }

    // ================== ADMIN ==================

    @Cacheable(cacheNames = ["tasks"], key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    fun getAllTasks(pageable: Pageable): CustomPageDto<TaskResponseDto> {
        val tasksPage = repository.findAll(pageable).map { it.toResponseDto() }
        return CustomPageDto(tasksPage)
    }

fun getTaskById(taskId: Long): TaskResponseDto {
        val task = checkIfTaskExists(taskId)
        return task.toResponseDto()
    }

    @Cacheable(
        cacheNames = ["tasksByUserId"],
        key = "#userId + '-' + #pageable.pageNumber + '-' + #pageable.pageSize"
    )
    fun getTasksByUserId(userId: Long, pageable: Pageable): CustomPageDto<TaskResponseDto> {
        userService.getUserById(userId)
        val responseTask = repository.findByUserId(userId, pageable).map { it.toResponseDto() }
        return CustomPageDto(responseTask)
    }

    // ====================================

    fun checkIfTaskExists(taskId: Long): Task = repository.findById(taskId)
        .orElseThrow { ResourceNotFoundException("Task not found") }

    private fun validateTaskOwnerOrAdmin(task: Task, loggedUser: User) {
        if (task.user.id != loggedUser.id && loggedUser.role != UserRole.ADMIN) {
            throw BusinessException("You cannot modify this task")
        }
    }
}