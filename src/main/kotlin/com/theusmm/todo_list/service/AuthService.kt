package com.theusmm.todo_list.service

import com.theusmm.todo_list.dto.request.LoginRequestDto
import com.theusmm.todo_list.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val tokenService: TokenService
) {
    fun authenticate(login: LoginRequestDto): String? {
        val user = userRepository.findByEmail(login.email)

        val actualPasswordInDb = user?.password ?: "\$2a\$10\$DummyHashForTimingAttackSecretString"
        val isPasswordValid = passwordEncoder.matches(login.password, actualPasswordInDb)

        if (user == null || !isPasswordValid) return null
        return tokenService.generateToken(user)
    }
}