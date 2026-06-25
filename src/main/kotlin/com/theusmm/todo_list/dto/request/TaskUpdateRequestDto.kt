package com.theusmm.todo_list.dto.request

import com.theusmm.todo_list.entity.TaskPriority
import com.theusmm.todo_list.entity.TaskStatus
import jakarta.validation.constraints.FutureOrPresent
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDate

data class TaskUpdateRequestDto(
    @field:NotBlank(message = "Title is required")
    @field:Size(max = 100, message = "Title must contain a maximum of 100 characters.")
    val title: String,

    val description: String?,

    @field:NotNull(message = "Status is required")
    val status: TaskStatus,

    @field:NotNull(message = "Priority is required")
    val priority: TaskPriority,

    @field:NotNull(message = "Scheduled date is required")
    @field:FutureOrPresent(message = "Scheduled date cannot be in the past")
    val scheduledDate: LocalDate?
)