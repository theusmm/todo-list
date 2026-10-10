package com.theusmm.todo_list.service

import com.theusmm.todo_list.dto.CustomPageDto
import com.theusmm.todo_list.dto.request.UserCreateRequestDto
import com.theusmm.todo_list.dto.request.UserUpdateRequestDto
import com.theusmm.todo_list.dto.response.UserResponseDto
import com.theusmm.todo_list.entity.User
import com.theusmm.todo_list.entity.UserRole
import com.theusmm.todo_list.exception.AccessDeniedException
import com.theusmm.todo_list.exception.ResourceNotFoundException
import com.theusmm.todo_list.mapper.toCreateEntity
import com.theusmm.todo_list.mapper.toUpdateEntity
import com.theusmm.todo_list.mapper.toResponseDto
import com.theusmm.todo_list.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Pageable
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService(
    private val repository: UserRepository,
    private val passwordEncoder: PasswordEncoder) {

    @Cacheable(cacheNames = ["users"], key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    fun getAllUsers(pageable: Pageable): CustomPageDto<UserResponseDto> {
        val userPage = repository.findAll(pageable).map { it.toResponseDto() }
        return CustomPageDto(userPage)
    }

    fun getUserById(userId: Long): UserResponseDto {
        val user = checkIfUserExists(userId)
        return user.toResponseDto()
    }

    // users with role USER
    @CacheEvict(cacheNames = ["users"], allEntries = true)
    @Transactional
    fun createUser(user: UserCreateRequestDto): UserResponseDto {
        return saveUserWithRole(user, UserRole.USER).toResponseDto()
    }

    // users with role ADMIN
    @CacheEvict(cacheNames = ["users"], allEntries = true)
    @Transactional
    fun createAdmin(user: UserCreateRequestDto): UserResponseDto {
        return saveUserWithRole(user, UserRole.ADMIN).toResponseDto()
    }

    // create users
    private fun saveUserWithRole(user: UserCreateRequestDto, role: UserRole): User {
        if (repository.existsByEmail(user.email)) {
            throw AccessDeniedException("This E-mail is already registered!")
        }

        val newUser = user.toCreateEntity()
        newUser.password = passwordEncoder.encode(user.password)!!
        newUser.role = role

        return repository.save(newUser)
    }

    @CacheEvict(cacheNames = ["users"], allEntries = true)
    @Transactional
    fun updateUser(user: UserUpdateRequestDto, userId: Long): UserResponseDto {
        val updatedUser = checkIfUserExists(userId)
        user.toUpdateEntity(updatedUser)

        updatedUser.password = passwordEncoder.encode(user.password)!!

        return repository.save(updatedUser).toResponseDto()
    }

    @CacheEvict(cacheNames = ["users"], allEntries = true)
    @Transactional
    fun deleteUser(userId: Long) {
        val user = checkIfUserExists(userId)
        repository.delete(user)
    }

    fun checkIfUserExists(userId: Long): User = repository.findById(userId)
        .orElseThrow { ResourceNotFoundException("User not found") }
}