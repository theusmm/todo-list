package com.theusmm.todo_list.mapper

import com.theusmm.todo_list.entity.Task
import com.theusmm.todo_list.entity.User
import com.theusmm.todo_list.dto.request.TaskCreateRequestDto
import com.theusmm.todo_list.dto.request.TaskUpdateRequestDto
import com.theusmm.todo_list.dto.response.TaskResponseDto

fun TaskCreateRequestDto.toCreateEntity(user: User): Task {
    return Task(
        title = this.title,
        description = this.description,
        priority = this.priority,
        dueDate = this.dueDate,
        user = user
    )
}

fun TaskUpdateRequestDto.toUpdateEntity(task: Task) {
    task.title = this.title
    task.description = this.description
    task.status = this.status
    task.priority = this.priority
    task.dueDate = this.dueDate
}

fun Task.toTaskResponseDto() = TaskResponseDto(
    id = this.id,
    title = this.title,
    description = this.description,
    status = this.status,
    priority = this.priority,
    dueDate = this.dueDate,
    userId = this.user.id
)