package com.theusmm.todo_list.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class LoginRequestDto(
    @field:Schema(
        description = "User's email address",
        example = "user@email.com"
    )
    @field:NotBlank(message = "Email cannot be blank")
    @field:Email(message = "Invalid email format")
    val email: String,

    @field:Schema(
        description = "User's password",
        example = "password123"
    )
    @field:NotBlank(message = "Password cannot be blank")
    val password: String
)
