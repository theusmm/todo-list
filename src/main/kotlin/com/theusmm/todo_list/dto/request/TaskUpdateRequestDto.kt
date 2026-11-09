package com.theusmm.todo_list.dto.request

import com.theusmm.todo_list.entity.TaskPriority
import com.theusmm.todo_list.entity.TaskStatus
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.FutureOrPresent
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDate

data class TaskUpdateRequestDto(
    @field:Schema(
        description = "Task title",
        example = "Study OpenAPI"
    )
    @field:NotBlank(message = "Title is required")
    @field:Size(max = 100, message = "Title must contain a maximum of 100 characters.")
    val title: String,

    @field:Schema(
        description = "Optional task description",
        example = "Study 3 hours of something"
    )
    val description: String?,

    @field:Schema(
        description = "Task status",
        example = "COMPLETED"
    )
    @field:NotNull(message = "Status is required")
    val status: TaskStatus,

    @field:Schema(
        description = "Task priority",
        example = "HIGH"
    )
    @field:NotNull(message = "Priority is required")
    val priority: TaskPriority,

    @field:Schema(
        description = "Deadline in YYYY-MM-DD format",
        example = "2027-01-30"
    )
    @field:NotNull(message = "Scheduled date is required")
    @field:FutureOrPresent(message = "Scheduled date cannot be in the past")
    val dueDate: LocalDate?
)