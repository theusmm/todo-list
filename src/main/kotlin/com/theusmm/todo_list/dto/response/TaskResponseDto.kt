package com.theusmm.todo_list.dto.response

import com.theusmm.todo_list.entity.TaskPriority
import com.theusmm.todo_list.entity.TaskStatus
import java.io.Serializable
import java.time.LocalDate

data class TaskResponseDto(
    val id: Long?,
    val title: String,
    val description: String?,
    val status: TaskStatus,
    val priority: TaskPriority,
    val scheduledDate: LocalDate?,
    val userId: Long?
) : Serializable
