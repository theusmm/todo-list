package com.theusmm.todo_list.controller

import com.theusmm.todo_list.dto.CustomPageDto
import com.theusmm.todo_list.dto.request.UserCreateRequestDto
import com.theusmm.todo_list.dto.request.UserUpdateRequestDto
import com.theusmm.todo_list.dto.response.UserResponseDto
import com.theusmm.todo_list.mapper.toUserResponseDto
import com.theusmm.todo_list.service.UserService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
class UserController(private val service: UserService){

    @GetMapping
    fun getAll(
        @PageableDefault(page = 0, size = 10, sort = ["id"]) pageable: Pageable
    ): ResponseEntity<CustomPageDto<UserResponseDto>> {
        val usersPage = service.getAllUsers(pageable)
        return ResponseEntity.ok(usersPage)
    }

    @GetMapping("/{userId}")
    fun getById(@PathVariable userId: Long): ResponseEntity<UserResponseDto> {
        val user = service.getUserById(userId)
        return ResponseEntity.ok(user)
    }

    @PostMapping()
    fun create(@Valid @RequestBody user: UserCreateRequestDto): ResponseEntity<UserResponseDto> {
        val newUser = service.createUser(user)
        return ResponseEntity.status(HttpStatus.CREATED).body(newUser)
    }

    @PutMapping("/{userId}")
    fun update(
        @Valid @RequestBody user: UserUpdateRequestDto,
        @PathVariable userId: Long): ResponseEntity<UserResponseDto>
    {
        val updatedUser = service.updateUser(user, userId)
        return ResponseEntity.ok(updatedUser)
    }

    @DeleteMapping("/{userId}")
    fun delete(@PathVariable userId: Long): ResponseEntity<Unit> {
        service.deleteUser(userId)
        return ResponseEntity.noContent().build()
    }
}