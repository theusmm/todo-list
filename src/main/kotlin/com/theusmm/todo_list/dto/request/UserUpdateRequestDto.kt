package com.theusmm.todo_list.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UserUpdateRequestDto(
    @field:Schema(
        description = "User name",
        example = "Matheus Moura"
    )
    @field:NotBlank(message = "Name is required")
    @field:Size(max = 100, message = "Name must contain a maximum of 100 characters.")
    val name: String,

    @field:Schema(
        description = "User's password",
        example = "password123"
    )
    @field:NotBlank(message = "Password is required")
    @field:Size(min = 6, message = "Password must be at least 6 characters long")
    val password: String
)