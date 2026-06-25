package com.theusmm.todo_list.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UserCreateRequestDto(
    @field:NotBlank(message = "Name is required")
    @field:Size(max = 50, message = "Name must contain a maximum of 50 characters.")
    val name: String,

    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Please provide a valid email address")
    val email: String,

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 6, message = "Password must be at least 6 characters long")
    val password: String
)
