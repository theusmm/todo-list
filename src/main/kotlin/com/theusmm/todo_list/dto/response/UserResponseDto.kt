package com.theusmm.todo_list.dto.response

import java.io.Serializable

data class UserResponseDto(
    val id: Long?,
    val name: String,
    val email: String
) : Serializable
