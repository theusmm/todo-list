package com.theusmm.todo_list.config

import com.theusmm.todo_list.dto.request.LoginRequestDto
import com.theusmm.todo_list.repository.UserRepository
import com.theusmm.todo_list.service.TokenService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class SecurityFilter(
    private val tokenService: TokenService,
    private val userRepository: UserRepository
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = recoverToken(request)

        if (token != null) {
            val email = tokenService.validateToken(token)

            if (email.isNotBlank()) {
                val user = userRepository.findByEmail(email)

                if (user != null) {
                    val authentication = UsernamePasswordAuthenticationToken(user, null, emptyList())
                    SecurityContextHolder.getContext().authentication = authentication
                }
            }
        }
        filterChain.doFilter(request, response)
    }

    private fun recoverToken(request: HttpServletRequest): String? {
        val authHeader = request.getHeader("Authorization") ?: return null

        if (!authHeader.startsWith("Bearer ")) return null
        return authHeader.replace("Bearer ", "")
    }
}