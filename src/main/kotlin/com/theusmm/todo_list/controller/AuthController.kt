package com.theusmm.todo_list.controller

import com.theusmm.todo_list.dto.request.LoginRequestDto
import com.theusmm.todo_list.dto.response.LoginResponseDto
import com.theusmm.todo_list.repository.UserRepository
import com.theusmm.todo_list.service.TokenService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/auth")
class AuthController(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val tokenService: TokenService
) {
    @PostMapping("/login")
    fun login(@RequestBody @Valid login: LoginRequestDto) : ResponseEntity<LoginResponseDto> {
        val user = userRepository.findByEmail(login.email)
            ?: return ResponseEntity.status(401).build()

        if (!passwordEncoder.matches(login.password, user.password)) {
            return ResponseEntity.status(401).build()
        }

        val token = tokenService.generateToken(user)
        return ResponseEntity.ok(LoginResponseDto(token))
    }
}