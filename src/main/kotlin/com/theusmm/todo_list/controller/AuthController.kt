package com.theusmm.todo_list.controller

import com.theusmm.todo_list.dto.request.LoginRequestDto
import com.theusmm.todo_list.dto.response.LoginResponseDto
import com.theusmm.todo_list.service.AuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Authentication", description = "Login and obtaining the JWT token")
@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService
) {
    @Operation(
        summary = "Log in",
        description = "Validates the credentials and returns a JWT token"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successful login"),
            ApiResponse(responseCode = "401", description = "Invalid credentials"),
            ApiResponse(responseCode = "400", description = "Invalid input data")
        ]
    )
    @PostMapping("/login")
    fun login(@RequestBody @Valid login: LoginRequestDto) : ResponseEntity<LoginResponseDto> {
        val authToken = authService.authenticate(login)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        return ResponseEntity.ok(LoginResponseDto(authToken))
    }
}