package com.theusmm.todo_list.mapper

import com.theusmm.todo_list.entity.User
import com.theusmm.todo_list.dto.request.UserCreateRequestDto
import com.theusmm.todo_list.dto.request.UserUpdateRequestDto
import com.theusmm.todo_list.dto.response.UserResponseDto

fun UserCreateRequestDto.toCreateEntity(): User {
    return User(
        name = this.name,
        email = this.email,
        password = this.password
    )
}

fun UserUpdateRequestDto.toUpdateEntity(user: User) {
    user.name = this.name
    user.password = this.password
}

fun User.toUserResponseDto() = UserResponseDto(
    id = this.id,
    name = this.name,
    email = this.email
)