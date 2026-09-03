package com.theusmm.todo_list.controller

import com.theusmm.todo_list.dto.CustomPageDto
import com.theusmm.todo_list.dto.request.UserCreateRequestDto
import com.theusmm.todo_list.dto.request.UserUpdateRequestDto
import com.theusmm.todo_list.dto.response.UserResponseDto
import com.theusmm.todo_list.entity.User
import com.theusmm.todo_list.service.UserService
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
class UserController(private val service: UserService) {

    // ================== ALL USERS ==================
    @PostMapping
    fun createUser(@Valid @RequestBody user: UserCreateRequestDto): ResponseEntity<UserResponseDto> {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createUser(user))
    }

    @PutMapping("/me")
    fun updateMe(
        @Valid @RequestBody user: UserUpdateRequestDto,
        @AuthenticationPrincipal loggedUser: User
    ): ResponseEntity<UserResponseDto>
    {
        return ResponseEntity.ok(service.updateUser(user, loggedUser.id!!))
    }

    @DeleteMapping("/me")
    fun deleteMe(@AuthenticationPrincipal loggedUser: User): ResponseEntity<Void> {
        service.deleteUser(loggedUser.id!!)
        return ResponseEntity.noContent().build()
    }

    // ================== ADMIN ==================

    @PostMapping("/admin")
    fun createAdmin(@Valid @RequestBody user: UserCreateRequestDto): ResponseEntity<UserResponseDto> {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createAdmin(user))
    }

    @GetMapping
    fun getAll(
        @PageableDefault(page = 0, size = 10, sort = ["name"]) pageable: Pageable
    ): ResponseEntity<CustomPageDto<UserResponseDto>> {
        return ResponseEntity.ok(service.getAllUsers(pageable))
    }

    @GetMapping("/{userId}")
    fun getById(@PathVariable userId: Long): ResponseEntity<UserResponseDto> {
        return ResponseEntity.ok(service.getUserById(userId))
    }

    @PutMapping("/{userId}")
    fun updateByAdmin(
        @RequestBody @Valid user: UserUpdateRequestDto, @PathVariable userId: Long
    ): ResponseEntity<UserResponseDto> {
        return ResponseEntity.ok(service.updateUser(user, userId))
    }

    @DeleteMapping("/{userId}")
    fun deleteByAdmin(@PathVariable userId: Long): ResponseEntity<Void> {
        service.deleteUser(userId)
        return ResponseEntity.noContent().build()
    }
}