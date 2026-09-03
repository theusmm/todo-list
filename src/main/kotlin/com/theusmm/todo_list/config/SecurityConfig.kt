package com.theusmm.todo_list.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val securityFilter: SecurityFilter
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->

                // PUBLIC
                auth.requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                auth.requestMatchers(HttpMethod.POST, "/users").permitAll()

                // /users
                auth.requestMatchers("/users/me").authenticated()

                // /users - ADMIN
                auth.requestMatchers(HttpMethod.GET,"/users").hasRole("ADMIN")
                auth.requestMatchers("/users/admin/**").hasRole("ADMIN")
                auth.requestMatchers("/users/{userId}").hasRole("ADMIN")

                // /tasks
                auth.requestMatchers("/tasks/admin/**").hasRole("ADMIN")
                auth.requestMatchers("/tasks/**").authenticated()

                auth.anyRequest().authenticated()
            }
            .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    @Bean
    fun authenticationManager(authenticationConfiguration: AuthenticationConfiguration): AuthenticationManager {
        return authenticationConfiguration.authenticationManager
    }
}