package com.theusmm.todo_list.controller

import com.theusmm.todo_list.dto.request.UserCreateRequestDto
import com.theusmm.todo_list.dto.request.UserUpdateRequestDto
import com.theusmm.todo_list.dto.response.UserResponseDto
import com.theusmm.todo_list.mapper.toUserResponseDto
import com.theusmm.todo_list.service.UserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
class UserController(private val service: UserService){

    @GetMapping
    fun getAll(): ResponseEntity<List<UserResponseDto>> {
        val users = service.getAllUsers().map { it.toUserResponseDto() }
        return ResponseEntity.ok(users)
    }

    @GetMapping("/{userId}")
    fun getById(@PathVariable userId: Long): ResponseEntity<UserResponseDto> {
        val user = service.getUserById(userId)
        return ResponseEntity.ok(user.toUserResponseDto())
    }

    @PostMapping()
    fun create(@Valid @RequestBody user: UserCreateRequestDto): ResponseEntity<UserResponseDto> {
        val newUser = service.createUser(user)
        return ResponseEntity.status(HttpStatus.CREATED).body(newUser.toUserResponseDto())
    }

    @PutMapping("/{userId}")
    fun update(
        @Valid @RequestBody user: UserUpdateRequestDto,
        @PathVariable userId: Long): ResponseEntity<UserResponseDto>
    {
        val updatedUser = service.updateUser(user, userId)
        return ResponseEntity.ok(updatedUser.toUserResponseDto())
    }

    @DeleteMapping("/{userId}")
    fun delete(@PathVariable userId: Long): ResponseEntity<Unit> {
        service.deleteUser(userId)
        return ResponseEntity.noContent().build()
    }
}