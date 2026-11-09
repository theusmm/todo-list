package com.theusmm.todo_list.controller

import com.theusmm.todo_list.dto.CustomPageDto
import com.theusmm.todo_list.dto.request.UserCreateRequestDto
import com.theusmm.todo_list.dto.request.UserUpdateRequestDto
import com.theusmm.todo_list.dto.response.UserResponseDto
import com.theusmm.todo_list.entity.User
import com.theusmm.todo_list.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@Tag(name = "Users")
@RestController
@RequestMapping("/users")
class UserController(private val service: UserService) {

    // ================== ALL USERS ==================
    @Operation(
        summary = "Create a user",
        description = "Creates a standard user account."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "User created"),
            ApiResponse(responseCode = "400", description = "Request validation failed or request data is invalid"),
            ApiResponse(responseCode = "403", description = "Email is already registered"),
            ApiResponse(responseCode = "500", description = "Unexpected error")
        ]
    )
    @PostMapping
    fun createUser(@Valid @RequestBody user: UserCreateRequestDto): ResponseEntity<UserResponseDto> {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createUser(user))
    }

    @Operation(
        summary = "Update my user account",
        description = "Updates the authenticated user's account."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "User updated"),
            ApiResponse(responseCode = "400", description = "Request validation failed or request data is invalid"),
            ApiResponse(responseCode = "401", description = "Authentication is required or the token is invalid"),
            ApiResponse(responseCode = "404", description = "User not found"),
            ApiResponse(responseCode = "500", description = "Unexpected error")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/me")
    fun updateMe(
        @Valid @RequestBody user: UserUpdateRequestDto,
        @AuthenticationPrincipal loggedUser: User
    ): ResponseEntity<UserResponseDto>
    {
        return ResponseEntity.ok(service.updateUser(user, loggedUser.id!!))
    }

    @Operation(
        summary = "Delete my user account",
        description = "Deletes the authenticated user's account."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "User deleted without response body"),
            ApiResponse(responseCode = "401", description = "Authentication is required or the token is invalid"),
            ApiResponse(responseCode = "404", description = "User not found"),
            ApiResponse(responseCode = "500", description = "Unexpected error")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/me")
    fun deleteMe(@AuthenticationPrincipal loggedUser: User): ResponseEntity<Void> {
        service.deleteUser(loggedUser.id!!)
        return ResponseEntity.noContent().build()
    }

    // ================== ADMIN ==================

    @Operation(
        summary = "Create an administrator",
        description = "Creates an administrator account. Requires the ADMIN role."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Admin created"),
            ApiResponse(responseCode = "400", description = "Request validation failed or request data is invalid"),
            ApiResponse(responseCode = "401", description = "Authentication is required or the token is invalid"),
            ApiResponse(responseCode = "403", description = "Email is already registered or authenticated user does not have the ADMIN role"),
            ApiResponse(responseCode = "500", description = "Unexpected error")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/admin")
    fun createAdmin(@Valid @RequestBody user: UserCreateRequestDto): ResponseEntity<UserResponseDto> {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createAdmin(user))
    }

    @Operation(
        summary = "List all users",
        description = "Returns a paginated list of all users. Requires the ADMIN role."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "User list returned, possibly empty"),
            ApiResponse(responseCode = "401", description = "Authentication is required or the token is invalid"),
            ApiResponse(responseCode = "403", description = "Authenticated user does not have the ADMIN role"),
            ApiResponse(responseCode = "500", description = "Unexpected error")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    fun getAll(
        @ParameterObject
        @PageableDefault(page = 0, size = 10, sort = ["name"]) pageable: Pageable
    ): ResponseEntity<CustomPageDto<UserResponseDto>> {
        return ResponseEntity.ok(service.getAllUsers(pageable))
    }

    @Operation(
        summary = "List user by id",
        description = "Returns the user with the specified ID. Requires the ADMIN role."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "User returned"),
            ApiResponse(responseCode = "401", description = "Authentication is required or the token is invalid"),
            ApiResponse(responseCode = "403", description = "Authenticated user does not have the ADMIN role"),
            ApiResponse(responseCode = "404", description = "User not found"),
            ApiResponse(responseCode = "500", description = "Unexpected error")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{userId}")
    fun getById(@PathVariable userId: Long): ResponseEntity<UserResponseDto> {
        return ResponseEntity.ok(service.getUserById(userId))
    }

    @Operation(
        summary = "Update user by id",
        description = "Updates the user with the specified ID. Requires the ADMIN role."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "User updated"),
            ApiResponse(responseCode = "400", description = "Request validation failed or request data is invalid"),
            ApiResponse(responseCode = "401", description = "Authentication is required or the token is invalid"),
            ApiResponse(responseCode = "403", description = "Authenticated user does not have the ADMIN role"),
            ApiResponse(responseCode = "404", description = "User not found"),
            ApiResponse(responseCode = "500", description = "Unexpected error")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{userId}")
    fun updateByAdmin(
        @RequestBody @Valid user: UserUpdateRequestDto, @PathVariable userId: Long
    ): ResponseEntity<UserResponseDto> {
        return ResponseEntity.ok(service.updateUser(user, userId))
    }

    @Operation(
        summary = "Delete user by id",
        description = "Deletes the user with the specified ID. Requires the ADMIN role."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "User deleted without response body"),
            ApiResponse(responseCode = "401", description = "Authentication is required or the token is invalid"),
            ApiResponse(responseCode = "403", description = "Authenticated user does not have the ADMIN role"),
            ApiResponse(responseCode = "404", description = "User not found"),
            ApiResponse(responseCode = "500", description = "Unexpected error")
        ]
    )
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{userId}")
    fun deleteByAdmin(@PathVariable userId: Long): ResponseEntity<Void> {
        service.deleteUser(userId)
        return ResponseEntity.noContent().build()
    }
}