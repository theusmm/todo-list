package com.theusmm.todo_list.service

import com.theusmm.todo_list.entity.Task
import com.theusmm.todo_list.dto.request.TaskCreateRequestDto
import com.theusmm.todo_list.dto.request.TaskUpdateRequestDto
import com.theusmm.todo_list.exception.ResourceNotFoundException
import com.theusmm.todo_list.mapper.toCreateEntity
import com.theusmm.todo_list.mapper.toUpdateEntity
import com.theusmm.todo_list.repository.TaskRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class TaskService(
    private val repository: TaskRepository,
    private val userService: UserService
) {

    fun getAllTasks() = repository.findAll()

    fun getTaskById(taskId: Long): Task = repository.findById(taskId)
        .orElseThrow { ResourceNotFoundException("Task not found") }

    fun getTasksByUserId(userId: Long): List<Task> {
        userService.getUserById(userId)
        return repository.findByUserId(userId)
    }

    @Transactional
    fun createTask(task: TaskCreateRequestDto, userId: Long): Task {
        val user = userService.getUserById(userId)
        val newTask = task.toCreateEntity(user)

        user.addTask(newTask)
        return repository.save(newTask)
    }

    @Transactional
    fun updateTask(task: TaskUpdateRequestDto, taskId: Long): Task {
        val updatedTask = getTaskById(taskId)
        task.toUpdateEntity(updatedTask)
        return repository.save(updatedTask)
    }

    @Transactional
    fun deleteTask(taskId: Long) {
        val task = getTaskById(taskId)
        repository.delete(task)
    }
}